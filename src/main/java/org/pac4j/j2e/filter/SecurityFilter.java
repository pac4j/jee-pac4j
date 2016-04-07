package org.pac4j.j2e.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.authorization.checker.AuthorizationChecker;
import org.pac4j.core.authorization.checker.DefaultAuthorizationChecker;
import org.pac4j.core.client.*;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.client.finder.ClientFinder;
import org.pac4j.core.client.finder.DefaultClientFinder;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigBuilder;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.*;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.matching.DefaultMatchingChecker;
import org.pac4j.core.matching.MatchingChecker;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter protects an url by checking that the user is authenticated and that the authorizations are checked, according to the clients and authorizers configuration.
 * If the user is not authenticated, it performs authentication for direct clients or starts the login process for indirect clients.</p>
 *
 * <p>The configuration can be provided via servlet parameters: <code>configFactory</code> (configuration factory), <code>clients</code> (list of clients for authentication),
 * <code>authorizers</code> (list of authorizers), <code>matchers</code> (list of matchers) and <code>multiProfile</code>  (whether multiple profiles should be kept).</p>
 * <p>Or it can be defined via setter methods: {@link #setConfig(Config)}, {@link #setClients(String)}, {@link #setAuthorizers(String)}, {@link #setMatchers(String)} and {@link #setMultiProfile(boolean)}.</p>
 *
 * @author Jerome Leleu, Michael Remond
 * @since 1.0.0
 */
public class SecurityFilter extends AbstractConfigFilter {

    protected ClientFinder clientFinder = new DefaultClientFinder();

    protected AuthorizationChecker authorizationChecker = new DefaultAuthorizationChecker();

    protected MatchingChecker matchingChecker = new DefaultMatchingChecker();

    protected String clients;

    protected String authorizers;

    protected String matchers;

    protected boolean multiProfile;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final String configFactoryParam = filterConfig.getInitParameter(Pac4jConstants.CONFIG_FACTORY);
        if (configFactoryParam != null) {
            final Config config = ConfigBuilder.build(configFactoryParam);
            ConfigSingleton.setConfig(config);
        }
        this.clients = getStringParam(filterConfig, Pac4jConstants.CLIENTS, this.clients);
        this.authorizers = getStringParam(filterConfig, Pac4jConstants.AUTHORIZERS, this.authorizers);
        this.matchers = getStringParam(filterConfig, Pac4jConstants.MATCHERS, this.matchers);
        this.multiProfile = getBooleanParam(filterConfig, Pac4jConstants.MULTI_PROFILE, this.multiProfile);

        // to help with backward compatibility
        checkForbiddenParameter(filterConfig, "clientsFactory");
        checkForbiddenParameter(filterConfig, "isAjax");
        checkForbiddenParameter(filterConfig, "stateless");
        checkForbiddenParameter(filterConfig, "requireAnyRole");
        checkForbiddenParameter(filterConfig, "requireAllRoles");
        checkForbiddenParameter(filterConfig, "clientName");
        checkForbiddenParameter(filterConfig, "authorizerName");
        checkForbiddenParameter(filterConfig, "matcherName");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                        final FilterChain chain) throws IOException, ServletException {

        final Config config = ConfigSingleton.getConfig();
        assertNotNull("config", config);
        final WebContext context = new J2EContext(request, response, config.getSessionStore());

        logger.debug("url: {}", context.getFullRequestURL());
        logger.debug("matchers: {}", matchers);
        if (matchingChecker.matches(context, this.matchers, getConfig().getMatchers())) {

            final Clients configClients = config.getClients();
            assertNotNull("configClients", configClients);
            logger.debug("clients: {}", clients);
            final List<Client> currentClients = clientFinder.find(configClients, context, this.clients);
            logger.debug("currentClients: {}", currentClients);

            final boolean loadProfilesFromSession = loadProfilesFromSession(context, currentClients);
            logger.debug("loadProfilesFromSession: {}", loadProfilesFromSession);
            final ProfileManager manager = new ProfileManager(context);
            List<CommonProfile> profiles = manager.getAll(loadProfilesFromSession);
            logger.debug("profiles: {}", profiles);
            logger.debug("multiProfile: {}", multiProfile);

            try {

                // no profile and some current clients
                if (isEmpty(profiles) && isNotEmpty(currentClients)) {
                    // loop on all clients searching direct ones to perform authentication
                    for (final Client currentClient : currentClients) {
                        if (currentClient instanceof DirectClient) {
                            logger.debug("Performing authentication for client: {}", currentClient);

                            final Credentials credentials = currentClient.getCredentials(context);
                            final CommonProfile profile = currentClient.getUserProfile(credentials, context);
                            logger.debug("profile: {}", profile);
                            if (profile != null) {
                                manager.save(false, profile, this.multiProfile);
                                if (!this.multiProfile) {
                                    break;
                                }
                            }
                        }
                    }
                    profiles = manager.getAll(loadProfilesFromSession);
                    logger.debug("new profiles: {}", profiles);
                }

                if (isNotEmpty(profiles)) {
                    logger.debug("authorizers: {}", authorizers);
                    if (authorizationChecker.isAuthorized(context, profiles, authorizers, config.getAuthorizers())) {
                        logger.debug("authenticated and authorized -> grant access");
                        chain.doFilter(request, response);
                    } else {
                        logger.debug("forbidden");
                        forbidden(context, currentClients, profiles, authorizers);
                    }
                } else {
                    if (startAuthentication(context, currentClients)) {
                        logger.debug("Starting authentication");
                        saveRequestedUrl(context, currentClients);
                        redirectToIdentityProvider(context, currentClients);
                    } else {
                        logger.debug("unauthorized");
                        unauthorized(context, currentClients);
                    }
                }

            } catch (final RequiresHttpAction e) {
                logger.debug("extra HTTP action required in security filter: {}", e.getCode());
                return;
            }

        } else {

            logger.debug("no matching for this request -> grant access");
            chain.doFilter(request, response);
        }
    }

    protected boolean loadProfilesFromSession(final WebContext context, final List<Client> currentClients) {
        return isEmpty(currentClients) || currentClients.get(0) instanceof IndirectClient || currentClients.get(0) instanceof AnonymousClient;
    }

    protected void forbidden(final WebContext context, final List<Client> currentClients, final List<CommonProfile> profile, final String authorizers) {
        context.setResponseStatus(HttpConstants.FORBIDDEN);
    }

    protected boolean startAuthentication(final WebContext context, final List<Client> currentClients) {
        return isNotEmpty(currentClients) && currentClients.get(0) instanceof IndirectClient;
    }

    protected void saveRequestedUrl(final WebContext context, final List<Client> currentClients) {
        final String requestedUrl = context.getFullRequestURL();
        logger.debug("requestedUrl: {}", requestedUrl);
        context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
    }

    protected void redirectToIdentityProvider(final WebContext context, final List<Client> currentClients) throws RequiresHttpAction {
        final IndirectClient currentClient = (IndirectClient) currentClients.get(0);
        currentClient.redirect(context);
    }

    protected void unauthorized(final WebContext context, final List<Client> currentClients) {
        context.setResponseStatus(HttpConstants.UNAUTHORIZED);
    }

    public Config getConfig() {
        return ConfigSingleton.getConfig();
    }

    public void setConfig(final Config config) {
        ConfigSingleton.setConfig(config);
    }

    public String getClients() {
        return clients;
    }

    public void setClients(String clients) {
        this.clients = clients;
    }

    public String getAuthorizers() {
        return authorizers;
    }

    public void setAuthorizers(String authorizers) {
        this.authorizers = authorizers;
    }

    public String getMatchers() {
        return matchers;
    }

    public void setMatchers(String matchers) {
        this.matchers = matchers;
    }

    public boolean isMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(boolean multiProfile) {
        this.multiProfile = multiProfile;
    }
}

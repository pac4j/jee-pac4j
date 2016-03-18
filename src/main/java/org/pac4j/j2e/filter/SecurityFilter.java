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
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter protects a resource (authentication + authorizations):</p>
 * <ul>
 *  <li>If an indirect client is used, it relies on the session to get the user profile(s) (after the {@link CallbackFilter} has terminated the authentication process)</li>
 *  <li>If a direct client is used, it validates the provided credentials from the request and retrieves the user profile(s) if the authentication succeeds.</li>
 * </ul>
 * <p>Then, authorizations are checked before accessing the resource. Forbidden or unauthorized errors can be returned.</p>
 * <p>An authentication process can be started (redirection to the identity provider) in case of an indirect client.</p>
 * <p>The configuration can be provided via servlet parameters: <code>configFactory</code> (configuration factory), <code>clients</code> (list of clients for authentication),
 * <code>authorizers</code> (list of authorizers), <code>matchers</code> (list of matchers) and <code>multiProfile</code>  (whether multiple profiles should be kept).</p>
 * <p>Or it can be defined via setter methods: {@link #setConfig(Config)}, {@link #setClients(String)}, {@link #setAuthorizers(String)}, {@link #setMatchers(String)} and {@link #setMultiProfile(boolean)}.</p>
 *
 * @author Jerome Leleu, Michael Remond
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
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

            final boolean useSession = useSession(context, currentClients);
            logger.debug("useSession: {}", useSession);
            final ProfileManager manager = new ProfileManager(context);
            List<UserProfile> profiles = manager.getAll(useSession);
            logger.debug("profiles: {}", profiles);

            // no profile and some current clients
            if (isEmpty(profiles) && isNotEmpty(currentClients)) {
                // loop on all clients searching direct ones to perform authentication
                for (final Client currentClient : currentClients) {
                    if (currentClient instanceof DirectClient) {
                        logger.debug("Performing authentication for client: {}", currentClient);
                        final Credentials credentials;
                        try {
                            credentials = currentClient.getCredentials(context);
                            logger.debug("credentials: {}", credentials);
                        } catch (final RequiresHttpAction e) {
                            logger.debug("extra HTTP action required: {}", e.getCode());
                            return;
                        }
                        final UserProfile profile = currentClient.getUserProfile(credentials, context);
                        logger.debug("profile: {}", profile);
                        if (profile != null) {
                            manager.save(useSession, profile, this.multiProfile);
                            if (!this.multiProfile) {
                                break;
                            }
                        }
                    }
                }
                profiles = manager.getAll(useSession);
                logger.debug("new profiles: {}", profiles);
            }

            if (isNotEmpty(profiles)) {
                logger.debug("authorizers: {}", authorizers);
                if (authorizationChecker.isAuthorized(context, profiles, authorizers, config.getAuthorizers())) {
                    logger.debug("authenticated and authorized -> grant access");
                    chain.doFilter(request, response);
                } else {
                    logger.debug("forbidden");
                    forbidden(context, currentClients, profiles);
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

        } else {

            logger.debug("no matching for this request -> grant access");
            chain.doFilter(request, response);
        }
    }

    protected boolean useSession(final WebContext context, final List<Client> currentClients) {
        return isEmpty(currentClients) || currentClients.get(0) instanceof IndirectClient;
    }

    protected void forbidden(final WebContext context, final List<Client> currentClients, final List<UserProfile> profile) {
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

    protected void redirectToIdentityProvider(final WebContext context, final List<Client> currentClients) {
        try {
            final IndirectClient currentClient = (IndirectClient) currentClients.get(0);
            currentClient.redirect(context);
        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required: {}", e.getCode());
        }
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

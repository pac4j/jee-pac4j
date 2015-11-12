/*
  Copyright 2013 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.j2e.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.authorization.AuthorizationChecker;
import org.pac4j.core.authorization.DefaultAuthorizationChecker;
import org.pac4j.core.client.*;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigBuilder;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.*;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.matching.DefaultMatchingChecker;
import org.pac4j.core.matching.MatchingChecker;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;

/**
 * <p>This filter protects a resource (authentication + authorization).</p>
 * <ul>
 *  <li>If a stateful / indirect client is used, it relies on the session to get the user profile (after the {@link CallbackFilter} has terminated the authentication process)</li>
 *  <li>If a stateless / direct client is used, it validates the provided credentials from the request and retrieves the user profile if the authentication succeeds.</li>
 * </ul>
 * <p>Then, authorizations are checked before accessing the resource.</p>
 * <p>Forbidden or unauthorized errors can be returned. An authentication process can be started (redirection to the identity provider) in case of an indirect client.</p>
 * <p>The configuration can be provided via servlet parameters: <code>configFactory</code>, <code>clientName</code>, <code>authorizerName</code> and <code>matcherName</code>.</p>
 * <p>Or it can be defined via setter methods: {@link #setConfig(Config)}, {@link #setClientName(String)}, {@link #setAuthorizerName(String)} and {@link #setMatcherName(String)} .</p>
 *
 * @author Jerome Leleu, Michael Remond
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class RequiresAuthenticationFilter extends AbstractConfigFilter {

    protected ClientFinder clientFinder = new DefaultClientFinder();

    protected AuthorizationChecker authorizationChecker = new DefaultAuthorizationChecker();

    protected MatchingChecker matchingChecker = new DefaultMatchingChecker();

    protected String clientName;

    protected String authorizerName;

    protected String matcherName;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final String configFactoryParam = filterConfig.getInitParameter(Pac4jConstants.CONFIG_FACTORY);
        if (configFactoryParam != null) {
            final Config config = ConfigBuilder.build(configFactoryParam);
            ConfigSingleton.setConfig(config);
        }
        this.clientName = getStringParam(filterConfig, Pac4jConstants.CLIENT_NAME, this.clientName);
        this.authorizerName = getStringParam(filterConfig, Pac4jConstants.AUTHORIZER_NAME, this.authorizerName);
        this.matcherName = getStringParam(filterConfig, Pac4jConstants.MATCHER_NAME, this.matcherName);

        // to help with backward compatibility
        checkUselessParameter(filterConfig, "clientsFactory");
        checkUselessParameter(filterConfig, Pac4jConstants.IS_AJAX);
        checkUselessParameter(filterConfig, Pac4jConstants.STATELESS);
        checkForbiddenParameter(filterConfig, Pac4jConstants.REQUIRE_ANY_ROLE);
        checkForbiddenParameter(filterConfig, Pac4jConstants.REQUIRE_ALL_ROLES);
    }

    @Override
    protected final void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                        final FilterChain chain) throws IOException, ServletException {

        final WebContext context = new J2EContext(request, response);
        logger.debug("url: {}", context.getFullRequestURL());
        final Config config = ConfigSingleton.getConfig();
        CommonHelper.assertNotNull("config", config);

        logger.debug("matcherName: {}", matcherName);
        if (matchingChecker.matches(context, this.matcherName, getConfig().getMatchers())) {

            final Clients configClients = config.getClients();
            CommonHelper.assertNotNull("configClients", configClients);
            logger.debug("clientName: {}", clientName);
            final List<Client> currentClients = clientFinder.find(configClients, context, this.clientName);
            logger.debug("currentClients: {}", currentClients);

            final boolean useSession = useSession(context, currentClients);
            logger.debug("useSession: {}", useSession);
            final ProfileManager manager = new ProfileManager(context);
            UserProfile profile = manager.get(useSession);
            logger.debug("profile: {}", profile);

            // no profile and some current clients
            if (profile == null && currentClients != null && currentClients.size() > 0) {
                // loop on all clients searching direct ones to perform authentication
                for (final Client currentClient : currentClients) {
                    if (currentClient instanceof DirectClient) {
                        logger.debug("Performing authentication for client: {}", currentClient);
                        final Credentials credentials;
                        try {
                            credentials = currentClient.getCredentials(context);
                            logger.debug("credentials: {}", credentials);
                        } catch (final RequiresHttpAction e) {
                            throw new TechnicalException("Unexpected HTTP action", e);
                        }
                        profile = currentClient.getUserProfile(credentials, context);
                        logger.debug("profile: {}", profile);
                        if (profile != null) {
                            manager.save(useSession, profile);
                            break;
                        }
                    }
                }
            }

            if (profile != null) {
                logger.debug("authorizerName: {}", authorizerName);
                if (authorizationChecker.isAuthorized(context, profile, authorizerName, config.getAuthorizers())) {
                    logger.debug("authenticated and authorized -> grant access");
                    chain.doFilter(request, response);
                } else {
                    logger.debug("forbidden");
                    forbidden(context, currentClients, profile);
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
        return currentClients == null || currentClients.size() == 0 || currentClients.get(0) instanceof IndirectClient;
    }

    protected void forbidden(final WebContext context, final List<Client> currentClients, final UserProfile profile) {
        context.setResponseStatus(HttpConstants.FORBIDDEN);
    }

    protected boolean startAuthentication(final WebContext context, final List<Client> currentClients) {
        return currentClients != null && currentClients.size() > 0 && currentClients.get(0) instanceof IndirectClient;
    }

    protected void saveRequestedUrl(final WebContext context, final List<Client> currentClients) {
        final String requestedUrl = context.getFullRequestURL();
        logger.debug("requestedUrl: {}", requestedUrl);
        context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
    }

    protected void redirectToIdentityProvider(final WebContext context, final List<Client> currentClients) {
        try {
            final IndirectClient currentClient = (IndirectClient) currentClients.get(0);
            currentClient.redirect(context, true);
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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(final String clientName) {
        this.clientName = clientName;
    }

    public String getAuthorizerName() {
        return authorizerName;
    }

    public void setAuthorizerName(final String authorizerName) {
        this.authorizerName = authorizerName;
    }

    public String getMatcherName() {
        return matcherName;
    }

    public void setMatcherName(String matcherName) {
        this.matcherName = matcherName;
    }
}

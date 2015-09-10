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

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.authorization.Authorizer;
import org.pac4j.core.authorization.AuthorizerBuilder;
import org.pac4j.core.authorization.DefaultAuthorizerBuilder;
import org.pac4j.core.client.*;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigBuilder;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;

/**
 * <p>This filter protects a resource (authentication + authorization).</p>
 * <ul>
 *  <li>If a stateful / indirect client is used, it relies on the session to get the user profile (after the {@link CallbackFilter} has terminated the authentication process)</li>
 *  <li>If a stateless / direct client is used, it validates the provided credentials from the request and retrieves the user profile if the authentication succeeds.</li>
 * </ul>
 * <p>Then, the authorization is checked before accessing the resource.</p>
 * <p>Forbidden or unauthorized errors can be returned. An authentication process can be started (redirection to the identity provider) in case of an indirect client.</p>
 * <p>The configuration can be provided via servlet parameters: <code>configFactory</code>, <code>clientName</code> and <code>authorizerName</code>.</p>
 * <p>Or it can be defined via setter methods: {@link #setClients(Clients)}, {@link #setClientName(String)} and {@link #setAuthorizer(Authorizer)}.</p>
 *
 * @author Jerome Leleu, Michael Remond
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class RequiresAuthenticationFilter extends AbstractConfigFilter {

    protected ClientFinder clientFinder = new DefaultClientFinder();

    protected AuthorizerBuilder authorizerBuilder = new DefaultAuthorizerBuilder();

    protected String clientName;

    protected Authorizer authorizer;

    protected String authorizerName;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final String configFactoryParam = filterConfig.getInitParameter(Pac4jConstants.CONFIG_FACTORY);
        if (configFactoryParam != null) {
            final Config config = ConfigBuilder.build(configFactoryParam);
            ConfigSingleton.setConfig(config);
        }
        this.clientName = getStringParam(filterConfig, Pac4jConstants.CLIENT_NAME, this.clientName);
        this.authorizerName = getStringParam(filterConfig, Pac4jConstants.AUTHORIZER_NAME, this.authorizerName);

        final String isAjaxParameter = getStringParam(filterConfig, Pac4jConstants.IS_AJAX, null);
        if (CommonHelper.isNotBlank(isAjaxParameter)) {
            logger.warn("the isAjax servlet parameter is no longer necessary and will be ignored as AJAX requests are automatically detected");
        }
        checkUnsupportedrequireParameter(filterConfig, Pac4jConstants.REQUIRE_ANY_ROLE);
        checkUnsupportedrequireParameter(filterConfig, Pac4jConstants.REQUIRE_ALL_ROLES);
    }

    private void checkUnsupportedrequireParameter(final FilterConfig filterConfig, final String name) {
        final String parameter = getStringParam(filterConfig, name, null);
        if (CommonHelper.isNotBlank(parameter)) {
            final String message = "the " + name + " servlet parameter is no longer supported: the authorizerName servlet parameter must be used instead";
            logger.error(message);
            throw new TechnicalException(message);
        }
    }

    @Override
    protected final void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                        final FilterChain chain) throws IOException, ServletException {

        final WebContext context = new J2EContext(request, response);

        final Config config = ConfigSingleton.getConfig();
        CommonHelper.assertNotNull("config", config);
        final Clients clients = config.getClients();
        CommonHelper.assertNotNull("clients", clients);
        final Client client = clientFinder.find(clients, context, this.clientName);
        logger.debug("client: {}", client);
        this.authorizer = authorizerBuilder.build(context, this.authorizer, this.authorizerName, config.getAuthorizers());
        logger.debug("authorizer: {}", this.authorizer);
        CommonHelper.assertNotNull("authorizer", this.authorizer);

        final boolean useSession = useSession(context, client);
        logger.debug("useSession: {}", useSession);
        final ProfileManager manager = new ProfileManager(context);
        UserProfile profile = manager.get(useSession);
        logger.debug("profile: {}", profile);

        if (profile == null && client instanceof DirectClient) {
            final Credentials credentials;
            try {
                credentials = client.getCredentials(context);
                logger.debug("credentials: {}", credentials);
            } catch (final RequiresHttpAction e) {
                throw new TechnicalException("Unexpected HTTP action", e);
            }
            profile = client.getUserProfile(credentials, context);
            logger.debug("profile: {}", profile);
            if (profile != null) {
                manager.save(useSession, profile);
            }
        }

        if (profile != null) {
            if (authorizer.isAuthorized(context, profile)) {
                chain.doFilter(request, response);
            } else {
                context.setResponseStatus(HttpConstants.FORBIDDEN);
            }
        } else {
            if (client instanceof IndirectClient) {
                saveRequestedUrl(context);
                redirectToIdentityProvider(client, context);
            } else {
                context.setResponseStatus(HttpConstants.UNAUTHORIZED);
            }
        }
    }

    protected boolean useSession(final WebContext context, final Client client) {
        return client == null || client instanceof IndirectClient;
    }

    protected void saveRequestedUrl(final WebContext context) {
        final String requestedUrl = context.getFullRequestURL();
        logger.debug("requestedUrl: {}", requestedUrl);
        context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
    }

    protected void redirectToIdentityProvider(final Client client, final WebContext context) {
        try {
            client.redirect(context, true);
        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required: {}", e.getCode());
        }
    }

    public Clients getClients() {
        return ConfigSingleton.getConfig().getClients();
    }

    public void setClients(final Clients clients) {
        ConfigSingleton.getConfig().setClients(clients);
    }

    public String getClientName() {
        return this.clientName;
    }

    public void setClientName(final String clientName) {
        this.clientName = clientName;
    }

    public Authorizer getAuthorizer() {
        return this.authorizer;
    }

    public void setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
    }
}

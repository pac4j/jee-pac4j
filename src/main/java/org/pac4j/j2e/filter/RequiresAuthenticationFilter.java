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
import org.pac4j.core.authorization.DefaultAuthorizerBuilder;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.DirectClient;
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
 * <p>This filter aims to protect a (stateful or stateless) resource.</p>
 * <ul>
 *  <li>If statefull, it relies on the web session and on the {@link CallbackFilter} to terminate the authentication process</li>
 *  <li>If stateless, it validates the provided credentials and forward the request to the underlying resource if the authentication succeeds.</li>
 * </ul>
 * <p>Authorizations are also handled by this filter.</p>
 * <p>The configuration can be provided via servlet parameters: <code>configFactory</code>, <code>clientName</code>, <code>isAjax</code>,
 * <code>requireAnyRole</code>, <code>requireAllRoles</code>, <code>authorizerName</code>, <code>useSessionForDirectClient</code>
 * and <code>allowDynamicClientSelection</code>.</p>
 * <p>Or the configuration can be defined via setter methods: {@link #setClients(Clients)}, {@link #setClientName(String)}, {@link #setIsAjax(boolean)},
 * {@link #setRequireAnyRole(String)}, {@link #setRequireAllRoles(String)}, {@link #setAuthorizer(Authorizer)}, {@link #setUseSessionForDirectClient(boolean)}
 * and {@link #setAllowDynamicClientSelection(boolean)}.</p>
 *
 * @author Jerome Leleu, Michael Remond
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class RequiresAuthenticationFilter extends AbstractConfigFilter {

    protected String clientName;

    protected boolean isAjax = false;

    protected String requireAnyRole;

    protected String requireAllRoles;

    protected Authorizer authorizer;

    protected String authorizerName;

    protected boolean useSessionForDirectClient = false;

    protected boolean allowDynamicClientSelection = false;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final String configFactoryParam = filterConfig.getInitParameter(Pac4jConstants.CONFIG_FACTORY);
        if (configFactoryParam != null) {
            final Config config = ConfigBuilder.build(configFactoryParam);
            ConfigSingleton.setConfig(config);
        }

        this.clientName = getStringParam(filterConfig, Pac4jConstants.CLIENT_NAME, this.clientName);
        CommonHelper.assertNotNull(Pac4jConstants.CLIENT_NAME, this.clientName);

        this.isAjax = getBooleanParam(filterConfig, Pac4jConstants.IS_AJAX, this.isAjax);

        this.requireAnyRole = getStringParam(filterConfig, Pac4jConstants.REQUIRE_ANY_ROLE, this.requireAnyRole);
        this.requireAllRoles = getStringParam(filterConfig, Pac4jConstants.REQUIRE_ALL_ROLES, this.requireAllRoles);
        this.authorizerName = getStringParam(filterConfig, Pac4jConstants.AUTHORIZER_NAME, this.authorizerName);

        this.useSessionForDirectClient = getBooleanParam(filterConfig, Pac4jConstants.USE_SESSION_FOR_DIRECT_CLIENT, this.useSessionForDirectClient);
        this.allowDynamicClientSelection = getBooleanParam(filterConfig, Pac4jConstants.ALLOW_DYNAMIC_CLIENT_SELECTION, this.allowDynamicClientSelection);
    }

    @Override
    protected final void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                        final FilterChain chain) throws IOException, ServletException {

        final Config config = ConfigSingleton.getConfig();
        CommonHelper.assertNotNull("config", config);
        final Clients clients = config.getClients();
        CommonHelper.assertNotNull("clients", clients);
        this.authorizer = DefaultAuthorizerBuilder.build(this.authorizer, this.authorizerName, config.getAuthorizers(),
                this.requireAnyRole, this.requireAllRoles);
        CommonHelper.assertNotNull("authorizer", this.authorizer);

        final WebContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        final Client client = findClient(context);
        logger.debug("client: {}", client);
        final boolean isDirectClient = client instanceof DirectClient;

        UserProfile profile = manager.get(!isDirectClient || this.useSessionForDirectClient);
        logger.debug("profile: {}", profile);

        if (profile == null && isDirectClient) {
            final Credentials credentials;
            try {
                credentials = client.getCredentials(context);
            } catch (final RequiresHttpAction e) {
                throw new TechnicalException("Unexpected HTTP action", e);
            }
            logger.debug("credentials: {}", credentials);

            profile = client.getUserProfile(credentials, context);
            logger.debug("profile: {}", profile);
            if (profile != null) {
                manager.save(this.useSessionForDirectClient, profile);
            }
        }

        if (profile != null) {
            if (authorizer.isAuthorized(context, profile)) {
                chain.doFilter(request, response);
            } else {
                context.setResponseStatus(HttpConstants.FORBIDDEN);
            }
        } else {
            if (isDirectClient) {
                context.setResponseStatus(HttpConstants.UNAUTHORIZED);
            } else {
                saveRequestedUrl(context);
                redirectToIdentityProvider(client, context);
            }
        }
    }

    protected Client findClient(final WebContext context) {
        Client client = null;
        if (this.allowDynamicClientSelection) {
            client = getClients().findClient(context);
        }
        if (client == null) {
            client = getClients().findClient(clientName);
        }
        return client;
    }

    protected void saveRequestedUrl(final WebContext context) {
        if (!this.isAjax) {
            final String requestedUrl = context.getFullRequestURL();
            logger.debug("requestedUrl: {}", requestedUrl);
            context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
        }
    }

    protected void redirectToIdentityProvider(final Client client, final WebContext context) {
        try {
            client.redirect(context, true, this.isAjax);
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

    public boolean isAjax() {
        return isAjax;
    }

    public void setIsAjax(final boolean isAjax) {
        this.isAjax = isAjax;
    }

    public String getRequireAnyRole() {
        return requireAnyRole;
    }

    public void setRequireAnyRole(String requireAnyRole) {
        this.requireAnyRole = requireAnyRole;
    }

    public String getRequireAllRoles() {
        return requireAllRoles;
    }

    public void setRequireAllRoles(String requireAllRoles) {
        this.requireAllRoles = requireAllRoles;
    }

    public Authorizer getAuthorizer() {
        return this.authorizer;
    }

    public void setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
    }

    public boolean isUseSessionForDirectClient() {
        return this.useSessionForDirectClient;
    }

    public void setUseSessionForDirectClient(boolean useSessionForDirectClient) {
        this.useSessionForDirectClient = useSessionForDirectClient;
    }

    public boolean isAllowDynamicClientSelection() {
        return this.allowDynamicClientSelection;
    }

    public void setAllowDynamicClientSelection(boolean allowDynamicClientSelection) {
        this.allowDynamicClientSelection = allowDynamicClientSelection;
    }
}

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
import org.pac4j.core.authorization.IsAuthenticatedAuthorizer;
import org.pac4j.core.authorization.RequireAllRolesAuthorizer;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.DirectClient;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;

/**
 * <p>This filter aims to protect a secured (stateful or stateless) resource.</p>
 * <ul>
 *  <li>If statefull, it relies on the session and on the callback filter to terminate the authentication process.</li>
 *  <li>If stateless it validates the provided credentials and forward the request to the underlying resource if the authentication succeeds.</li>
 * </ul>
 * <p>The filter handles authorization based on an <code>Authorizer</code> (possibly built via two parameters: requireAnyRole and requireAllRoles).</p>
 * 
 * @author Jerome Leleu, Michael Remond
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class RequiresAuthenticationFilter extends ClientsConfigFilter {

    protected String clientName;

    protected boolean isAjax = false;

    protected String requireAnyRole;

    protected String requireAllRoles;

    protected Authorizer authorizer = new IsAuthenticatedAuthorizer();

    protected boolean useSessionForDirectClient = false;

    protected boolean allowDynamicClientSelection = false;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.clientName = getStringParam(filterConfig, "clientName", this.clientName);
        CommonHelper.assertNotNull("clientName", this.clientName);

        this.isAjax = getBooleanParam(filterConfig, "isAjax", this.isAjax);

        setRequireAnyRole(getStringParam(filterConfig, "requireAnyRole", this.requireAnyRole));
        setRequireAnyRole(getStringParam(filterConfig, "requireAllRoles", this.requireAllRoles));
        CommonHelper.assertNotNull("authorizer", this.authorizer);

        this.useSessionForDirectClient = getBooleanParam(filterConfig, "useSessionForDirectClient", this.useSessionForDirectClient);
        this.allowDynamicClientSelection = getBooleanParam(filterConfig, "allowDynamicClientSelection", this.allowDynamicClientSelection);
    }

    @Override
    protected final void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

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
                saveOriginalUrl(context);
                redirectToIdentityProvider(client, context);
            }
        }
    }

    /**
     * Find the client from the request.
     *
     * @param context the web context
     * @return the appropriate client
     */
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

    /**
     * Save the current requested url.
     *
     * @param context the web context
     */
    protected void saveOriginalUrl(final WebContext context) {
        if (!this.isAjax) {
            final String requestedUrl = context.getFullRequestURL();
            logger.debug("requestedUrl: {}", requestedUrl);
            context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
        }
    }

    /**
     * Redirect the user to the identity provider for login.
     *
     * @param client the current client
     * @param context the web context
     */
    protected void redirectToIdentityProvider(final Client client, final WebContext context) {
        try {
            client.redirect(context, true, this.isAjax);
        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required: {}", e.getCode());
        }
    }

    public String getClientName() {
        return this.clientName;
    }

    /**
     * The client name to use for this filter.
     *
     * @param clientName the client name
     */
    public void setClientName(final String clientName) {
        this.clientName = clientName;
    }

    public boolean isAjax() {
        return this.isAjax;
    }

    /**
     * Define if this filter will be called in an AJAX way.
     *
     * @param isAjax whether it is an AJAX call
     */
    public void setAjax(final boolean isAjax) {
        this.isAjax = isAjax;
    }

    public String getRequireAnyRole() {
        return this.requireAnyRole;
    }

    /**
     * Define that the user must have one of the roles to access the resource.
     *
     * @param requireAnyRole the roles list (separated by commas)
     */
    public void setRequireAnyRole(final String requireAnyRole) {
        this.requireAnyRole = requireAnyRole;
        if (requireAnyRole != null) {
            this.authorizer = new RequireAnyRoleAuthorizer(requireAnyRole.split(","));
        }
    }

    public String getRequireAllRoles() {
        return this.requireAllRoles;
    }

    /**
     * Define that the user must have all roles to access the resource.
     *
     * @param requireAllRoles the roles list (separated by commas)
     */
    public void setRequireAllRoles(final String requireAllRoles) {
        this.requireAllRoles = requireAllRoles;
        if (requireAllRoles != null) {
            this.authorizer = new RequireAllRolesAuthorizer(requireAllRoles.split(","));
        }
    }

    public Authorizer getAuthorizer() {
        return this.authorizer;
    }

    /**
     * The authorizer used to protect this resource.
     *
     * @param authorizer the authorizer
     */
    public void setAuthorizer(Authorizer authorizer) {
        this.authorizer = authorizer;
    }

    public boolean isUseSessionForDirectClient() {
        return this.useSessionForDirectClient;
    }

    /**
     * Define if the web session must be used even for REST support.
     *
     * @param useSessionForDirectClient whether the web session must be used even for REST support
     */
    public void setUseSessionForDirectClient(boolean useSessionForDirectClient) {
        this.useSessionForDirectClient = useSessionForDirectClient;
    }

    public boolean isAllowDynamicClientSelection() {
        return this.allowDynamicClientSelection;
    }

    /**
     * Define if other client can be used on this filter (in addition to the one defined by the {@link #setClientName(String)}.
     *
     * @param allowDynamicClientSelection whether other client can be used on this filter
     */
    public void setAllowDynamicClientSelection(boolean allowDynamicClientSelection) {
        this.allowDynamicClientSelection = allowDynamicClientSelection;
    }
}

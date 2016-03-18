package org.pac4j.j2e.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter handles the callback from the identity provider (indirect client) to finish the authentication process.</p>
 * <p>The configuration can be provided via servlet parameters: <code>defaultUrl</code> (default url after login if none was requested) and
 * <code>multiProfile</code> (whether multiple profiles should be kept).</p>
 * <p>Or it can be defined via setter methods: {@link #setDefaultUrl(String)} and {@link #setMultiProfile(boolean)}.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class CallbackFilter extends AbstractConfigFilter {

    protected String defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;

    protected boolean multiProfile;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        assertNotBlank(Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.multiProfile = getBooleanParam(filterConfig, Pac4jConstants.MULTI_PROFILE, this.multiProfile);

        // to help with backward compatibility
        checkForbiddenParameter(filterConfig, "clientsFactory");
    }

    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                           final FilterChain chain) throws IOException, ServletException {

        final Config config = ConfigSingleton.getConfig();
        assertNotNull("config", config);
        final WebContext context = new J2EContext(request, response, config.getSessionStore());

        final Clients clients = config.getClients();
        assertNotNull("clients", clients);
        final Client client = clients.findClient(context);
        logger.debug("client: {}", client);
        assertNotNull("client", client);
        assertTrue(client instanceof IndirectClient, "only indirect clients are allowed on the callback url");

        final Credentials credentials;
        try {
            credentials = client.getCredentials(context);
        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required: {}", e.getCode());
            return;
        }
        logger.debug("credentials: {}", credentials);

        final UserProfile profile = client.getUserProfile(credentials, context);
        logger.debug("profile: {}", profile);
        saveUserProfile(context, profile);
        redirectToOriginallyRequestedUrl(context, response);
    }

    protected void saveUserProfile(final WebContext context, final UserProfile profile) {
        final ProfileManager manager = new ProfileManager(context);
        if (profile != null) {
            manager.save(true, profile, this.multiProfile);
        }
    }

    protected void redirectToOriginallyRequestedUrl(final WebContext context, final HttpServletResponse response) throws IOException {
        final String requestedUrl = (String) context.getSessionAttribute(Pac4jConstants.REQUESTED_URL);
        logger.debug("requestedUrl: {}", requestedUrl);
        if (isNotBlank(requestedUrl)) {
            context.setSessionAttribute(Pac4jConstants.REQUESTED_URL, null);
            response.sendRedirect(requestedUrl);
        } else {
            response.sendRedirect(this.defaultUrl);
        }
    }

    public String getDefaultUrl() {
        return this.defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public boolean isMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(boolean multiProfile) {
        this.multiProfile = multiProfile;
    }
}

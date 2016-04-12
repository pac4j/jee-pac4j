package org.pac4j.j2e.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter finishes the login process for an indirect client.</p>
 *
 * <p>The configuration can be provided via servlet parameters: <code>defaultUrl</code> (default url after login if none was requested),
 * <code>multiProfile</code> (whether multiple profiles should be kept) and <code>renewSession</code> (whether the session must be renewed after login).</p>
 * <p>Or it can be defined via setter methods: {@link #setDefaultUrl(String)}, {@link #setMultiProfile(boolean)} and ({@link #setRenewSession(boolean)}.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class CallbackFilter extends AbstractConfigFilter {

    protected String defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;

    protected boolean multiProfile;

    protected boolean renewSession = true;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        assertNotBlank(Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.multiProfile = getBooleanParam(filterConfig, Pac4jConstants.MULTI_PROFILE, this.multiProfile);
        this.renewSession = getBooleanParam(filterConfig, Pac4jConstants.RENEW_SESSION, this.renewSession);

        // to help with backward compatibility
        checkForbiddenParameter(filterConfig, "clientsFactory");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                           final FilterChain chain) throws IOException, ServletException {

        final Config config = getConfig();
        assertNotNull("config", config);
        final J2EContext context = new J2EContext(request, response, config.getSessionStore());

        final Clients clients = config.getClients();
        assertNotNull("clients", clients);
        final Client client = clients.findClient(context);
        logger.debug("client: {}", client);
        assertNotNull("client", client);
        assertTrue(client instanceof IndirectClient, "only indirect clients are allowed on the callback url");

        try {
            final Credentials credentials = client.getCredentials(context);
            logger.debug("credentials: {}", credentials);

            final CommonProfile profile = client.getUserProfile(credentials, context);
            logger.debug("profile: {}", profile);
            saveUserProfile(context, profile);
            redirectToOriginallyRequestedUrl(context, response);

        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required in callback: {}", e.getCode());
        }
    }

    protected void saveUserProfile(final J2EContext context, final CommonProfile profile) {
        final ProfileManager manager = new ProfileManager(context);
        if (profile != null) {
            manager.save(true, profile, this.multiProfile);
            if (renewSession) {
                renewSession(context);
            }
        }
    }

    protected void renewSession(final J2EContext context) {
        logger.debug("Discard old session and replace by new one...");
        final HttpServletRequest request = context.getRequest();
        final HttpSession session = request.getSession();
        final Map<String, Object> attributes = new HashMap<>();
        Collections.list(session.getAttributeNames()).forEach(k -> attributes.put(k, session.getAttribute(k)));
        session.invalidate();
        final HttpSession newSession = request.getSession(true);
        attributes.forEach((k, v) -> newSession.setAttribute(k, v));
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

    public boolean isRenewSession() {
        return renewSession;
    }

    public void setRenewSession(boolean renewSession) {
        this.renewSession = renewSession;
    }
}

package org.pac4j.j2e.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.http.adapter.J2ENopHttpActionAdapter;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter finishes the login process for an indirect client, based on the {@link #callbackLogic}.</p>
 *
 * <p>The configuration can be provided via servlet parameters, setters or constructors for the following options:</p>
 * <ul>
 *     <li><code>configFactory</code> (the class name of the factory to build the configuration) or <code>config</code> (the configuration itself)</li>
 *     <li><code>defaultUrl</code> (default url after login if none was requested)</li>
 *     <li><code>saveInSession</code> (whether the profile should be saved into the session)</li>
 *     <li><code>multiProfile</code> (whether multiple profiles should be kept)</li>
 *     <li><code>renewSession</code> (whether the session must be renewed after login)</li>
 *     <li><code>defaultClient</code> (the default client if none is provided on the URL)</li>
 * </ul>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class CallbackFilter extends AbstractConfigFilter {

    private CallbackLogic<Object, J2EContext> callbackLogic = new DefaultCallbackLogic<>();

    private String defaultUrl;

    private Boolean saveInSession;

    private Boolean multiProfile;

    private Boolean renewSession;

    private String defaultClient;

    public CallbackFilter() {}

    public CallbackFilter(final Config config) {
        setConfig(config);
    }

    public CallbackFilter(final Config config, final String defaultUrl) {
        this(config);
        this.defaultUrl = defaultUrl;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.saveInSession = getBooleanParam(filterConfig, Pac4jConstants.SAVE_IN_SESSION, this.saveInSession);
        this.multiProfile = getBooleanParam(filterConfig, Pac4jConstants.MULTI_PROFILE, this.multiProfile);
        this.renewSession = getBooleanParam(filterConfig, Pac4jConstants.RENEW_SESSION, this.renewSession);
        this.defaultClient = getStringParam(filterConfig, Pac4jConstants.DEFAULT_CLIENT, this.defaultClient);

        // check backward incompatibility
        checkForbiddenParameter(filterConfig, "clientsFactory");
    }

    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                           final FilterChain chain) throws IOException, ServletException {

        assertNotNull("callbackLogic", callbackLogic);

        final Config config = getConfig();
        assertNotNull("config", config);
        final J2EContext context = new J2EContext(request, response, config.getSessionStore());

        callbackLogic.perform(context, config, J2ENopHttpActionAdapter.INSTANCE, this.defaultUrl, this.saveInSession, this.multiProfile, this.renewSession, this.defaultClient);
    }

    public String getDefaultUrl() {
        return this.defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public Boolean getSaveInSession() {
        return saveInSession;
    }

    public void setSaveInSession(final Boolean saveInSession) {
        this.saveInSession = saveInSession;
    }

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(final Boolean multiProfile) {
        this.multiProfile = multiProfile;
    }

    public Boolean getRenewSession() {
        return renewSession;
    }

    public void setRenewSession(final Boolean renewSession) {
        this.renewSession = renewSession;
    }

    public CallbackLogic<Object, J2EContext> getCallbackLogic() {
        return callbackLogic;
    }

    public void setCallbackLogic(final CallbackLogic<Object, J2EContext> callbackLogic) {
        this.callbackLogic = callbackLogic;
    }

    public String getDefaultClient() {
        return defaultClient;
    }

    public void setDefaultClient(final String defaultClient) {
        this.defaultClient = defaultClient;
    }
}

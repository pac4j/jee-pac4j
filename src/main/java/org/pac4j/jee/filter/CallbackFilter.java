package org.pac4j.jee.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;

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

    private CallbackLogic<Object, JEEContext> callbackLogic = new DefaultCallbackLogic<>();

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
        final JEEContext context = new JEEContext(request, response, config.getSessionStore());

        retrieveCallbackLogic().perform(context, config, retrieveHttpActionAdapter(), this.defaultUrl, this.saveInSession, this.multiProfile, this.renewSession, this.defaultClient);
    }

    protected CallbackLogic<Object, JEEContext> retrieveCallbackLogic() {
        if (getConfig() != null) {
            final CallbackLogic<Object, JEEContext> configCallbackLogic = getConfig().getCallbackLogic();
            if (configCallbackLogic != null) {
                return configCallbackLogic;
            }
        }
        return callbackLogic;
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

    public CallbackLogic<Object, JEEContext> getCallbackLogic() {
        return callbackLogic;
    }

    public void setCallbackLogic(final CallbackLogic<Object, JEEContext> callbackLogic) {
        this.callbackLogic = callbackLogic;
    }

    public String getDefaultClient() {
        return defaultClient;
    }

    public void setDefaultClient(final String defaultClient) {
        this.defaultClient = defaultClient;
    }
}

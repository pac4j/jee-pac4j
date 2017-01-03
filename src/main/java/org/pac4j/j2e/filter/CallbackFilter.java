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
import org.pac4j.core.http.J2ENopHttpActionAdapter;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter finishes the login process for an indirect client, based on the {@link #callbackLogic}.</p>
 *
 * <p>The configuration can be provided via servlet parameters: <code>defaultUrl</code> (default url after login if none was requested),
 * <code>multiProfile</code> (whether multiple profiles should be kept) and <code>renewSession</code> (whether the session must be renewed after login).</p>
 * <p>Or it can be defined via setter methods: {@link #setDefaultUrl(String)}, {@link #setMultiProfile(Boolean)} and ({@link #setRenewSession(Boolean)}.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class CallbackFilter extends AbstractConfigFilter {

    private CallbackLogic<Object, J2EContext> callbackLogic = new DefaultCallbackLogic<>();

    private String defaultUrl;

    private Boolean multiProfile;

    private Boolean renewSession;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.multiProfile = getBooleanParam(filterConfig, Pac4jConstants.MULTI_PROFILE, this.multiProfile);
        this.renewSession = getBooleanParam(filterConfig, Pac4jConstants.RENEW_SESSION, this.renewSession);

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

        callbackLogic.perform(context, config, J2ENopHttpActionAdapter.INSTANCE, this.defaultUrl, this.multiProfile, this.renewSession);
    }

    public String getDefaultUrl() {
        return this.defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
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
}

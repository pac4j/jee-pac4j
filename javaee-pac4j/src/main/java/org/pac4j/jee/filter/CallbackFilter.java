package org.pac4j.jee.filter;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.config.AbstractConfigFilter;
import org.pac4j.jee.context.JEEContextFactory;
import org.pac4j.jee.context.session.JEESessionStoreFactory;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>This filter finishes the login process for an indirect client.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class CallbackFilter extends AbstractConfigFilter {

    private CallbackLogic callbackLogic;

    private String defaultUrl;

    private Boolean renewSession;

    private String defaultClient;

    public CallbackFilter() {}

    public CallbackFilter(final Config config) {
        setSharedConfig(config);
    }

    public CallbackFilter(final Config config, final String defaultUrl) {
        this(config);
        this.defaultUrl = defaultUrl;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.renewSession = getBooleanParam(filterConfig, Pac4jConstants.RENEW_SESSION, this.renewSession);
        this.defaultClient = getStringParam(filterConfig, Pac4jConstants.DEFAULT_CLIENT, this.defaultClient);
    }

    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                           final FilterChain chain) throws IOException, ServletException {

        final Config config = getSharedConfig();

        final HttpActionAdapter bestAdapter = FindBest.httpActionAdapter(null, config, JEEHttpActionAdapter.INSTANCE);
        final CallbackLogic bestLogic = FindBest.callbackLogic(callbackLogic, config, DefaultCallbackLogic.INSTANCE);

        final WebContext context = FindBest.webContextFactory(null, config, JEEContextFactory.INSTANCE).newContext(request, response);
        final SessionStore sessionStore = FindBest.sessionStoreFactory(null, config, JEESessionStoreFactory.INSTANCE).newSessionStore(request, response);

        bestLogic.perform(context, sessionStore, config, bestAdapter, this.defaultUrl, this.renewSession, this.defaultClient);
    }

    public String getDefaultUrl() {
        return this.defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public Boolean getRenewSession() {
        return renewSession;
    }

    public void setRenewSession(final Boolean renewSession) {
        this.renewSession = renewSession;
    }

    public CallbackLogic getCallbackLogic() {
        return callbackLogic;
    }

    public void setCallbackLogic(final CallbackLogic callbackLogic) {
        this.callbackLogic = callbackLogic;
    }

    public String getDefaultClient() {
        return defaultClient;
    }

    public void setDefaultClient(final String defaultClient) {
        this.defaultClient = defaultClient;
    }
}

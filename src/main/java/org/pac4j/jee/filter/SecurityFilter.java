package org.pac4j.jee.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.*;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.util.FindBest;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.util.Pac4JHttpServletRequestWrapper;

/**
 * <p>This filter protects an url.</p>
 *
 * @author Jerome Leleu, Michael Remond
 * @since 1.0.0
 */
public class SecurityFilter extends AbstractConfigFilter {

    private SecurityLogic<Object, JEEContext> securityLogic;

    private String clients;

    private String authorizers;

    private String matchers;

    private Boolean multiProfile;

    public SecurityFilter() {}

    public SecurityFilter(final Config config) {
        setSharedConfig(config);
    }

    public SecurityFilter(final Config config, final String clients) {
        this(config);
        this.clients = clients;
    }

    public SecurityFilter(final Config config, final String clients, final String authorizers) {
        this(config, clients);
        this.authorizers = authorizers;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.clients = getStringParam(filterConfig, Pac4jConstants.CLIENTS, this.clients);
        this.authorizers = getStringParam(filterConfig, Pac4jConstants.AUTHORIZERS, this.authorizers);
        this.matchers = getStringParam(filterConfig, Pac4jConstants.MATCHERS, this.matchers);
        this.multiProfile = getBooleanParam(filterConfig, Pac4jConstants.MULTI_PROFILE, this.multiProfile);
    }

    @Override
    protected final void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                        final FilterChain filterChain) throws IOException, ServletException {

        final Config config = getSharedConfig();

        final SessionStore<JEEContext> bestSessionStore = FindBest.sessionStore(null, config, JEESessionStore.INSTANCE);
        final HttpActionAdapter<Object, JEEContext> bestAdapter = FindBest.httpActionAdapter(null, config, JEEHttpActionAdapter.INSTANCE);
        final SecurityLogic<Object, JEEContext> bestLogic = FindBest.securityLogic(securityLogic, config, DefaultSecurityLogic.INSTANCE);

        final JEEContext context = new JEEContext(request, response, bestSessionStore);
        bestLogic.perform(context, config, (ctx, profiles, parameters) -> {
            // if no profiles are loaded, pac4j is not concerned with this request
            filterChain.doFilter(profiles.isEmpty() ? request : new Pac4JHttpServletRequestWrapper(request, profiles), response);
            return null;
        }, bestAdapter, clients, authorizers, matchers, multiProfile);
    }

    public String getClients() {
        return clients;
    }

    public void setClients(final String clients) {
        this.clients = clients;
    }

    public String getAuthorizers() {
        return authorizers;
    }

    public void setAuthorizers(final String authorizers) {
        this.authorizers = authorizers;
    }

    public String getMatchers() {
        return matchers;
    }

    public void setMatchers(final String matchers) {
        this.matchers = matchers;
    }

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(final Boolean multiProfile) {
        this.multiProfile = multiProfile;
    }

    public SecurityLogic<Object, JEEContext> getSecurityLogic() {
        return securityLogic;
    }

    public void setSecurityLogic(final SecurityLogic<Object, JEEContext> securityLogic) {
        this.securityLogic = securityLogic;
    }
}

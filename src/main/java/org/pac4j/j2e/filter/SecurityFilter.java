package org.pac4j.j2e.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.*;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.http.J2ENopHttpActionAdapter;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter protects an url, based on the {@link #securityLogic}.</p>
 *
 * <p>The configuration can be provided via servlet parameters: <code>configFactory</code> (configuration factory), <code>clients</code> (list of clients for authentication),
 * <code>authorizers</code> (list of authorizers), <code>matchers</code> (list of matchers) and <code>multiProfile</code>  (whether multiple profiles should be kept).</p>
 * <p>Or it can be defined via setter methods: {@link #setConfig(Config)}, {@link #setClients(String)}, {@link #setAuthorizers(String)}, {@link #setMatchers(String)} and {@link #setMultiProfile(Boolean)}.</p>
 *
 * @author Jerome Leleu, Michael Remond
 * @since 1.0.0
 */
public class SecurityFilter extends AbstractConfigFilter {

    private SecurityLogic<Object, J2EContext> securityLogic = new DefaultSecurityLogic<>();

    private String clients;

    private String authorizers;

    private String matchers;

    private Boolean multiProfile;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.clients = getStringParam(filterConfig, Pac4jConstants.CLIENTS, this.clients);
        this.authorizers = getStringParam(filterConfig, Pac4jConstants.AUTHORIZERS, this.authorizers);
        this.matchers = getStringParam(filterConfig, Pac4jConstants.MATCHERS, this.matchers);
        this.multiProfile = getBooleanParam(filterConfig, Pac4jConstants.MULTI_PROFILE, this.multiProfile);

        // check backward incompatibility
        checkForbiddenParameter(filterConfig, "clientsFactory");
        checkForbiddenParameter(filterConfig, "isAjax");
        checkForbiddenParameter(filterConfig, "stateless");
        checkForbiddenParameter(filterConfig, "requireAnyRole");
        checkForbiddenParameter(filterConfig, "requireAllRoles");
        checkForbiddenParameter(filterConfig, "clientName");
        checkForbiddenParameter(filterConfig, "authorizerName");
        checkForbiddenParameter(filterConfig, "matcherName");
    }

    @Override
    protected final void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                        final FilterChain chain) throws IOException, ServletException {

        final Config config = getConfig();
        assertNotNull("config", config);
        final J2EContext context = new J2EContext(request, response, config.getSessionStore());

        securityLogic.perform(context, config, (ctx, parameters) -> {
            final HttpServletRequest req = context.getRequest();
            final HttpServletResponse resp = context.getResponse();
            final FilterChain filterChain = (FilterChain) parameters[0];
            filterChain.doFilter(req, resp);
            return null;
        }, J2ENopHttpActionAdapter.INSTANCE, clients, authorizers, matchers, multiProfile, chain);
    }

    public String getClients() {
        return clients;
    }

    public void setClients(String clients) {
        this.clients = clients;
    }

    public String getAuthorizers() {
        return authorizers;
    }

    public void setAuthorizers(String authorizers) {
        this.authorizers = authorizers;
    }

    public String getMatchers() {
        return matchers;
    }

    public void setMatchers(String matchers) {
        this.matchers = matchers;
    }

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public void setMultiProfile(Boolean multiProfile) {
        this.multiProfile = multiProfile;
    }

    public SecurityLogic<Object, J2EContext> getSecurityLogic() {
        return securityLogic;
    }

    public void setSecurityLogic(SecurityLogic<Object, J2EContext> securityLogic) {
        this.securityLogic = securityLogic;
    }
}

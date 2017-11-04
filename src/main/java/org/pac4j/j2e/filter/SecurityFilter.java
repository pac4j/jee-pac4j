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
import org.pac4j.core.http.adapter.J2ENopHttpActionAdapter;
import org.pac4j.j2e.util.Pac4JHttpServletRequestWrapper;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter protects an url, based on the {@link #securityLogic}.</p>
 *
 * <p>The configuration can be provided via servlet parameters, setters or constructors for the following options:</p>
 * <ul>
 *     <li><code>configFactory</code> (the class name of the factory to build the configuration) or <code>config</code> (the configuration itself)</li>
 *     <li><code>clients</code> (list of clients for authentication)</li>
 *     <li><code>authorizers</code> (list of authorizers)</li>
 *     <li><code>matchers</code> (list of matchers)</li>
 *     <li><code>multiProfile</code>  (whether multiple profiles should be kept).</li>
 * </ul>
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

    public SecurityFilter() {}

    public SecurityFilter(final Config config) {
        setConfig(config);
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
                                        final FilterChain filterChain) throws IOException, ServletException {

        assertNotNull("securityLogic", securityLogic);

        final Config config = getConfig();
        assertNotNull("config", config);
        final J2EContext context = new J2EContext(request, response, config.getSessionStore());

        securityLogic.perform(context, config, (ctx, profiles, parameters) -> {
            // if no profiles are loaded, pac4j is not concerned with this request
            filterChain.doFilter(profiles.isEmpty() ? request : new Pac4JHttpServletRequestWrapper(request, profiles), response);
            return null;
        }, J2ENopHttpActionAdapter.INSTANCE, clients, authorizers, matchers, multiProfile);
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

    public SecurityLogic<Object, J2EContext> getSecurityLogic() {
        return securityLogic;
    }

    public void setSecurityLogic(final SecurityLogic<Object, J2EContext> securityLogic) {
        this.securityLogic = securityLogic;
    }
}

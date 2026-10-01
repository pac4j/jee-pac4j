package org.pac4j.jee.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.core.util.security.SecurityEndpoint;
import org.pac4j.core.util.security.SecurityEndpointBuilder;
import org.pac4j.jee.config.AbstractConfigFilter;
import org.pac4j.jee.context.JEEFrameworkParameters;
import org.pac4j.jee.util.Pac4JHttpServletRequestWrapper;

import java.io.IOException;

/**
 * <p>Protects a URL using the configured authentication clients, authorizers and matchers.</p>
 *
 * @author Jerome Leleu, Michael Remond
 * @since 1.0.0
 */
@Getter
@Setter
public class SecurityFilter extends AbstractConfigFilter implements SecurityEndpoint {

    /**
     * Comma-separated names of authentication clients.
     */
    private String clients;

    /**
     * Comma-separated names of authorizers.
     */
    private String authorizers;

    /**
     * Comma-separated names of matchers.
     */
    private String matchers;

    /**
     * Creates a security filter configured through servlet initialization parameters or setters.
     */
    public SecurityFilter() {}

    /**
     * Creates a security filter using the supplied configuration.
     *
     * @param config the pac4j configuration
     */
    public SecurityFilter(final Config config) {
        setConfig(config);
    }

    /**
     * Creates a security filter with the specified clients.
     *
     * @param config the pac4j configuration
     * @param clients the comma-separated client names
     */
    public SecurityFilter(final Config config, final String clients) {
        this(config);
        this.clients = clients;
    }

    /**
     * Creates a security filter with the specified clients and authorizers.
     *
     * @param config the pac4j configuration
     * @param clients the comma-separated client names
     * @param authorizers the comma-separated authorizer names
     */
    public SecurityFilter(final Config config, final String clients, final String authorizers) {
        this(config, clients);
        this.authorizers = authorizers;
    }

    /**
     * Creates a security filter with the specified clients, authorizers and matchers.
     *
     * @param config the pac4j configuration
     * @param clients the comma-separated client names
     * @param authorizers the comma-separated authorizer names
     * @param matchers the comma-separated matcher names
     */
    public SecurityFilter(final Config config, final String clients, final String authorizers, final String matchers) {
        this(config, clients, authorizers);
        this.matchers = matchers;
    }

    /**
     * Builds a security filter using {@link SecurityEndpointBuilder}.
     * A configuration, client, authorizer or matcher instance can be supplied. When a configuration
     * is supplied, up to three strings select client, authorizer and matcher names, in that order.
     * Client, authorizer and matcher instances, and strings, may also be grouped in collections or object arrays.
     *
     * @param parameters the configuration and security components to apply
     * @return the configured security filter
     * @throws org.pac4j.core.exception.TechnicalException if a parameter type or combination is unsupported
     */
    public static SecurityFilter build(final Object... parameters) {
        final SecurityFilter securityFilter = new SecurityFilter();
        SecurityEndpointBuilder.buildConfig(securityFilter, parameters);
        return securityFilter;
    }

    /** {@inheritDoc} */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.clients = getStringParam(filterConfig, Pac4jConstants.CLIENTS, this.clients);
        this.authorizers = getStringParam(filterConfig, Pac4jConstants.AUTHORIZERS, this.authorizers);
        this.matchers = getStringParam(filterConfig, Pac4jConstants.MATCHERS, this.matchers);
    }

    /** {@inheritDoc} */
    @Override
    protected final void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                        final FilterChain filterChain) throws IOException, ServletException {

        val config = getSharedConfig();

        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

        config.getSecurityLogic().perform(config, (ctx, session, profiles) -> {
            // if no profiles are loaded, pac4j is not concerned with this request
            filterChain.doFilter(profiles.isEmpty() ? request : new Pac4JHttpServletRequestWrapper(request, profiles), response);
            return null;
        }, clients, authorizers, matchers, new JEEFrameworkParameters(request, response));
    }
}

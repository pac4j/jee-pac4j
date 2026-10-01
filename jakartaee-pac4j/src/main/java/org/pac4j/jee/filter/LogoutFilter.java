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
import org.pac4j.jee.config.AbstractConfigFilter;
import org.pac4j.jee.context.JEEFrameworkParameters;

import java.io.IOException;

/**
 * <p>This filter handles the (application + identity provider) logout process.</p>
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
@Getter
@Setter
public class LogoutFilter extends AbstractConfigFilter {

    /**
     * Fallback redirect URL used by the pac4j logic.
     */
    private String defaultUrl;

    /**
     * Regular expression restricting redirect URLs supplied to the logout endpoint.
     */
    private String logoutUrlPattern;

    /**
     * Whether to remove local user profiles; {@code null} uses the pac4j default.
     */
    private Boolean localLogout;

    /**
     * Whether to destroy the local session; {@code null} uses the pac4j default.
     */
    private Boolean destroySession;

    /**
     * Whether to log out at the identity provider; {@code null} uses the pac4j default.
     */
    private Boolean centralLogout;

    /**
     * Creates a logout filter configured through servlet initialization parameters or setters.
     */
    public LogoutFilter() {}

    /**
     * Creates a logout filter using the supplied configuration.
     *
     * @param config the pac4j configuration
     */
    public LogoutFilter(final Config config) {
        setConfig(config);
    }

    /**
     * Creates a logout filter with a fallback redirect URL.
     *
     * @param config the pac4j configuration
     * @param defaultUrl the fallback URL after logout
     */
    public LogoutFilter(final Config config, final String defaultUrl) {
        this(config);
        this.defaultUrl = defaultUrl;
    }

    /** {@inheritDoc} */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.logoutUrlPattern = getStringParam(filterConfig, Pac4jConstants.LOGOUT_URL_PATTERN, this.logoutUrlPattern);
        this.localLogout = getBooleanParam(filterConfig, Pac4jConstants.LOCAL_LOGOUT, this.localLogout);
        this.destroySession = getBooleanParam(filterConfig, Pac4jConstants.DESTROY_SESSION, this.destroySession);
        this.centralLogout = getBooleanParam(filterConfig, Pac4jConstants.CENTRAL_LOGOUT, this.centralLogout);
    }

    /** {@inheritDoc} */
    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                  final FilterChain chain) throws IOException, ServletException {

        val config = getSharedConfig();

        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

        config.getLogoutLogic().perform(config, defaultUrl, logoutUrlPattern, localLogout, destroySession, centralLogout, new JEEFrameworkParameters(request, response));
    }
}

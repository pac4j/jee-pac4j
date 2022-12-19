package org.pac4j.jee.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.config.AbstractConfigFilter;
import org.pac4j.jee.context.JEEFrameworkParameters;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    private String defaultUrl;

    private String logoutUrlPattern;

    private Boolean localLogout;

    private Boolean destroySession;

    private Boolean centralLogout;

    public LogoutFilter() {}

    public LogoutFilter(final Config config) {
        setConfig(config);
    }

    public LogoutFilter(final Config config, final String defaultUrl) {
        this(config);
        this.defaultUrl = defaultUrl;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.logoutUrlPattern = getStringParam(filterConfig, Pac4jConstants.LOGOUT_URL_PATTERN, this.logoutUrlPattern);
        this.localLogout = getBooleanParam(filterConfig, Pac4jConstants.LOCAL_LOGOUT, this.localLogout);
        this.destroySession = getBooleanParam(filterConfig, Pac4jConstants.DESTROY_SESSION, this.destroySession);
        this.centralLogout = getBooleanParam(filterConfig, Pac4jConstants.CENTRAL_LOGOUT, this.centralLogout);
    }

    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                           final FilterChain chain) throws IOException, ServletException {

        val config = getSharedConfig();

        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

        config.getLogoutLogic().perform(config, defaultUrl, logoutUrlPattern, localLogout, destroySession, centralLogout, new JEEFrameworkParameters(request, response));
    }
}

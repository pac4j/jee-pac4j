package org.pac4j.j2e.filter;

import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter handles the application logout process.</p>
 * <p>After logout, the user is redirected to the url defined by the <code>url</code> request parameter. If no url is provided, a blank page is displayed.
 * If the <code>url</code> does not match the <code>logoutUrlPattern</code>, the <code>defaultUrl</code> is used.</p>
 *
 * <p>The configuration can be provided via servlet parameters: <code>defaultUrl</code> (default logourl url) and <code>logoutUrlPattern</code> (logout url pattern).</p>
 * <p>Or it can be defined via setter methods: {@link #setDefaultUrl(String)} and {@link #setLogoutUrlPattern(String)}.</p>
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class ApplicationLogoutFilter extends AbstractConfigFilter {

    protected String defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;

    protected String logoutUrlPattern = Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        assertNotBlank(Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.logoutUrlPattern = getStringParam(filterConfig, Pac4jConstants.LOGOUT_URL_PATTERN, this.logoutUrlPattern);
        assertNotBlank(Pac4jConstants.LOGOUT_URL_PATTERN, this.logoutUrlPattern);
    }

    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                           final FilterChain chain) throws IOException, ServletException {

        final Config config = ConfigSingleton.getConfig();
        assertNotNull("config", config);
        final WebContext context = new J2EContext(request, response, config.getSessionStore());
        final ProfileManager manager = new ProfileManager(context);
        manager.logout();

        final String url = context.getRequestParameter(Pac4jConstants.URL);
        if (url != null) {
            if (Pattern.matches(this.logoutUrlPattern, url)) {
                response.sendRedirect(url);
            } else {
                response.sendRedirect(this.defaultUrl);
            }
        }
    }

    public String getDefaultUrl() {
        return this.defaultUrl;
    }

    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public String getLogoutUrlPattern() {
        return logoutUrlPattern;
    }

    public void setLogoutUrlPattern(String logoutUrlPattern) {
        this.logoutUrlPattern = logoutUrlPattern;
    }
}

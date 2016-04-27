package org.pac4j.j2e.filter;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.ApplicationLogoutLogic;
import org.pac4j.core.engine.DefaultApplicationLogoutLogic;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter handles the application logout process, based on the {@link #applicationLogoutLogic}.</p>
 *
 * <p>The configuration can be provided via servlet parameters: <code>defaultUrl</code> (default logourl url) and <code>logoutUrlPattern</code> (logout url pattern).</p>
 * <p>Or it can be defined via setter methods: {@link #setDefaultUrl(String)} and {@link #setLogoutUrlPattern(String)}.</p>
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class ApplicationLogoutFilter extends AbstractConfigFilter {

    private ApplicationLogoutLogic<Object> applicationLogoutLogic = new DefaultApplicationLogoutLogic<>();

    private String defaultUrl;

    private String logoutUrlPattern;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.logoutUrlPattern = getStringParam(filterConfig, Pac4jConstants.LOGOUT_URL_PATTERN, this.logoutUrlPattern);
    }

    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                           final FilterChain chain) throws IOException, ServletException {

        final Config config = getConfig();
        assertNotNull("config", config);
        final WebContext context = new J2EContext(request, response, config.getSessionStore());

        applicationLogoutLogic.perform(context, config, (code, ctx) -> null, this.defaultUrl, this.logoutUrlPattern);
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

    public ApplicationLogoutLogic<Object> getApplicationLogoutLogic() {
        return applicationLogoutLogic;
    }

    public void setApplicationLogoutLogic(ApplicationLogoutLogic<Object> applicationLogoutLogic) {
        this.applicationLogoutLogic = applicationLogoutLogic;
    }
}

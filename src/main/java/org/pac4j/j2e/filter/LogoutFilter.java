package org.pac4j.j2e.filter;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.http.J2ENopHttpActionAdapter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * <p>This filter handles the (application + identity provider) logout process, based on the {@link #logoutLogic}.</p>
 *
 * <p>The configuration can be provided via servlet parameters: <code>defaultUrl</code> (default logourl url),<code>logoutUrlPattern</code> (pattern that logout urls must match),
 * <code>localLogout</code> (whether the application logout must be performed) and <code>centralLogout</code> (whether the centralLogout must be performed).</p>
 * <p>Or it can be defined via setter methods: {@link #setDefaultUrl(String)}, {@link #setLogoutUrlPattern(String)}, {@link #setLocalLogout(Boolean)} and
 * {@link #setCentralLogout(Boolean)}.</p>
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class LogoutFilter extends AbstractConfigFilter {

    private LogoutLogic<Object, J2EContext> logoutLogic = new DefaultLogoutLogic<>();

    private String defaultUrl;

    private String logoutUrlPattern;

    private Boolean localLogout;

    private Boolean centralLogout;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.logoutUrlPattern = getStringParam(filterConfig, Pac4jConstants.LOGOUT_URL_PATTERN, this.logoutUrlPattern);
        this.localLogout = getBooleanParam(filterConfig, Pac4jConstants.LOCAL_LOGOUT, this.localLogout);
        this.centralLogout = getBooleanParam(filterConfig, Pac4jConstants.CENTRAL_LOGOUT, this.centralLogout);
    }

    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                           final FilterChain chain) throws IOException, ServletException {

        assertNotNull("applicationLogoutLogic", logoutLogic);

        final Config config = getConfig();
        assertNotNull("config", config);
        final J2EContext context = new J2EContext(request, response, config.getSessionStore());

        logoutLogic.perform(context, config, J2ENopHttpActionAdapter.INSTANCE, this.defaultUrl, this.logoutUrlPattern, this.localLogout, this.centralLogout);
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

    public LogoutLogic<Object, J2EContext> getLogoutLogic() {
        return logoutLogic;
    }

    public void setLogoutLogic(final LogoutLogic<Object, J2EContext> logoutLogic) {
        this.logoutLogic = logoutLogic;
    }

    public Boolean getLocalLogout() {
        return localLogout;
    }

    public void setLocalLogout(final Boolean localLogout) {
        this.localLogout = localLogout;
    }

    public Boolean getCentralLogout() {
        return centralLogout;
    }

    public void setCentralLogout(final Boolean centralLogout) {
        this.centralLogout = centralLogout;
    }
}

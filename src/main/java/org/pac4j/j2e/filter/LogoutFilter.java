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
 * <p>The configuration can be provided via servlet parameters, setters or constructors for the following options:</p>
 * <ul>
 *     <li><code>configFactory</code> (the class name of the factory to build the configuration) or <code>config</code> (the configuration itself)</li>
 *     <li><code>defaultUrl</code> (default logourl url)</li>
 *     <li><code>logoutUrlPattern</code> (pattern that logout urls must match)</li>
 *     <li><code>localLogout</code> (whether the application logout must be performed)</li>
 *     <li><code>destroySession</code> (whether we must destroy the web session during the local logout)</li>
 *     <li><code>centralLogout</code> (whether the centralLogout must be performed)</li>
 * </ul>
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class LogoutFilter extends AbstractConfigFilter {

    private LogoutLogic<Object, J2EContext> logoutLogic = new DefaultLogoutLogic<>();

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

        assertNotNull("applicationLogoutLogic", logoutLogic);

        final Config config = getConfig();
        assertNotNull("config", config);
        final J2EContext context = new J2EContext(request, response, config.getSessionStore());

        logoutLogic.perform(context, config, J2ENopHttpActionAdapter.INSTANCE, this.defaultUrl, this.logoutUrlPattern, this.localLogout, this.destroySession, this.centralLogout);
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

    public Boolean getDestroySession() {
        return destroySession;
    }

    public void setDestroySession(final Boolean destroySession) {
        this.destroySession = destroySession;
    }

    public Boolean getCentralLogout() {
        return centralLogout;
    }

    public void setCentralLogout(final Boolean centralLogout) {
        this.centralLogout = centralLogout;
    }
}

package org.pac4j.jee.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.pac4j.core.config.Config;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.config.AbstractConfigFilter;
import org.pac4j.jee.config.Pac4jJEEConfig;
import org.pac4j.jee.context.JEEFrameworkParameters;

import java.io.IOException;

/**
 * <p>This filter finishes the login process for an indirect client.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Getter
@Setter
public class CallbackFilter extends AbstractConfigFilter {

    private String defaultUrl;

    private Boolean renewSession;

    private String defaultClient;

    public CallbackFilter() {}

    public CallbackFilter(final Config config) {
        setConfig(config);
    }

    public CallbackFilter(final Config config, final String defaultUrl) {
        this(config);
        this.defaultUrl = defaultUrl;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.renewSession = getBooleanParam(filterConfig, Pac4jConstants.RENEW_SESSION, this.renewSession);
        this.defaultClient = getStringParam(filterConfig, Pac4jConstants.DEFAULT_CLIENT, this.defaultClient);
    }

    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                  final FilterChain chain) throws IOException, ServletException {

        val config = getSharedConfig();

        Pac4jJEEConfig.applyJEESettingsIfUndefined(config);

        config.getCallbackLogic().perform(config, defaultUrl, renewSession, defaultClient, new JEEFrameworkParameters(request, response));
    }
}

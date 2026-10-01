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
 * <p>This filter finishes the login process for an indirect client.</p>
 *
 * @author Jerome Leleu
 * @since 1.0.0
 */
@Getter
@Setter
public class CallbackFilter extends AbstractConfigFilter {

    /**
     * Fallback redirect URL used by the pac4j logic.
     */
    private String defaultUrl;

    /**
     * Whether the callback logic renews the session after login; {@code null} uses the pac4j default.
     */
    private Boolean renewSession;

    /**
     * Client name used when the callback does not identify a client.
     */
    private String defaultClient;

    /**
     * Creates a callback filter configured through servlet initialization parameters or setters.
     */
    public CallbackFilter() {}

    /**
     * Creates a callback filter using the supplied configuration.
     *
     * @param config the pac4j configuration
     */
    public CallbackFilter(final Config config) {
        setConfig(config);
    }

    /**
     * Creates a callback filter with a fallback redirect URL.
     *
     * @param config the pac4j configuration
     * @param defaultUrl the fallback URL after a successful login
     */
    public CallbackFilter(final Config config, final String defaultUrl) {
        this(config);
        this.defaultUrl = defaultUrl;
    }

    /** {@inheritDoc} */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.defaultUrl = getStringParam(filterConfig, Pac4jConstants.DEFAULT_URL, this.defaultUrl);
        this.renewSession = getBooleanParam(filterConfig, Pac4jConstants.RENEW_SESSION, this.renewSession);
        this.defaultClient = getStringParam(filterConfig, Pac4jConstants.DEFAULT_CLIENT, this.defaultClient);
    }

    /** {@inheritDoc} */
    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                  final FilterChain chain) throws IOException, ServletException {

        val config = getSharedConfig();

        FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

        config.getCallbackLogic().perform(config, defaultUrl, renewSession, defaultClient, new JEEFrameworkParameters(request, response));
    }
}

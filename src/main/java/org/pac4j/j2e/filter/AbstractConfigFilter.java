package org.pac4j.j2e.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigBuilder;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract filter which handles configuration.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public abstract class AbstractConfigFilter implements Filter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected Config config;

    public void init(final FilterConfig filterConfig) throws ServletException {
        final String configFactoryParam = filterConfig.getInitParameter(Pac4jConstants.CONFIG_FACTORY);
        if (configFactoryParam != null) {
            final Config config = ConfigBuilder.build(configFactoryParam);
            setConfig(config);
        }
    }

    protected String getStringParam(final FilterConfig filterConfig, final String name, final String defaultValue) {
        final String param = filterConfig.getInitParameter(name);
        final String value;
        if (param != null) {
            value = param;
        } else {
            value = defaultValue;
        }
        logger.debug("String param: {}: {}", name, value);
        return value;
    }

    protected boolean getBooleanParam(final FilterConfig filterConfig, final String name, final boolean defaultValue) {
        final String param = filterConfig.getInitParameter(name);
        final boolean value;
        if (param != null) {
            value = Boolean.parseBoolean(param);
        } else {
            value = defaultValue;
        }
        logger.debug("Boolean param: {}: {}", name, value);
        return value;
    }

    protected void checkForbiddenParameter(final FilterConfig filterConfig, final String name) {
        final String parameter = getStringParam(filterConfig, name, null);
        if (CommonHelper.isNotBlank(parameter)) {
            final String message = "the " + name + " servlet parameter is no longer supported";
            logger.error(message);
            throw new TechnicalException(message);
        }
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse resp = (HttpServletResponse) response;

        internalFilter(req, resp, chain);
    }

    protected abstract void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException;

    public void destroy() {}

    public Config getConfig() {
        if (this.config == null) {
            return ConfigSingleton.getConfig();
        }
        return this.config;
    }

    public void setConfig(final Config config) {
        CommonHelper.assertNotNull("config", config);
        this.config = config;
        ConfigSingleton.setConfig(config);
    }
}

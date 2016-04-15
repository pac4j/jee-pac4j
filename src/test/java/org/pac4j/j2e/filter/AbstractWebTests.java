package org.pac4j.j2e.filter;

import org.junit.Before;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.util.TestsConstants;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Utility class for tests.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public abstract class AbstractWebTests implements TestsConstants {

    protected MockFilterConfig filterConfig;

    protected Config config;

    protected MockHttpServletRequest request;

    protected MockHttpServletResponse response;

    protected MockFilterChain filterChain;

    protected WebContext webContext;

    @Before
    public void setUp() {
        filterConfig = new MockFilterConfig();
        config = new Config();
        ConfigSingleton.setConfig(config);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        webContext = new J2EContext(request, response);
    }
}

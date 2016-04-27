package org.pac4j.j2e.filter;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.TestsConstants;
import org.pac4j.core.util.TestsHelper;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Utility class for tests.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public abstract class AbstractWebTests implements TestsConstants {

    protected AbstractConfigFilter filter;

    protected MockFilterConfig filterConfig;

    protected Config config;

    protected MockHttpServletRequest request;

    protected MockHttpServletResponse response;

    protected MockFilterChain filterChain;

    @Before
    public void setUp() {
        filterConfig = new MockFilterConfig();
        config = new Config();
        ConfigSingleton.setConfig(config);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    protected void call() throws Exception {
        filter.init(filterConfig);
        filter.internalFilter(request, response, filterChain);
    }

    @Test
    public void testMissingConfig() throws Exception {
        ConfigSingleton.setConfig(null);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "config cannot be null");
    }

    @Test
    public void setGetConfig() {
        ConfigSingleton.setConfig(null);
        assertNull(ConfigSingleton.getConfig());
        assertNull(filter.getConfig());
        final Config config2 = new Config();
        ConfigSingleton.setConfig(config2);
        assertEquals(config2, filter.getConfig());
        final Config config3 = new Config();
        filter.setConfig(config3);
        assertEquals(config3, filter.getConfig());
        assertEquals(config3, ConfigSingleton.getConfig());
    }
}

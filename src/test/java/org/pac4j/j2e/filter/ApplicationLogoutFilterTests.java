package org.pac4j.j2e.filter;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.TestsConstants;
import org.pac4j.core.util.TestsHelper;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * Tests {@link ApplicationLogoutFilter}.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public final class ApplicationLogoutFilterTests implements TestsConstants {

    private final static String POST_LOGOUT_URL = "/postLogoutUrl";
    private final static String NEW_DEFAULT_URL = "/newDefaultUrl";

    private ApplicationLogoutFilter filter;

    private MockFilterConfig filterConfig;

    private Config config;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private MockFilterChain filterChain;

    @Before
    public void setUp() {
        filter = new ApplicationLogoutFilter();
        filterConfig = new MockFilterConfig();
        config = new Config();
        ConfigSingleton.setConfig(config);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    private void call() throws Exception {
        filter.init(filterConfig);
        filter.internalFilter(request, response, filterChain);
    }

    @Test
    public void testMissingConfig() throws Exception {
        ConfigSingleton.setConfig(null);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "config cannot be null");
    }

    @Test
    public void testBlankDefaultUrl() throws Exception {
        filter.setDefaultUrl("");
        TestsHelper.expectException(() -> filter.init(filterConfig), TechnicalException.class, "defaultUrl cannot be blank");
    }

    @Test
    public void testBlankLogoutUrlPattern() throws Exception {
        filter.setLogoutUrlPattern("");
        TestsHelper.expectException(() -> filter.init(filterConfig), TechnicalException.class, "logoutUrlPattern cannot be blank");
    }

    @Test
    public void testLogout() throws Exception {
        final LinkedHashMap<String, UserProfile> profiles = new LinkedHashMap<>();
        profiles.put(NAME, new CommonProfile());
        request.setAttribute(Pac4jConstants.USER_PROFILES, profiles);
        request.getSession().setAttribute(Pac4jConstants.USER_PROFILES, profiles);
        call();
        assertEquals(200, response.getStatus());
        assertEquals("", response.getContentAsString());
        final LinkedHashMap<String, UserProfile> profiles2 = (LinkedHashMap<String, UserProfile>) request.getAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(0, profiles2.size());
        final LinkedHashMap<String, UserProfile> profiles3 = (LinkedHashMap<String, UserProfile>) request.getSession().getAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(0, profiles3.size());
    }

    @Test
    public void testLogoutWithUrl() throws Exception {
        request.addParameter(Pac4jConstants.URL, POST_LOGOUT_URL);
        call();
        assertEquals(302, response.getStatus());
        assertEquals(POST_LOGOUT_URL, response.getRedirectedUrl());
    }

    @Test
    public void testLogoutWithBadUrl() throws Exception {
        request.addParameter(Pac4jConstants.URL, POST_LOGOUT_URL);
        filterConfig.addInitParameter(Pac4jConstants.LOGOUT_URL_PATTERN, VALUE);
        filter.init(filterConfig);
        call();
        assertEquals(302, response.getStatus());
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, response.getRedirectedUrl());
    }

    @Test
    public void testLogoutWithBadUrlAndSpecificDefaultUrl() throws Exception {
        request.addParameter(Pac4jConstants.URL, POST_LOGOUT_URL);
        filterConfig.addInitParameter(Pac4jConstants.LOGOUT_URL_PATTERN, VALUE);
        filterConfig.addInitParameter(Pac4jConstants.DEFAULT_URL, NEW_DEFAULT_URL);
        filter.init(filterConfig);
        call();
        assertEquals(302, response.getStatus());
        assertEquals(NEW_DEFAULT_URL, response.getRedirectedUrl());
    }
}

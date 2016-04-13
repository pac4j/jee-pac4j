package org.pac4j.j2e.filter;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.TestsHelper;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * Tests {@link ApplicationLogoutFilter}.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public final class ApplicationLogoutFilterTests extends AbstractWebTests {

    private ApplicationLogoutFilter filter;

    @Before
    public void setUp() {
        filter = new ApplicationLogoutFilter();
        super.setUp();
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
    public void testBlankLogoutUrlPattern() throws Exception {
        filter.setLogoutUrlPattern("");
        TestsHelper.expectException(() -> filter.init(filterConfig), TechnicalException.class, "logoutUrlPattern cannot be blank");
    }

    @Test
    public void testLogout() throws Exception {
        final LinkedHashMap<String, CommonProfile> profiles = new LinkedHashMap<>();
        profiles.put(NAME, new CommonProfile());
        webContext.setRequestAttribute(Pac4jConstants.USER_PROFILES, profiles);
        webContext.setSessionAttribute(Pac4jConstants.USER_PROFILES, profiles);
        call();
        assertEquals(200, response.getStatus());
        assertEquals("", response.getContentAsString());
        final LinkedHashMap<String, CommonProfile> profiles2 = (LinkedHashMap<String, CommonProfile>) webContext.getRequestAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(0, profiles2.size());
        final LinkedHashMap<String, CommonProfile> profiles3 = (LinkedHashMap<String, CommonProfile>) webContext.getSessionAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(0, profiles3.size());
    }

    @Test
    public void testLogoutWithDefaultUrl() throws Exception {
        filter.setDefaultUrl(CALLBACK_URL);
        call();
        assertEquals(302, response.getStatus());
        assertEquals(CALLBACK_URL, response.getRedirectedUrl());
    }

    @Test
    public void testLogoutWithGoodUrl() throws Exception {
        request.addParameter(Pac4jConstants.URL, PATH);
        call();
        assertEquals(302, response.getStatus());
        assertEquals(PATH, response.getRedirectedUrl());
    }

    @Test
    public void testLogoutWithBadUrlNoDefaultUrl() throws Exception {
        request.addParameter(Pac4jConstants.URL, PATH);
        filterConfig.addInitParameter(Pac4jConstants.LOGOUT_URL_PATTERN, VALUE);
        call();
        assertEquals(200, response.getStatus());
        assertEquals("", response.getContentAsString());
    }

    @Test
    public void testLogoutWithBadUrlButDefaultUrl() throws Exception {
        request.addParameter(Pac4jConstants.URL, PATH);
        filterConfig.addInitParameter(Pac4jConstants.LOGOUT_URL_PATTERN, VALUE);
        filterConfig.addInitParameter(Pac4jConstants.DEFAULT_URL, CALLBACK_URL);
        call();
        assertEquals(302, response.getStatus());
        assertEquals(CALLBACK_URL, response.getRedirectedUrl());
    }
}

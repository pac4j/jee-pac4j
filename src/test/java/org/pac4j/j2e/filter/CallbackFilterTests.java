package org.pac4j.j2e.filter;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.TestsConstants;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * Tests {@link CallbackFilter}.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public final class CallbackFilterTests implements TestsConstants {

    private CallbackFilter filter;

    private MockFilterConfig filterConfig;

    private Config config;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private MockFilterChain filterChain;

    @Before
    public void setUp() {
        filter = new CallbackFilter();
        filterConfig = new MockFilterConfig();
        config = new Config();
        ConfigSingleton.setConfig(config);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    private void call() throws Exception {
        filter.internalFilter(request, response, filterChain);
    }

    @Test
    public void testCallback() throws Exception {
        final String originalSessionId = request.getSession().getId();
        request.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, NAME);
        final CommonProfile profile = new CommonProfile();
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), profile);
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        call();
        final String newSessionId = request.getSession().getId();
        final LinkedHashMap<String, UserProfile> profiles = (LinkedHashMap<String, UserProfile>) request.getSession().getAttribute(Pac4jConstants.USER_PROFILES);
        assertTrue(profiles.containsValue(profile));
        assertNotEquals(newSessionId, originalSessionId);
        assertEquals(302, response.getStatus());
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, response.getRedirectedUrl());
    }
}

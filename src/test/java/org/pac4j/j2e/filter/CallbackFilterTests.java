package org.pac4j.j2e.filter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.TestsConstants;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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

    private SessionStore sessionStore;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private MockFilterChain filterChain;

    @Before
    public void setUp() {
        filter = new CallbackFilter();
        filterConfig = new MockFilterConfig();
        config = new Config();
        sessionStore = new MockSessionStore();
        config.setSessionStore(sessionStore);
        ConfigSingleton.setConfig(config);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    private void call() throws Exception {
        filter.internalFilter(request, response, filterChain);
    }

    @Test
    @Ignore
    public void testCallback() throws Exception {
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, null, new CommonProfile());
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        call();
    }
}

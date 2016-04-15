package org.pac4j.j2e.filter;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.client.MockDirectClient;
import org.pac4j.core.client.MockIndirectClient;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.MockCredentials;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.TestsHelper;

import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * Tests {@link CallbackFilter}.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public final class CallbackFilterTests extends AbstractWebTests {

    private CallbackFilter filter;

    @Before
    public void setUp() {
        filter = new CallbackFilter();
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
    public void testMissingClients() throws Exception {
        config.setClients(null);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "clients cannot be null");
    }

    @Test
    public void testBlankDefaultUrl() throws Exception {
        filter.setDefaultUrl("");
        TestsHelper.expectException(() -> call(), TechnicalException.class, "defaultUrl cannot be blank");
    }

    @Test
    public void testOldClientsFactory() throws Exception {
        filterConfig.addInitParameter("clientsFactory", VALUE);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "the clientsFactory servlet parameter is no longer supported");
    }

    @Test
    public void testDirectClient() throws Exception {
        request.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, NAME);
        final MockDirectClient directClient = new MockDirectClient(NAME, new MockCredentials(), new CommonProfile());
        config.setClients(new Clients(directClient));
        TestsHelper.expectException(() -> call(), TechnicalException.class, "only indirect clients are allowed on the callback url");
    }

    @Test
    public void testCallback() throws Exception {
        final String originalSessionId = request.getSession().getId();
        request.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, NAME);
        final CommonProfile profile = new CommonProfile();
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), profile);
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        call();
        final HttpSession session = request.getSession();
        final String newSessionId = session.getId();
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) session.getAttribute(Pac4jConstants.USER_PROFILES);
        assertTrue(profiles.containsValue(profile));
        assertEquals(1, profiles.size());
        assertNotEquals(newSessionId, originalSessionId);
        assertEquals(302, response.getStatus());
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, response.getRedirectedUrl());
    }

    @Test
    public void testCallbackWithOriginallyRequestedUrl() throws Exception {
        HttpSession session = request.getSession();
        final String originalSessionId = session.getId();
        session.setAttribute(Pac4jConstants.REQUESTED_URL, PAC4J_URL);
        request.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, NAME);
        final CommonProfile profile = new CommonProfile();
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), profile);
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        call();
        session = request.getSession();
        final String newSessionId = session.getId();
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) session.getAttribute(Pac4jConstants.USER_PROFILES);
        assertTrue(profiles.containsValue(profile));
        assertEquals(1, profiles.size());
        assertNotEquals(newSessionId, originalSessionId);
        assertEquals(302, response.getStatus());
        assertEquals(PAC4J_URL, response.getRedirectedUrl());
    }

    @Test
    public void testCallbackNoRenew() throws Exception {
        final String originalSessionId = request.getSession().getId();
        request.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, NAME);
        final CommonProfile profile = new CommonProfile();
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), profile);
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        filter.setRenewSession(false);
        call();
        final HttpSession session = request.getSession();
        final String newSessionId = session.getId();
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) session.getAttribute(Pac4jConstants.USER_PROFILES);
        assertTrue(profiles.containsValue(profile));
        assertEquals(1, profiles.size());
        assertEquals(newSessionId, originalSessionId);
        assertEquals(302, response.getStatus());
        assertEquals(Pac4jConstants.DEFAULT_URL_VALUE, response.getRedirectedUrl());
    }
}

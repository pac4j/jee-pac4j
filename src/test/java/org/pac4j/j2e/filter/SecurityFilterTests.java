package org.pac4j.j2e.filter;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.client.*;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.MockCredentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.TestsHelper;

import javax.servlet.FilterChain;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

/**
 * Tests {@link SecurityFilter}.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public final class SecurityFilterTests extends AbstractWebTests {

    private FilterChain filterChain;

    private SecurityFilter filter;

    @Before
    public void setUp() {
        filter = new SecurityFilter();
        super.setUp();
        filterChain = mock(FilterChain.class);
    }

    private void call() throws Exception {
        filter.init(filterConfig);
        filter.internalFilter(request, response, filterChain);
    }

    @Test
    public void testOldClientsFactory() throws Exception {
        filterConfig.addInitParameter("clientsFactory", VALUE);
        TestsHelper.expectException(() -> filter.init(filterConfig), TechnicalException.class, "the clientsFactory servlet parameter is no longer supported");
    }

    @Test
    public void testMissingConfig() throws Exception {
        ConfigSingleton.setConfig(null);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "config cannot be null");
    }

    @Test
    public void testMissingClients() throws Exception {
        config.setClients(null);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "configClients cannot be null");
    }

    @Test
    public void testNotAuthenticated() throws Exception {
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), new CommonProfile());
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        call();
        assertEquals(401, response.getStatus());
    }

    @Test
    public void testNotAuthenticatedButMatcher() throws Exception {
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), new CommonProfile());
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        config.addMatcher(NAME, context -> false);
        filter.setMatchers(NAME);
        call();
        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testAlreadyAuthenticatedAndAuthorized() throws Exception {
        final CommonProfile profile = new CommonProfile();
        profile.setId(ID);
        final LinkedHashMap<String, CommonProfile> profiles = new LinkedHashMap<>();
        profiles.put(NAME, profile);
        webContext.setSessionAttribute(Pac4jConstants.USER_PROFILES, profiles);
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), new CommonProfile());
        filter.setAuthorizers(NAME);
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        config.addAuthorizer(NAME, (context, prof) -> ID.equals(((CommonProfile) prof.get(0)).getId()));
        call();
        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testAlreadyAuthenticatedNotAuthorized() throws Exception {
        final CommonProfile profile = new CommonProfile();
        final LinkedHashMap<String, CommonProfile> profiles = new LinkedHashMap<>();
        profiles.put(NAME, profile);
        webContext.setSessionAttribute(Pac4jConstants.USER_PROFILES, profiles);
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), new CommonProfile());
        filter.setAuthorizers(NAME);
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        config.addAuthorizer(NAME, (context, prof) -> ID.equals(((CommonProfile) prof.get(0)).getId()));
        call();
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testAuthorizerThrowsRequiresHttpAction() throws Exception {
        final CommonProfile profile = new CommonProfile();
        final LinkedHashMap<String, CommonProfile> profiles = new LinkedHashMap<>();
        profiles.put(NAME, profile);
        webContext.setSessionAttribute(Pac4jConstants.USER_PROFILES, profiles);
        final IndirectClient indirectClient = new MockIndirectClient(NAME, null, new MockCredentials(), new CommonProfile());
        filter.setAuthorizers(NAME);
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        config.addAuthorizer(NAME, (context, prof) -> { throw RequiresHttpAction.status("bad request", 400, new J2EContext(request, response)); } );
        call();
        assertEquals(400, response.getStatus());
        verify(filterChain, times(0)).doFilter(request, response);
    }

    @Test
    public void testDoubleDirectClient() throws Exception {
        final CommonProfile profile = new CommonProfile();
        profile.setId(NAME);
        final CommonProfile profile2 = new CommonProfile();
        profile2.setId(VALUE);
        final DirectClient directClient = new MockDirectClient(NAME, new MockCredentials(), profile);
        final DirectClient directClient2 = new MockDirectClient(VALUE, new MockCredentials(), profile2);
        config.setClients(new Clients(CALLBACK_URL, directClient, directClient2));
        filter.setClients(NAME + "," + VALUE);
        call();
        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) webContext.getRequestAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(1, profiles.size());
        assertTrue(profiles.containsValue(profile));
    }

    @Test
    public void testDirectClientThrowsRequiresHttpAction() throws Exception {
        final CommonProfile profile = new CommonProfile();
        profile.setId(NAME);
        final DirectClient directClient = new MockDirectClient(NAME, () -> { throw RequiresHttpAction.status("bad request", 400, new J2EContext(request, response)); }, profile);
        config.setClients(new Clients(CALLBACK_URL, directClient));
        filter.setClients(NAME);
        call();
        assertEquals(400, response.getStatus());
        verify(filterChain, times(0)).doFilter(request, response);
    }

    @Test
    public void testDoubleDirectClientSupportingMultiProfile() throws Exception {
        final CommonProfile profile = new CommonProfile();
        profile.setId(NAME);
        final CommonProfile profile2 = new CommonProfile();
        profile2.setId(VALUE);
        final DirectClient directClient = new MockDirectClient(NAME, new MockCredentials(), profile);
        final DirectClient directClient2 = new MockDirectClient(VALUE, new MockCredentials(), profile2);
        config.setClients(new Clients(CALLBACK_URL, directClient, directClient2));
        filter.setClients(NAME + "," + VALUE);
        filterConfig.addInitParameter(Pac4jConstants.MULTI_PROFILE, Boolean.TRUE.toString());
        call();
        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) webContext.getRequestAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(2, profiles.size());
        assertTrue(profiles.containsValue(profile));
        assertTrue(profiles.containsValue(profile2));
    }

    @Test
    public void testDoubleDirectClientChooseDirectClient() throws Exception {
        final CommonProfile profile = new CommonProfile();
        profile.setId(NAME);
        final CommonProfile profile2 = new CommonProfile();
        profile2.setId(VALUE);
        final DirectClient directClient = new MockDirectClient(NAME, new MockCredentials(), profile);
        final DirectClient directClient2 = new MockDirectClient(VALUE, new MockCredentials(), profile2);
        config.setClients(new Clients(CALLBACK_URL, directClient, directClient2));
        filter.setClients(NAME + "," + VALUE);
        request.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, VALUE);
        filterConfig.addInitParameter(Pac4jConstants.MULTI_PROFILE, Boolean.TRUE.toString());
        call();
        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) webContext.getRequestAttribute(Pac4jConstants.USER_PROFILES);
        assertEquals(1, profiles.size());
        assertTrue(profiles.containsValue(profile2));
    }

    @Test
    public void testDoubleDirectClientChooseBadDirectClient() {
        final CommonProfile profile = new CommonProfile();
        profile.setId(NAME);
        final CommonProfile profile2 = new CommonProfile();
        profile2.setId(VALUE);
        final DirectClient directClient = new MockDirectClient(NAME, new MockCredentials(), profile);
        final DirectClient directClient2 = new MockDirectClient(VALUE, new MockCredentials(), profile2);
        config.setClients(new Clients(CALLBACK_URL, directClient, directClient2));
        filter.setClients(NAME);
        request.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, VALUE);
        filterConfig.addInitParameter(Pac4jConstants.MULTI_PROFILE, Boolean.TRUE.toString());
        TestsHelper.expectException(() -> call(), TechnicalException.class, "Client not allowed: " + VALUE);
    }

    @Test
    public void testRedirectByIndirectClient() throws Exception {
        final IndirectClient indirectClient = new MockIndirectClient(NAME, RedirectAction.redirect(PAC4J_URL), new MockCredentials(), new CommonProfile());
        config.setClients(new Clients(CALLBACK_URL, indirectClient));
        filter.setClients(NAME);
        call();
        assertEquals(302, response.getStatus());
        assertEquals(PAC4J_URL, response.getRedirectedUrl());
    }

    @Test
    public void testDoubleIndirectClientOneChosen() throws Exception {
        final IndirectClient indirectClient = new MockIndirectClient(NAME, RedirectAction.redirect(PAC4J_URL), new MockCredentials(), new CommonProfile());
        final IndirectClient indirectClient2 = new MockIndirectClient(VALUE, RedirectAction.redirect(PAC4J_BASE_URL), new MockCredentials(), new CommonProfile());
        config.setClients(new Clients(CALLBACK_URL, indirectClient, indirectClient2));
        filter.setClients(NAME + "," + VALUE);
        request.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, VALUE);
        call();
        assertEquals(302, response.getStatus());
        assertEquals(PAC4J_BASE_URL, response.getRedirectedUrl());
    }

    @Test
    public void testDoubleIndirectClientBadOneChosen() throws Exception {
        final IndirectClient indirectClient = new MockIndirectClient(NAME, RedirectAction.redirect(PAC4J_URL), new MockCredentials(), new CommonProfile());
        final IndirectClient indirectClient2 = new MockIndirectClient(VALUE, RedirectAction.redirect(PAC4J_BASE_URL), new MockCredentials(), new CommonProfile());
        config.setClients(new Clients(CALLBACK_URL, indirectClient, indirectClient2));
        filter.setClients(NAME);
        request.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, VALUE);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "Client not allowed: " + VALUE);
    }
}

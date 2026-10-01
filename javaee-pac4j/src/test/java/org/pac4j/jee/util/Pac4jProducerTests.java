package org.pac4j.jee.util;

import javax.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.Pac4jConstants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Pac4jProducerTests {

    @SuppressWarnings("unchecked")
    private Instance<Config> configInstance(final Config config) {
        final Instance<Config> instance = mock(Instance.class);
        when(instance.isResolvable()).thenReturn(true);
        when(instance.get()).thenReturn(config);
        return instance;
    }

    @Test
    void producedManagerRenewsExpiredProfiles() {
        final BaseClient client = mock(BaseClient.class);
        when(client.getName()).thenReturn("renewable");
        final Config config = new Config(client);
        final WebContext context = mock(WebContext.class);
        final SessionStore session = mock(SessionStore.class);
        final UserProfile expired = mock(CommonProfile.class);
        when(expired.isExpired()).thenReturn(true);
        when(expired.getClientName()).thenReturn("renewable");
        final UserProfile renewed = new CommonProfile();
        final LinkedHashMap<String, UserProfile> profiles = new LinkedHashMap<>();
        profiles.put("renewable", expired);
        when(context.getRequestAttribute(Pac4jConstants.USER_PROFILES)).thenReturn(Optional.of(profiles));
        when(session.get(context, Pac4jConstants.USER_PROFILES)).thenReturn(Optional.empty());
        when(client.renewUserProfile(any(CallContext.class), same(expired))).thenReturn(Optional.of(renewed));

        final ProfileManager manager = new Pac4jProducer().getProfileManager(configInstance(config), context, session);

        assertEquals(List.of(renewed), manager.getProfiles());
        assertSame(config, manager.getConfig());
        verify(client).renewUserProfile(any(CallContext.class), same(expired));
    }

    @Test
    void customProfileManagerFactoryReceivesConfiguration() {
        final Config config = new Config();
        final WebContext context = mock(WebContext.class);
        final SessionStore session = mock(SessionStore.class);
        final ProfileManager customManager = new ProfileManager(context, session);
        config.setProfileManagerFactory((ctx, store) -> customManager);

        final ProfileManager manager = new Pac4jProducer().getProfileManager(configInstance(config), context, session);

        assertSame(customManager, manager);
        assertSame(config, manager.getConfig());
    }

    @Test
    @SuppressWarnings("unchecked")
    void missingConfigurationDoesNotProduceManager() {
        final Instance<Config> instance = mock(Instance.class);
        assertNull(new Pac4jProducer().getProfileManager(instance, mock(WebContext.class), mock(SessionStore.class)));
    }
}

package org.pac4j.jee.filter;

import org.junit.jupiter.api.Test;
import org.pac4j.core.config.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SecurityFilterTests {

    @Test
    void lombokAccessorsConfigureSecurityEndpoint() {
        final Config config = new Config();
        final SecurityFilter filter = new SecurityFilter(config);
        filter.setClients("oidc");
        filter.setAuthorizers("isAuthenticated");
        filter.setMatchers("excludedPath");

        assertSame(config, filter.getSharedConfig());
        assertEquals("oidc", filter.getClients());
        assertEquals("isAuthenticated", filter.getAuthorizers());
        assertEquals("excludedPath", filter.getMatchers());
    }
}

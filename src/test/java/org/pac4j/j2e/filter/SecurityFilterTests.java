package org.pac4j.j2e.filter;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.TestsHelper;

import static org.mockito.Mockito.*;

/**
 * Tests {@link SecurityFilter}.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public final class SecurityFilterTests extends AbstractWebTests {

    @Before
    public void setUp() {
        super.setUp();
        filter = new SecurityFilter();
    }

    @Test
    public void testOldClientsFactory() throws Exception {
        filterConfig.addInitParameter("clientsFactory", VALUE);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "the clientsFactory servlet parameter is no longer supported");
    }

    @Test
    public void testLogicCalled() throws Exception {
        final SecurityLogic logic2 = mock(SecurityLogic.class);
        ((SecurityFilter) filter).setSecurityLogic(logic2);
        call();
        verify(logic2, times(1)).perform(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}

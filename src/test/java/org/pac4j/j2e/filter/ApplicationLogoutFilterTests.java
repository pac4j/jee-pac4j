package org.pac4j.j2e.filter;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.engine.ApplicationLogoutLogic;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link ApplicationLogoutFilter}.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public final class ApplicationLogoutFilterTests extends AbstractWebTests {

    @Before
    public void setUp() {
        filter = new ApplicationLogoutFilter();
        super.setUp();
    }

    @Test
    public void testLogicCalled() throws Exception {
        final ApplicationLogoutLogic logic2 = mock(ApplicationLogoutLogic.class);
        ((ApplicationLogoutFilter) filter).setApplicationLogoutLogic(logic2);
        call();
        verify(logic2, times(1)).perform(any(), any(), any(), any(), any());
    }
}

package org.pac4j.j2e.filter;

import org.junit.Before;
import org.junit.Test;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.TestsHelper;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link CallbackFilter}.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public final class CallbackFilterTests extends AbstractWebTests {

    @Before
    public void setUp() {
        filter = new CallbackFilter();
        super.setUp();
    }

    @Test
    public void testOldClientsFactory() throws Exception {
        filterConfig.addInitParameter("clientsFactory", VALUE);
        TestsHelper.expectException(() -> call(), TechnicalException.class, "the clientsFactory servlet parameter is no longer supported");
    }

    @Test
    public void testLogicCalled() throws Exception {
        final CallbackLogic logic2 = mock(CallbackLogic.class);
        ((CallbackFilter) filter).setCallbackLogic(logic2);
        call();
        verify(logic2, times(1)).perform(any(), any(), any(), any(), any(), any());
    }
}

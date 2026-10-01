package org.pac4j.jee.util;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.jee.filter.HttpServletResponseFilter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpServletResponseProducerTests {

    @Test
    void producesServletResponseWithoutFacesContext() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getAttribute(HttpServletResponseFilter.RESPONSE_ATTRIBUTE)).thenReturn(response);

        assertSame(response, new HttpServletResponseProducer().getHttpServletResponse(request));
    }

    @Test
    void retainsFacesResponseFallback() {
        final FacesContext context = mock(FacesContext.class);
        final ExternalContext externalContext = mock(ExternalContext.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(context.getExternalContext()).thenReturn(externalContext);
        when(externalContext.getResponse()).thenReturn(response);
        CurrentFacesContext.set(context);
        try {
            assertSame(response, new HttpServletResponseProducer().getHttpServletResponse(mock(HttpServletRequest.class)));
        } finally {
            CurrentFacesContext.set(null);
        }
    }

    @Test
    void missingResponseReportsRequiredFilter() {
        final TechnicalException failure = assertThrows(TechnicalException.class,
            () -> new HttpServletResponseProducer().getHttpServletResponse(mock(HttpServletRequest.class)));
        assertTrue(failure.getMessage().contains("HttpServletResponseFilter"));
    }

    @SuppressWarnings("deprecation")
    private static class CurrentFacesContext extends FacesContextWrapper {
        static void set(final FacesContext context) {
            setCurrentInstance(context);
        }
    }
}

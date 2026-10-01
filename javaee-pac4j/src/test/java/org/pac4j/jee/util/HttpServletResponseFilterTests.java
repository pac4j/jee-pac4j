package org.pac4j.jee.util;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.filter.HttpServletResponseFilter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpServletResponseFilterTests {

    private HttpServletRequest request() {
        final Map<String, Object> attributes = new HashMap<>();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(anyString())).thenAnswer(call -> attributes.get(call.getArgument(0)));
        doAnswer(call -> {
            attributes.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(request).setAttribute(anyString(), any());
        doAnswer(call -> {
            attributes.remove(call.getArgument(0));
            return null;
        }).when(request).removeAttribute(anyString());
        return request;
    }

    @Test
    void servletChainProducesResponseWithoutFacesAndCleansUp() throws Exception {
        final HttpServletRequest request = request();
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpServletResponseProducer producer = new HttpServletResponseProducer();

        new HttpServletResponseFilter().doFilter(request, response, (req, resp) -> {
            assertSame(request, req);
            assertSame(response, resp);
            assertSame(response, producer.getHttpServletResponse(request));
        });

        assertNull(request.getAttribute(HttpServletResponseFilter.RESPONSE_ATTRIBUTE));
    }

    @Test
    void nestedDispatchRestoresOuterResponse() throws Exception {
        final HttpServletRequest request = request();
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpServletResponse wrapped = new HttpServletResponseWrapper(response);
        final HttpServletResponseFilter filter = new HttpServletResponseFilter();
        final HttpServletResponseProducer producer = new HttpServletResponseProducer();

        filter.doFilter(request, response, (req, resp) -> {
            filter.doFilter(request, wrapped, (nestedReq, nestedResp) ->
                assertSame(wrapped, producer.getHttpServletResponse(request)));
            assertSame(response, producer.getHttpServletResponse(request));
        });

        assertNull(request.getAttribute(HttpServletResponseFilter.RESPONSE_ATTRIBUTE));
    }

    @Test
    void exceptionStillCleansUpResponse() {
        final HttpServletRequest request = request();
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final ServletException failure = new ServletException("downstream failure");

        assertSame(failure, assertThrows(ServletException.class,
            () -> new HttpServletResponseFilter().doFilter(request, response, (req, resp) -> { throw failure; })));
        assertNull(request.getAttribute(HttpServletResponseFilter.RESPONSE_ATTRIBUTE));
    }

    @Test
    void asyncResponseRemainsAvailableUntilRedispatchCompletes() throws Exception {
        final HttpServletRequest request = request();
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpServletResponse wrapped = new HttpServletResponseWrapper(response);
        final HttpServletResponseFilter filter = new HttpServletResponseFilter();
        final HttpServletResponseProducer producer = new HttpServletResponseProducer();
        when(request.isAsyncStarted()).thenReturn(true);

        filter.doFilter(request, response, (req, resp) -> { });
        assertSame(response, producer.getHttpServletResponse(request));

        when(request.getDispatcherType()).thenReturn(DispatcherType.ASYNC);
        when(request.isAsyncStarted()).thenReturn(false);
        filter.doFilter(request, wrapped, (req, resp) ->
            assertSame(wrapped, producer.getHttpServletResponse(request)));
        assertNull(request.getAttribute(HttpServletResponseFilter.RESPONSE_ATTRIBUTE));
    }

    @Test
    void separateRequestsKeepSeparateResponses() throws Exception {
        final HttpServletRequest first = request();
        final HttpServletRequest second = request();
        final HttpServletResponse firstResponse = mock(HttpServletResponse.class);
        final HttpServletResponse secondResponse = mock(HttpServletResponse.class);
        final HttpServletResponseFilter filter = new HttpServletResponseFilter();
        final HttpServletResponseProducer producer = new HttpServletResponseProducer();

        filter.doFilter(first, firstResponse, (req, resp) -> {
            filter.doFilter(second, secondResponse, (otherReq, otherResp) -> {
                assertSame(firstResponse, producer.getHttpServletResponse(first));
                assertSame(secondResponse, producer.getHttpServletResponse(second));
            });
            assertSame(firstResponse, producer.getHttpServletResponse(first));
        });
    }
}

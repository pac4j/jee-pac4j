package org.pac4j.jee.filter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import java.io.IOException;

/**
 * Makes the current servlet response available to the pac4j CDI producer.
 * <p>The filter is registered through {@link WebFilter} when annotation scanning is enabled.
 * Otherwise, register it explicitly before components which inject the response or a pac4j web context.
 * Nested dispatches restore the outer response. The request attribute is retained while asynchronous
 * processing is active and replaced on asynchronous redispatch.</p>
 * @deprecated Use the corresponding class from the {@code jakartaee-pac4j} library.
 */
@WebFilter(filterName = "pac4jHttpServletResponseFilter", urlPatterns = "/*", asyncSupported = true,
    dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE,
        DispatcherType.ERROR, DispatcherType.ASYNC})
@Deprecated
public class HttpServletResponseFilter implements Filter {

    /** Request attribute holding the response for the current filter invocation. */
    public static final String RESPONSE_ATTRIBUTE = HttpServletResponseFilter.class.getName() + ".response";

    /**
     * Creates a filter exposing servlet responses to CDI producers.
     */
    public HttpServletResponseFilter() {}

    /**
     * Exposes the current response while the downstream filter chain runs.
     *
     * @param request the current servlet request
     * @param response the current servlet response
     * @param chain the downstream filter chain
     * @throws IOException if downstream processing fails with an I/O error
     * @throws ServletException if downstream processing fails with a servlet error
     */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException {
        final Object previousResponse = request.getDispatcherType() == DispatcherType.ASYNC
            ? null : request.getAttribute(RESPONSE_ATTRIBUTE);
        request.setAttribute(RESPONSE_ATTRIBUTE, response);
        try {
            chain.doFilter(request, response);
        } finally {
            if (previousResponse != null) {
                request.setAttribute(RESPONSE_ATTRIBUTE, previousResponse);
            } else if (!request.isAsyncStarted()) {
                request.removeAttribute(RESPONSE_ATTRIBUTE);
            }
        }
    }
}

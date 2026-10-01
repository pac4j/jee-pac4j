package org.pac4j.jee.util;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.jee.filter.HttpServletResponseFilter;

/**
 * Produces a servlet response object corresponding to the response for the current request.
 *
 * @author Phillip Ross
 * @since 3.0.0
 */
@Named
@RequestScoped
@Slf4j
public class HttpServletResponseProducer {

    /**
     * Creates a CDI producer for the current HTTP response.
     */
    public HttpServletResponseProducer() {}

    private static final String RESPONSE_UNAVAILABLE = "No HTTP response available: register HttpServletResponseFilter "
        + "before components which inject the response or pac4j web context.";

    /**
     * Returns the response exposed by {@link HttpServletResponseFilter}, falling back to
     * the current {@link FacesContext} for existing JSF applications.
     *
     * @param request the current HTTP servlet request
     * @return the HTTP servlet response associated with the current request
     * @throws TechnicalException if neither the response filter nor a Faces context provides a response
     */
    @Produces
    HttpServletResponse getHttpServletResponse(final HttpServletRequest request) {
        LOGGER.trace("Producing an http servlet response...");
        final Object response = request.getAttribute(HttpServletResponseFilter.RESPONSE_ATTRIBUTE);
        if (response instanceof HttpServletResponse) {
            return (HttpServletResponse) response;
        }

        // Preserve support for existing JSF applications without the response filter.
        try {
            final FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                return (HttpServletResponse) facesContext.getExternalContext().getResponse();
            }
        } catch (final LinkageError e) {
            // Faces is optional in servlet applications.
            throw new TechnicalException(RESPONSE_UNAVAILABLE, e);
        }
        throw new TechnicalException(RESPONSE_UNAVAILABLE);
    }
}

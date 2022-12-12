package org.pac4j.jee.util;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

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
     * Factory method which produces an http servlet response.
     *
     * @return the http servlet response associated with the current servlet request
     */
    @Produces
    HttpServletResponse getHttpServletResponse() {
        LOGGER.trace("Producing an http servlet response...");
        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResponse();
        LOGGER.trace("Returning an http servlet response. (is null: {})", httpServletResponse == null);
        return httpServletResponse;
    }
}

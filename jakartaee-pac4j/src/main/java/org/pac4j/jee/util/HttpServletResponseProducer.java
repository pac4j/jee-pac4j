package org.pac4j.jee.util;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces a servlet response object corresponding to the response for the current request.
 *
 * @author Phillip Ross
 * @since 3.0.0
 */
@Named
@RequestScoped
public class HttpServletResponseProducer {

    /** The static logger instance. */
    private static final Logger logger = LoggerFactory.getLogger(HttpServletResponseProducer.class);

    /**
     * Factory method which produces an http servlet response.
     *
     * @return the http servlet response associated with the current servlet request
     */
    @Produces
    HttpServletResponse getHttpServletResponse() {
        logger.trace("Producing an http servlet response...");
        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResponse();
        logger.trace("Returning an http servlet response. (is null: {})", httpServletResponse == null);
        return httpServletResponse;
    }
}

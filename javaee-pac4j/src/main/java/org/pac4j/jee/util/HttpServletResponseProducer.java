package org.pac4j.jee.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

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

package org.pac4j.jee.util;

import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigSingleton;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Produces request-scoped web context and profile manager.
 *
 * @author Phillip Ross
 * @since 3.0.0
 */
@Named
@RequestScoped
public class Pac4jProducer {

    /** The static logger instance. */
    private static final Logger logger = LoggerFactory.getLogger(Pac4jProducer.class);

    /**
     * Factory method which produces a pac4j web context.
     *
     * @param httpServletRequest the http servlet request to be used for building the web context
     * @param httpServletResponse the http servlet response to be used for building the web context
     * @return a web context associated with the current servlet request
     */
    @Produces
    JEEContext getWebContext(final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse) {
        logger.trace("Producing a pac4j web context...");
        final Config config = ConfigSingleton.getConfig();
        if (config == null) {
            throw new TechnicalException("The Config of the ConfigSingleton is null. You must define at least one pac4j filter!");
        }
        JEEContext jEEContext = new JEEContext(
                httpServletRequest,
                httpServletResponse,
                config.getSessionStore()
        );
        logger.trace("Returning a pac4j web context.");
        return jEEContext;
    }

    /**
     * Factory method which produces a pac4j profile manager.
     *
     * @param webContext the web context to be used for building the profile manager
     * @return a profile manager associated with the current servlet request
     */
    @Produces
    ProfileManager getProfileManager(final WebContext webContext) {
        logger.trace("Producing a pac4j profile manager...");
        ProfileManager profileManager = new ProfileManager(webContext);
        logger.trace("Returning a pac4j profile manager.");
        return profileManager;
    }
}

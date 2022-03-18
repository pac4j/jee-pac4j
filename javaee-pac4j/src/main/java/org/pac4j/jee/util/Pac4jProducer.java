package org.pac4j.jee.util;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.FindBest;
import org.pac4j.jee.context.JEEContextFactory;
import org.pac4j.jee.context.session.JEESessionStore;
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
    WebContext getWebContext(final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse) {
        logger.trace("Producing a pac4j web context...");
        final WebContext webContext = FindBest.webContextFactory(null, Config.INSTANCE, JEEContextFactory.INSTANCE)
                .newContext(httpServletRequest, httpServletResponse);
        logger.trace("Returning a pac4j web context.");
        return webContext;
    }

    /**
     * Factory method which produces a pac4j session store.
     *
     * @return a session store associated with the current servlet request
     */
    @Produces
    SessionStore getSessionStore() {
        logger.trace("Producing a pac4j session store...");
        final SessionStore sessionStore = FindBest.sessionStore(null, Config.INSTANCE, JEESessionStore.INSTANCE);
        logger.trace("Returning a pac4j session store.");
        return sessionStore;
    }

    /**
     * Factory method which produces a pac4j profile manager.
     *
     * @param webContext the web context to be used for building the profile manager
     * @param sessionStore the session store to be used for building the profile manager
     * @return a profile manager associated with the current servlet request
     */
    @Produces
    ProfileManager getProfileManager(final WebContext webContext, final SessionStore sessionStore) {
        logger.trace("Producing a pac4j profile manager...");
        ProfileManager profileManager = new ProfileManager(webContext, sessionStore);
        logger.trace("Returning a pac4j profile manager.");
        return profileManager;
    }
}

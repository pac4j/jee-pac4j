package org.pac4j.jee.util;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.adapter.FrameworkAdapter;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEFrameworkParameters;

/**
 * Produces web contexts, session stores and configured profile managers for servlet requests.
 *
 * @author Phillip Ross
 * @since 3.0.0
 */
@Named
@RequestScoped
@Slf4j
public class Pac4jProducer {

    /**
     * Creates a CDI producer for pac4j components associated with servlet requests.
     */
    public Pac4jProducer() {}

    /**
     * Factory method which produces a pac4j web context.
     *
     * @param instanceConfig the CDI lookup for a unique pac4j configuration
     * @param httpServletRequest the HTTP servlet request
     * @param httpServletResponse the HTTP servlet response
     * @return the configured web context, or {@code null} if the configuration cannot be resolved
     */
    @Produces
    WebContext getWebContext(final Instance<Config> instanceConfig,
                             final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse) {

        if (instanceConfig.isResolvable()) {
            val config = instanceConfig.get();
            FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

            LOGGER.trace("Producing a pac4j web context...");
            val webContext = config.getWebContextFactory().newContext(new JEEFrameworkParameters(httpServletRequest, httpServletResponse));
            LOGGER.trace("Returning a pac4j web context.");
            return webContext;
        } else {
            LOGGER.debug("Unable to produce a web context: no Config available");
        }

        return null;
    }

    /**
     * Factory method which produces a pac4j session store.
     *
     * @param instanceConfig the CDI lookup for a unique pac4j configuration
     * @param httpServletRequest the HTTP servlet request
     * @param httpServletResponse the HTTP servlet response
     * @return the configured session store, or {@code null} if the configuration cannot be resolved
     */
    @Produces
    SessionStore getSessionStore(final Instance<Config> instanceConfig,
                                 final HttpServletRequest httpServletRequest,
                                 final HttpServletResponse httpServletResponse) {

        if (instanceConfig.isResolvable()) {
            val config = instanceConfig.get();
            FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

            LOGGER.trace("Producing a pac4j session store...");
            val sessionStore = config.getSessionStoreFactory().newSessionStore(new JEEFrameworkParameters(httpServletRequest, httpServletResponse));
            LOGGER.trace("Returning a pac4j session store.");
            return sessionStore;
        } else {
            LOGGER.debug("Unable to produce a session store: no Config available");
        }

        return null;
    }

    /**
     * Creates a pac4j profile manager and supplies its configuration for profile renewal.
     *
     * @param instanceConfig the CDI lookup for a unique pac4j configuration
     * @param webContext the web context to be used for building the profile manager
     * @param sessionStore the session store to be used for building the profile manager
     * @return the configured profile manager, or {@code null} if the configuration cannot be resolved
     */
    @Produces
    ProfileManager getProfileManager(final Instance<Config> instanceConfig,
                                     final WebContext webContext,
                                     final SessionStore sessionStore) {

        if (instanceConfig.isResolvable()) {
            val config = instanceConfig.get();
            FrameworkAdapter.INSTANCE.applyDefaultSettingsIfUndefined(config);

            LOGGER.trace("Producing a pac4j profile manager...");
            val profileManager = config.getProfileManagerFactory().apply(webContext, sessionStore);
            profileManager.setConfig(config);
            LOGGER.trace("Returning a pac4j profile manager.");
            return profileManager;
        } else {
            LOGGER.debug("Unable to produce a profile manager: no Config available");
        }

        return null;
    }
}

package org.pac4j.j2e.filter;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock a session store in memory.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public final class MockSessionStore implements SessionStore {

    private final Map<String, Object> attributes = new HashMap<>();

    @Override
    public String getOrCreateSessionId(WebContext context) {
        return "" + System.currentTimeMillis();
    }

    @Override
    public Object get(WebContext context, String key) {
        return attributes.get(key);
    }

    @Override
    public void set(WebContext context, String key, Object value) {
        attributes.put(key, value);
    }
}

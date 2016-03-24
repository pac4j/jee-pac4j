package org.pac4j.j2e.filter;

import org.pac4j.core.credentials.Credentials;

/**
 * Mock a credentials.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public final class MockCredentials extends Credentials {

    @Override
    public boolean equals(final Object o) {
        return (o instanceof MockCredentials);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}

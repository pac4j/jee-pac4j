package org.pac4j.j2e.filter;

import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;

/**
 * Return a credentials and can throw a {@link RequiresHttpAction}.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public interface ReturnCredentials {

    Credentials get() throws RequiresHttpAction;
}

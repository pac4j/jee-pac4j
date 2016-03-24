package org.pac4j.j2e.filter;

import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.CommonProfile;

/**
 * Mock an indirect client.
 *
 * @author Jerome Leleu
 * @since 1.3.0
 */
public final class MockIndirectClient extends IndirectClient<Credentials, CommonProfile> {

    private final RedirectAction redirectAction;

    private final Credentials credentials;

    private final CommonProfile profile;

    public MockIndirectClient(final String name, final RedirectAction redirectAction, final Credentials credentials, final CommonProfile profile) {
        setName(name);
        this.redirectAction = redirectAction;
        this.credentials = credentials;
        this.profile = profile;
    }

    @Override
    protected RedirectAction retrieveRedirectAction(final WebContext context) throws RequiresHttpAction {
        return redirectAction;
    }

    @Override
    protected Credentials retrieveCredentials(final WebContext context) throws RequiresHttpAction {
        return credentials;
    }

    @Override
    protected CommonProfile retrieveUserProfile(final Credentials credentials, final WebContext context) throws RequiresHttpAction {
        return profile;
    }
}

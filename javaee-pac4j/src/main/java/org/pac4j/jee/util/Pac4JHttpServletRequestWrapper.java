package org.pac4j.jee.util;

import org.pac4j.core.profile.ProfileHelper;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

/**
 * Exposes pac4j user profiles through the user and role methods of {@link HttpServletRequest}.
 * 
 * @author Victor Noel
 * @since 4.0.0
 *
 * @deprecated Use the corresponding class from the {@code jakartaee-pac4j} library.
 */
@Deprecated
public class Pac4JHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private Collection<UserProfile> profiles;

    /**
     * Wraps a request using the supplied pac4j profiles.
     *
     * @param request the HTTP servlet request to wrap
     * @param profiles the profiles used to resolve the principal and roles
     */
    public Pac4JHttpServletRequestWrapper(final HttpServletRequest request, final Collection<UserProfile> profiles) {
        super(request);
        this.profiles = profiles;
    }

    /**
     * Returns the name of the selected pac4j principal.
     *
     * @return the principal name, or {@code null} if no principal is available
     */
    @Override
    public String getRemoteUser() {
        return getPrincipal().map(p -> p.getName()).orElse(null);
    }

    private Optional<UserProfile> getProfile() {
        return ProfileHelper.flatIntoOneProfile(profiles);
    }

    private Optional<Principal> getPrincipal() {
        return getProfile().map(UserProfile::asPrincipal);
    }

    /**
     * Returns the principal of the first non-anonymous profile, falling back to an anonymous profile if necessary.
     *
     * @return the selected principal, or {@code null} if no principal is available
     */
    @Override
    public Principal getUserPrincipal() {
        return getPrincipal().orElse(null);
    }

    /**
     * Checks whether any supplied profile has the specified role.
     *
     * @param role the role name to check
     * @return {@code true} if at least one profile has the role
     */
    @Override
    public boolean isUserInRole(String role) {
        return this.profiles.stream().anyMatch(p -> p.getRoles().contains(role));
    }
}

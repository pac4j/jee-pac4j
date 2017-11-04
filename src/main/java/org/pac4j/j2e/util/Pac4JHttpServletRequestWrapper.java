package org.pac4j.j2e.util;

import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;

/**
 * This wraps the {@link HttpServletRequest} to make it pac4j-aware by with respect to user-related methods.
 * 
 * @author Victor Noel
 * @since 4.0.0
 *
 */
public class Pac4JHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private Collection<CommonProfile> profiles;

    public Pac4JHttpServletRequestWrapper(HttpServletRequest request, Collection<CommonProfile> profiles) {
        super(request);
        this.profiles = profiles;
    }

    @Override
    public String getRemoteUser() {
        return getPrincipal().map(p -> p.getName()).orElse(null);
    }

    private Optional<CommonProfile> getProfile() {
        return ProfileHelper.flatIntoOneProfile(profiles);
    }

    private Optional<Principal> getPrincipal() {
        return getProfile().map(CommonProfile::asPrincipal);
    }

    @Override
    public Principal getUserPrincipal() {
        return getPrincipal().orElse(null);
    }

    @Override
    public boolean isUserInRole(String role) {
        return this.profiles.stream().anyMatch(p -> p.getRoles().contains(role));
    }
}

/*
  Copyright 2013 - 2014 Jerome Leleu

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.j2e.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.profile.CommonProfile;

/**
 * An utility class to read/write the user profile.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class UserUtils {

    /**
     * Return if the user is authenticated.
     * 
     * @param request
     * @return if the user is authenticated
     */
    public static boolean isAuthenticated(final HttpServletRequest request) {
        return getProfile(request) != null;
    }

    /**
     * Return if the user is authenticated.
     * 
     * @param session
     * @return if the user is authenticated
     */
    public static boolean isAuthenticated(final HttpSession session) {
        return getProfile(session) != null;
    }

    /**
     * Read the profile from the request (stateless mode) or from the session if none is found (stateful mode).
     * 
     * @param request
     * @return the user profile
     */
    public static CommonProfile getProfile(final HttpServletRequest request) {
        CommonProfile profile = (CommonProfile) request.getAttribute(HttpConstants.USER_PROFILE);
        return (profile != null) ? profile : getProfile(request.getSession(false));
    }

    /**
     * Read the profile from the session.
     * 
     * @param session
     * @return the user profile
     */
    public static CommonProfile getProfile(final HttpSession session) {
        if (session != null) {
            return (CommonProfile) session.getAttribute(HttpConstants.USER_PROFILE);
        }
        return null;
    }

    /**
     * Save the profile in the request if stateless or in the session otherwise.
     * 
     * @param request
     * @param profile
     * @param stateless
     */
    public static void setProfile(final HttpServletRequest request, final CommonProfile profile, boolean stateless) {
        if (stateless) {
            request.setAttribute(HttpConstants.USER_PROFILE, profile);
        } else {
            setProfile(request.getSession(true), profile);
        }
    }

    /**
     * Use setProfile(request, profile, stateless) instead.
     * 
     * Save the profile in session.
     * 
     * @param request
     * @param profile
     */
    @Deprecated
    public static void setProfile(final HttpServletRequest request, final CommonProfile profile) {
        setProfile(request.getSession(true), profile);
    }

    /**
     * Save the profile in session.
     * 
     * @param session
     * @param profile
     */
    public static void setProfile(final HttpSession session, final CommonProfile profile) {
        session.setAttribute(HttpConstants.USER_PROFILE, profile);
    }

    /**
     * Logout the user.
     * 
     * @param request
     */
    public static void logout(final HttpServletRequest request) {
        setProfile(request, null);
    }

    /**
     * Logout the user.
     * 
     * @param session
     */
    public static void logout(final HttpSession session) {
        setProfile(session, null);
    }
}

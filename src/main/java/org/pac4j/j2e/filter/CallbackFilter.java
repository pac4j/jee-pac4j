/*
  Copyright 2013 - 2015 pac4j organization

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
package org.pac4j.j2e.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter handles the callback from the provider to finish the authentication process.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class CallbackFilter extends RequiresAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(CallbackFilter.class);

    private String defaultUrl = "/";

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        this.defaultUrl = filterConfig.getInitParameter("defaultUrl");
        CommonHelper.assertNotBlank("defaultUrl", this.defaultUrl);
    }

    @Override
    protected CommonProfile retrieveUserProfile(HttpServletRequest request, HttpServletResponse response,
            WebContext context) throws RequiresHttpAction {

        return super.authenticate(request, response, context);
    }

    @Override
    protected void authenticationSuccess(CommonProfile profile, HttpServletRequest request,
            HttpServletResponse response, FilterChain chain, WebContext context) throws IOException, ServletException {

        redirectToTarget(request, response);
    }

    @Override
    protected void authenticationFailure(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            WebContext context) throws IOException, ServletException {

        redirectToTarget(request, response);
    }

    private void redirectToTarget(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String requestedUrl = retrieveOriginalUrl(request);
        logger.debug("requestedUrl : {}", requestedUrl);
        if (CommonHelper.isNotBlank(requestedUrl)) {
            response.sendRedirect(requestedUrl);
        } else {
            response.sendRedirect(this.defaultUrl);
        }
    }

    /**
     * @return the default callback URL
     */
    public String getDefaultUrl() {
        return defaultUrl;
    }

    /**
     * @param defaultUrl the default callback URL to set
     */
    public void setDefaultUrl(final String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

}

/*
  Copyright 2013 Jerome Leleu

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
import javax.servlet.http.HttpSession;

import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.j2e.configuration.ClientsConfiguration;
import org.pac4j.j2e.util.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter is used to protect urls and redirect the user to the appropriate provider.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class RequiresAuthenticationFilter extends ClientsConfigFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequiresAuthenticationFilter.class);
    
    public final static String ATTEMPTED_AUTHENTICATION_SUFFIX = "attemptedAuthentifcationSuffix";
    
    public final static String ORIGINAL_REQUESTED_URL = "pac4jOriginalRequestedUrl";
    
    private String clientName;
    
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        this.clientName = filterConfig.getInitParameter("clientName");
        CommonHelper.assertNotBlank("clientName", this.clientName);
    }
    
    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                  final HttpSession session, final FilterChain chain) throws IOException,
        ServletException {
        
        final CommonProfile profile = UserUtils.getProfile(request);
        logger.debug("profile : {}", profile);
        
        // profile not null, already authenticated -> access
        if (profile != null) {
            chain.doFilter(request, response);
        } else {
            // no profile -> has this authentication already be attempted ?
            final String triedAuth = (String) session.getAttribute(this.clientName + ATTEMPTED_AUTHENTICATION_SUFFIX);
            logger.debug("triedAuth : {}", triedAuth);
            // authentication already tried -> 403
            if (CommonHelper.isNotBlank(triedAuth)) {
                session.setAttribute(this.clientName + ATTEMPTED_AUTHENTICATION_SUFFIX, null);
                response.sendError(403);
                logger.error("authentication already tried -> forbidden");
            } else {
                // no authentication tried -> redirect to provider
                // keep the current url
                final String requestedUrl = request.getRequestURL().toString() + "?" + request.getQueryString();
                logger.debug("requestedUrl : {}", requestedUrl);
                session.setAttribute(ORIGINAL_REQUESTED_URL, requestedUrl);
                // compute and perform the redirection
                final WebContext context = new J2EContext(request, response);
                final String redirectUrl = ClientsConfiguration.getClients().findClient(this.clientName)
                    .getRedirectionUrl(context);
                logger.debug("redirectUrl : {}", redirectUrl);
                response.sendRedirect(redirectUrl);
            }
        }
    }
}

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

import org.pac4j.core.client.Client;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.j2e.configuration.ClientsConfiguration;
import org.pac4j.j2e.util.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter handles the callback from the provider to finish the authentication process.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public class CallbackFilter extends ClientsConfigFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(CallbackFilter.class);
    
    private String defaultUrl = "/";
    
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        this.defaultUrl = filterConfig.getInitParameter("defaultUrl");
        CommonHelper.assertNotBlank("defaultUrl", this.defaultUrl);
    }
    
    @Override
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
                                  final HttpSession session, final FilterChain chain) throws IOException,
        ServletException {
        
        final WebContext context = new J2EContext(request, response);
        final Client client = ClientsConfiguration.getClients().findClient(context);
        logger.debug("client : {}", client);
        
        final Credentials credentials;
        try {
            credentials = client.getCredentials(context);
        } catch (final RequiresHttpAction e) {
            logger.debug("extra HTTP action required : {}", e.getCode());
            return;
        }
        logger.debug("credentials : {}", credentials);
        
        // get user profile
        final CommonProfile profile = (CommonProfile) client.getUserProfile(credentials);
        logger.debug("profile : {}", profile);
        
        if (profile == null) {
            // save that this kind of authentication has already been attempted and returns a null profile
            session.setAttribute(client.getName() + RequiresAuthenticationFilter.ATTEMPTED_AUTHENTICATION_SUFFIX,
                                 "true");
        }
        UserUtils.setProfile(session, profile);
        
        final String requestedUrl = (String) session.getAttribute(RequiresAuthenticationFilter.ORIGINAL_REQUESTED_URL);
        logger.debug("requestedUrl : {}", requestedUrl);
        if (CommonHelper.isNotBlank(requestedUrl)) {
            response.sendRedirect(requestedUrl);
        } else {
            response.sendRedirect(this.defaultUrl);
        }
    }
}

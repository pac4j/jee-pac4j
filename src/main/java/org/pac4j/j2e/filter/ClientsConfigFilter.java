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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.client.Clients;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.j2e.configuration.ClientsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class which handles the configuration of the clients.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public abstract class ClientsConfigFilter implements Filter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String getStringParam(final FilterConfig filterConfig, final String name, final String defaultValue) {
        final String param = filterConfig.getInitParameter(name);
        if (param != null) {
            return param;
        } else {
            return defaultValue;
        }
    }

    protected boolean getBooleanParam(final FilterConfig filterConfig, final String name, final boolean defaultValue) {
        final String param = filterConfig.getInitParameter(name);
        if (param != null) {
            return Boolean.parseBoolean(param);
        } else {
            return defaultValue;
        }
    }

    public void init(final FilterConfig filterConfig) throws ServletException {
        final String clientsFactoryParam = filterConfig.getInitParameter("clientsFactory");
        setClientsFactory(clientsFactoryParam);
        CommonHelper.assertNotNull("clients", ClientsConfiguration.getClients());
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HttpServletResponse resp = (HttpServletResponse) response;

        internalFilter(req, resp, chain);
    }

    protected abstract void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException;

    public void destroy() {
    }

    public Clients getClients() {
        return ClientsConfiguration.getClients();
    }

    public void setClients(final Clients clients) {
        ClientsConfiguration.setClients(clients);
    }

    public void setClientsFactory(String name) {
        if (name != null) {
            ClientsConfiguration.build(name);
        }
    }
}

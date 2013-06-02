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
package org.pac4j.j2e.configuration;

import org.pac4j.core.client.Clients;

/**
 * This class represents the configuration of all clients.
 * 
 * @author Jerome Leleu
 * @since 1.0.0
 */
public final class ClientsConfiguration {
    
    private static Clients clients;
    
    /**
     * Build the clients from the factory name.
     * 
     * @param name
     */
    @SuppressWarnings("unchecked")
    public synchronized static void build(final String name) {
        if (clients == null) {
            
            try {
                final Class<ClientsFactory> clazz = (Class<ClientsFactory>) Class.forName(name);
                final ClientsFactory factory = clazz.newInstance();
                clients = factory.build();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static Clients getClients() {
        return clients;
    }
}

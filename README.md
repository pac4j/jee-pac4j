## What is the j2e-pac4j library? [![Build Status](https://travis-ci.org/pac4j/j2e-pac4j.png?branch=master)](https://travis-ci.org/pac4j/j2e-pac4j)

The `j2e-pac4j` project is an authentication/authorization security library for J2E. It's available under the Apache 2 license and based on the [pac4j](https://github.com/pac4j/pac4j) library.

It supports stateful and stateless [authentication flows](https://github.com/pac4j/pac4j/wiki/Authentication-flows) using external identity providers or direct internal credentials authenticator and user profile creator:

1. **OAuth** (1.0 & 2.0): Facebook, Twitter, Google, Yahoo, LinkedIn, Github... using the `pac4j-oauth` module
2. **CAS** (1.0, 2.0, SAML, logout & proxy) + REST API support using the `pac4j-cas` module
3. **HTTP** (form, basic auth, IP, header, GET/POST parameter authentications) using the `pac4j-http` module
4. **OpenID** using the `pac4j-openid` module
5. **SAML** (2.0) using the `pac4j-saml` module
6. **Google App Engine** UserService using the `pac4j-gae` module
7. **OpenID Connect** 1.0 using the `pac4j-oidc` module
8. **JWT** using the `pac4j-jwt` module
9. **LDAP** using the `pac4j-ldap` module
10. **relational DB** using the `pac4j-sql` module
11. **MongoDB** using the `pac4j-mongo` module.

See [all authentication mechanisms](https://github.com/pac4j/pac4j/wiki/Clients).


## Technical description

This library has **only 4 classes**:

1. the `ClientConfiguration` class gathers all the clients configuration
2. the `ClientsConfigFilter` is an abstract J2E filter in charge of loading clients configuration
3. the `RequiresAuthenticationFilter` is a J2E filter to protect urls and requires authentication
4. the `CallbackFilter` is a J2E filter to handle the callback from an identity provider after login to finish the authentication process.

and is based on the `pac4j-core` library. Learn more by browsing the [j2e-pac4j Javadoc](http://www.pac4j.org/apidocs/j2e-pac4j/index.html) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/index.html).


## How to use it?

### Add the required dependencies (`j2e-pac4j` + `pac4j-*` libraries)

You need to add a dependency on the `j2e-pac4j` library (<em>groupId</em>: **org.pac4j**, *latest version*: **1.2.0-SNAPSHOT**) as well as on the appropriate `pac4j` modules (<em>groupId</em>: **org.pac4j**, *version*: **1.8.0-SNAPSHOT**): the `pac4j-oauth` dependency for OAuth support, the `pac4j-cas` dependency for CAS support, the `pac4j-ldap` module for LDAP authentication, ...  

As snapshot dependencies are only available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j/), this repository must be added in the Maven *pom.xml* file for example:

    <repositories>
      <repository>
        <id>sonatype-nexus-snapshots</id>
        <name>Sonatype Nexus Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
          <enabled>false</enabled>
        </releases>
        <snapshots>
          <enabled>true</enabled>
        </snapshots>
      </repository>
    </repositories>


### Define the authentication mechanisms (`*Client` + `Clients` classes)

Each authentication mechanism (Facebook, Twitter, a CAS server...) is defined by a client. All clients must be gathered in a `Clients` class.  
They can be defined in a specific class implementing the `org.pac4j.core.client.ClientsFactory` interface or through dependency injection. For example:

    public class DemoClientsFactory implements ClientsFactory {
    
        @Override
        public Clients build(final Object env) {
            final OidcClient oidcClient = new OidcClient();
            oidcClient.setClientID("id");
            oidcClient.setSecret("secret");
            oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
            oidcClient.addCustomParam("prompt", "consent");
    
            final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks", "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:testshib-providers.xml");
            cfg.setMaximumAuthenticationLifetime(3600);
            cfg.setServiceProviderEntityId("urn:mace:saml:pac4j.org");
            cfg.setServiceProviderMetadataPath(new File("target", "sp-metadata.xml").getAbsolutePath());
            final SAML2Client saml2Client = new SAML2Client(cfg);
    
            final FacebookClient facebookClient = new FacebookClient("key", "secret");
            final TwitterClient twitterClient = new TwitterClient("key", "secret");
    
            final FormClient formClient = new FormClient("http://localhost:8080/theForm.jsp", new SimpleTestUsernamePasswordAuthenticator(), new SimpleTestUsernameProfileCreator());
            final IndirectBasicAuthClient basicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator(), new SimpleTestUsernameProfileCreator());
    
            final CasClient casClient = new CasClient();
            casClient.setCasLoginUrl("http://localhost:8888/cas/login");
    
            // REST authent with JWT for a token passed in the url as the token parameter
            ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator("salt"), new AuthenticatorProfileCreator());
    
            return new Clients("http://localhost:8080/callback", oidcClient, saml2Client, facebookClient,
                    twitterClient, formClient, basicAuthClient, casClient, stravaClient, parameterClient);
        }
    }

"http://localhost:8080/callback" is the url of the callback endpoint (see below). It may not be defined for REST support only.


### Define the callback endpoint (only for stateful authentication mechanisms)

Some authentication mechanisms rely on external identity providers (like Facebook) and thus require to define a callback endpoint where the user will be redirected after login at the identity provider. For REST support only, this callback endpoint is not necessary.  
It must be define in the *web.xml* file by the `CallbackFilter` (callback url: "/callback", `defaultUrl`(optional): default url after authentication, clients defined via the `DemoClientsFactory`):

    <filter>
        <filter-name>callbackFilter</filter-name>
        <filter-class>org.pac4j.j2e.filter.CallbackFilter</filter-class>
        <init-param>
        	<param-name>clientsFactory</param-name>
        	<param-value>org.pac4j.demo.j2e.config.DemoClientsFactory</param-value>
        </init-param>
        <init-param>
        	<param-name>defaultUrl</param-name>
        	<param-value>/</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>callbackFilter</filter-name>
        <url-pattern>/callback</url-pattern>
        <dispatcher>REQUEST</dispatcher>
    </filter-mapping>

Or you can use dependency injection. For example, using Spring, you can define the callback filter as a `DelegatingFilterProxy` in the *web.xml* file:

    <filter>
        <filter-name>callbackFilter</filter-name>
        <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>callbackFilter</filter-name>
        <url-pattern>/callback</url-pattern>
        <dispatcher>REQUEST</dispatcher>
    </filter-mapping>
    
and the specific bean in the *application-context.xml* file:

    <bean id="callbackFilter" class="org.pac4j.j2e.filter.CallbackFilter">
        <property name="clientsFactory" value="org.pac4j.demo.j2e.config.DemoClientsFactory" />
        <property name="defaultUrl" value="/" />
    </bean>

### Protect an url (authentication + authorization)

You can protect an url and require the user to be authenticated by a client (and optionnally have the appropriate roles / permissions) by using the `RequiresAuthenticationFilter`
(`clientName`: the authentication mechanism (`FacebookClient`), `requireAnyRole` (optional): one of the provided roles is necessary, `requireAllRoles` (optional): all roles are necessary, `allowDynamicClientSelection` (optional): if other clients can be used on this url (providing a *client_name* parameter in the url), `useSessionForDirectClient` (optional): if the session must be used (REST client), `isAjax` (optional): if this url is called in an AJAX way): 

    <filter>
        <filter-name>FacebookAdminFilter</filter-name>
        <filter-class>org.pac4j.j2e.filter.RequiresAuthenticationFilter</filter-class>
        <init-param>
            <param-name>clientName</param-name>
            <param-value>FacebookClient</param-value>
        </init-param>
        <init-param>
            <param-name>requireAnyRole</param-name>
            <param-value>ROLE_ADMIN</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>FacebookAdminFilter</filter-name>
        <url-pattern>/facebookadmin/*</url-pattern>
        <dispatcher>REQUEST</dispatcher>
    </filter-mapping>

Define the appropriate `AuthorizationGenerator` and attach it to the client (using the `addAuthorizationGenerator` method of the `UserProfile` class) to compute the roles / permissions of the authenticated user.

This filter can be defined via dependency injection as well. In that case, the `authorizer` property of the `RequiresAuthenticationFilter` enables you to define a specific `Authorizer` for the protected url.


#### Get redirection urls

You can also explicitly compute a redirection url to a provider by using the `getRedirectAction` method and the `ClientsConfiguration` class, in order to create an explicit link for login. For example with Facebook:

	Clients client = ClientsConfiguration.getClients();
	FacebookClient fbClient = (FacebookClient) client.findClient("FacebookClient");
	WebContext context = new J2EContext(request, response);
	String fbLoginUrl = fbClient.getRedirectAction(context, false, false).getLocation();


#### Get the user profile

You can test if the user is authenticated using the `ProfileManager.isAuthenticated()` method or get the user profile using the `ProfileManager.get(true)` method (`false` not to use the session, but only the current HTTP request).

The retrieved profile is at least a `CommonProfile`, from which you can retrieve the most common properties that all profiles share. But you can also cast the user profile to the appropriate profile according to the provider used for authentication. For example, after a Facebook authentication:
 
    FacebookProfile facebookProfile = (FacebookProfile) commonProfile;


#### Logout

You can log out the current authenticated user using the `ProfileManager.logout()` method.


## Demo

The [j2e-pac4j-demo](https://github.com/pac4j/j2e-pac4j-demo) is available with various authentication mechanisms: Facebook, Twitter, CAS, form, basic auth...


## Release notes

See the [release notes](https://github.com/pac4j/j2e-pac4j/wiki/Release-Notes).


## Need help?

If you have any question, please use the following mailing lists:

- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)

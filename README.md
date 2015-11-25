<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-j2e.png" width="300" />
</p>

The `j2e-pac4j` project is an **easy and powerful security library for J2E** web applications which supports authentication and authorization, but also application logout and advanced features like CSRF protection. It's available under the Apache 2 license and based on the **[pac4j security engine](https://github.com/pac4j/pac4j)**.

It supports most authentication mechanisms, called [**clients**](https://github.com/pac4j/pac4j/wiki/Clients):

- **indirect / stateful clients** are for UI when the user authenticates once at an external provider (like Facebook, a CAS server...) or via a local form (or basic auth popup)  
- **direct / stateless clients** are for web services when credentials (like basic auth, tokens...) are passed for each HTTP request.

See the [authentication flows](https://github.com/pac4j/pac4j/wiki/Authentication-flows).

| The authentication mechanism you want | The `pac4j-*` submodule(s) you must use
|---------------------------------------|----------------------------------------
| OAuth (1.0 & 2.0): Facebook, Twitter, Google, Yahoo, LinkedIn, Github... | `pac4j-oauth`
| CAS (1.0, 2.0, 3.0, SAML, logout, proxy) | `pac4j-cas`
| SAML (2.0) | `pac4j-saml`
| OpenID Connect (1.0) | `pac4j-oidc`
| HTTP (form, basic auth, IP, header, cookie, GET/POST parameter)<br />+<br />JWT<br />or LDAP<br />or Relational DB<br />or MongoDB<br />or Stormpath<br />or CAS REST API| `pac4j-http`<br />+<br />`pac4j-jwt`<br />or `pac4j-ldap`<br />or `pac4j-sql`<br />or `pac4j-mongo`<br />or `pac4j-stormpath`<br />or `pac4j-cas`
| Google App Engine UserService | `pac4j-gae`
| OpenID | `pac4j-openid`

It also supports many authorization checks, called [**authorizers**](https://github.com/pac4j/pac4j/wiki/Authorizers) available in the `pac4j-core` (and `pac4j-http`) submodules: role / permission checks, profile verification, CSRF token validation...


## How to use it?

First, you need to add a dependency on this library as well as on the appropriate `pac4j` submodules. Then, you must define the [**clients**](https://github.com/pac4j/pac4j/wiki/Clients) for authentication and the [**authorizers**](https://github.com/pac4j/pac4j/wiki/Authorizers) to check authorizations.

Define the `CallbackFilter` to finish authentication processes if you use indirect clients (like Facebook).

Use the `RequiresAuthenticationFilter` to secure the urls of your web application (using the `clientName` parameter for authentication and the `authorizerName` parameter for authorizations).

Just follow these easy steps:


### Add the required dependencies (`j2e-pac4j` + `pac4j-*` libraries)

You need to add a dependency on the `j2e-pac4j` library (<em>groupId</em>: **org.pac4j**, *version*: **1.2.0**) as well as on the appropriate `pac4j` submodules (<em>groupId</em>: **org.pac4j**, *version*: **1.8.0**): the `pac4j-oauth` dependency for OAuth support, the `pac4j-cas` dependency for CAS support, the `pac4j-ldap` module for LDAP authentication, ...

All artifacts are available in the [Maven central repository](http://search.maven.org/#search%7Cga%7C1%7Cpac4j).

### Define the configuration (`Config` + `Clients` + `XXXClient` + `Authorizer`)

Each authentication mechanism (Facebook, Twitter, a CAS server...) is defined by a client (implementing the `org.pac4j.core.client.Client` interface). All clients must be gathered in a `org.pac4j.core.client.Clients` class.

They can be defined in a specific class implementing the `org.pac4j.core.config.ConfigFactory` interface to build a `org.pac4j.core.config.Config` which contains the `Clients` as well as the authorizers which will be used by the application.
For example:

    public class DemoConfigFactory implements ConfigFactory {
    
        @Override
        public Config build() {
            final OidcClient oidcClient = new OidcClient();
            oidcClient.setClientID("id");
            oidcClient.setSecret("secret");
            oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
            oidcClient.addCustomParam("prompt", "consent");
    
            final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks",
                    "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:testshib-providers.xml");
            cfg.setMaximumAuthenticationLifetime(3600);
            cfg.setServiceProviderEntityId("urn:mace:saml:pac4j.org");
            cfg.setServiceProviderMetadataPath(new File("target", "sp-metadata.xml").getAbsolutePath());
            final SAML2Client saml2Client = new SAML2Client(cfg);
    
            final FacebookClient facebookClient = new FacebookClient("fbId", "fbSecret");
            final TwitterClient twitterClient = new TwitterClient("twId", "twSecret");
    
            final FormClient formClient = new FormClient("http://localhost:8080/theForm.jsp", new SimpleTestUsernamePasswordAuthenticator());
            final IndirectBasicAuthClient basicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());
    
            final CasClient casClient = new CasClient();
            casClient.setCasLoginUrl("http://mycasserver/login");
    
            ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator("salt"));
    
            final Clients clients = new Clients("http://localhost:8080/callback", oidcClient, saml2Client, facebookClient,
                    twitterClient, formClient, basicAuthClient, casClient, parameterClient);
    
            final Config config = new Config(clients);
            config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
            config.addAuthorizer("custom", new CustomAuthorizer());
    
            return config;
        }
    }

"http://localhost:8080/callback" is the url of the callback endpoint (see below). It may not be defined for REST support / direct clients only.

Notice that you can also use a specific `SessionStore` by defining it via the `Config.setSessionStore(sessionStore)` method.

If your application is configured via dependency injection, no factory is required to build the configuration, you can directly inject the `Config` via the appropriate setter.


### Define the callback endpoint (only for stateful / indirect authentication mechanisms)

Indirect clients rely on external identity providers (like Facebook) and thus require to define a callback endpoint in the application where the user will be redirected after login at the identity provider. For REST support / direct clients only, this callback endpoint is not necessary.  
It must be defined in the *web.xml* file by the `CallbackFilter`:

    <filter>
        <filter-name>callbackFilter</filter-name>
        <filter-class>org.pac4j.j2e.filter.CallbackFilter</filter-class>
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

The `defaultUrl` parameter defines where the user will be redirected after login if no url was originally requested.

Using dependency injection via Spring for example, you can define the callback filter as a `DelegatingFilterProxy` in the *web.xml* file:

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
        <property name="defaultUrl" value="/" />
    </bean>


### Protect an url (authentication + authorization)

You can protect an url and require the user to be authenticated by a client (and optionally have the appropriate authorizations) by using the `RequiresAuthenticationFilter`:

    <filter>
        <filter-name>FacebookAdminFilter</filter-name>
        <filter-class>org.pac4j.j2e.filter.RequiresAuthenticationFilter</filter-class>
        <init-param>
            <param-name>configFactory</param-name>
            <param-value>org.pac4j.demo.j2e.config.DemoConfigFactory</param-value>
        </init-param>
        <init-param>
            <param-name>clientName</param-name>
            <param-value>FacebookClient</param-value>
        </init-param>
        <init-param>
            <param-name>authorizerName</param-name>
            <param-value>admin</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>FacebookAdminFilter</filter-name>
        <url-pattern>/facebookadmin/*</url-pattern>
        <dispatcher>REQUEST</dispatcher>
    </filter-mapping>

The following parameters can be defined:

- `clientName` (optional): the list of client names (separated by commas) used for authentication. If the user is not authenticated, direct clients are tried successively then if the user is still not authenticated and if the first client is an indirect one, this client is used to start the authentication. Otherwise, a 401 HTTP error is returned. If the *client_name* request parameter is provided, only the matching client is selected
- `configFactory`: the factory to initialize the configuration: clients and authorizers (only one filter needs to define it as the configuration is shared)
- `authorizerName` (optional): the list of authorizer names (separated by commas) used to check authorizations. If the user is not authorized, a 403 HTTP error is returned. By default (if blank), the user only requires to be authenticated to access the resource.
- `matcherName` (optional): the list of matcher names (separated by commas) that the request must satisfy to apply authentication / authorization. By default, all requests are checked

This filter can be defined via dependency injection as well. In that case, these parameters will be defined via setters.


### Get redirection urls

You can also explicitly compute a redirection url to a provider by using the `getRedirectAction` method of the client, in order to create an explicit link for login. For example with Facebook:

	Clients client = ConfigSingleton.getConfig().getClients();
	FacebookClient fbClient = (FacebookClient) client.findClient("FacebookClient");
	WebContext context = new J2EContext(request, response);
	String fbLoginUrl = fbClient.getRedirectAction(context, false).getLocation();


### Get the user profile

You can test if the user is authenticated using the `ProfileManager.isAuthenticated()` method or get the user profile using the `ProfileManager.get(true)` method (`false` not to use the session, but only the current HTTP request).

The retrieved profile is at least a `CommonProfile`, from which you can retrieve the most common properties that all profiles share. But you can also cast the user profile to the appropriate profile according to the provider used for authentication. For example, after a Facebook authentication:
 
    FacebookProfile facebookProfile = (FacebookProfile) commonProfile;


### Logout

You can log out the current authenticated user using the `ApplicationLogoutFilter`:

    <filter>
        <filter-name>logoutFilter</filter-name>
        <filter-class>org.pac4j.j2e.filter.ApplicationLogoutFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>logoutFilter</filter-name>
        <url-pattern>/logout</url-pattern>
        <dispatcher>REQUEST</dispatcher>
    </filter-mapping>

To perfom the logout, you must call the /logout url. A blank page is displayed by default unless an *url* request parameter is provided. In that case, the user will be redirected to this specified url (if it matches the logout url pattern defined) or to the default logout url otherwise.

The following parameters can be defined:

- `defaultUrl` (optional): the default logout url if the provided *url* parameter does not match the `logoutUrlPattern` (by default: /)
- `logoutUrlPattern` (optional): the logout url pattern that the logout url must match (it's a security check, only relative urls are allowed by default).


## Migration guide (1.1 -> 1.2)

Authorizations are now handled by the library so the `ClientFactory` can now longer be used and is replaced by a `ConfigFactory` which builds a `Config` which gathers clients (for authentication) and authorizers (for authorizations).

The `isAjax` parameter is no longer available as AJAX requests are now automatically detected. The `stateless` parameter is no longer available as the stateless nature is held by the client itself.

The `requireAnyRole` and `requieAllRoles` parameters are no longer available and authorizers must be used instead (with the `authorizerName` parameter).

The application logout process can be managed with the `ApplicationLogoutFilter`.

## Demo

The demo webapp: [j2e-pac4j-demo](https://github.com/pac4j/j2e-pac4j-demo) is available for tests and implement many authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...


## Release notes

See the [release notes](https://github.com/pac4j/j2e-pac4j/wiki/Release-Notes). Learn more by browsing the [j2e-pac4j Javadoc](http://www.javadoc.io/doc/org.pac4j/j2e-pac4j/1.2.0) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/1.8.0/index.html).


## Need help?

If you have any question, please use the following mailing lists:

- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)

## Development

The next version 1.2.1-SNAPSHOT is under development.

Maven artifacts are built via Travis: [![Build Status](https://travis-ci.org/pac4j/j2e-pac4j.png?branch=master)](https://travis-ci.org/pac4j/j2e-pac4j) and available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j). This repository must be added in the Maven *pom.xml* file for example:

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

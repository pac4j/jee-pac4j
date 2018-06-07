<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-j2e.png" width="300" />
</p>

The `j2e-pac4j` project is an **easy and powerful security library for J2E** web applications which supports authentication and authorization, but also logout and advanced features like session fixation and CSRF protection.
It's based on Java 8, JavaEE 7 and on the **[pac4j security engine](https://github.com/pac4j/pac4j) v3**. It's available under the Apache 2 license.

[**Main concepts and components:**](http://www.pac4j.org/docs/main-concepts-and-components.html)

1) A [**client**](http://www.pac4j.org/docs/clients.html) represents an authentication mechanism. It performs the login process and returns a user profile. An indirect client is for UI authentication while a direct client is for web services authentication:

&#9656; OAuth - SAML - CAS - OpenID Connect - HTTP - OpenID - Google App Engine - LDAP - SQL - JWT - MongoDB - Stormpath - IP address

2) An [**authorizer**](http://www.pac4j.org/docs/authorizers.html) is meant to check authorizations on the authenticated user profile(s) or on the current web context:

&#9656; Roles / permissions - Anonymous / remember-me / (fully) authenticated - Profile type, attribute -  CORS - CSRF - Security headers - IP address, HTTP method

3) The `SecurityFilter` protects an url by checking that the user is authenticated and that the authorizations are valid, according to the clients and authorizers configuration. If the user is not authenticated, it performs authentication for direct clients or starts the login process for indirect clients

4) The `CallbackFilter` finishes the login process for an indirect client

5) The `LogoutFilter` handles the logout process.


Just follow these easy steps to secure your JavaEE application:

### 1) Add the required dependencies (`j2e-pac4j` + `pac4j-*` libraries)

You need to add a dependency on:
 
- the `j2e-pac4j` library (<em>groupId</em>: **org.pac4j**, *version*: **4.0.0**)
- the appropriate `pac4j` [submodules](http://www.pac4j.org/docs/clients.html) (<em>groupId</em>: **org.pac4j**, *version*: **3.0.0**): `pac4j-oauth` for OAuth support (Facebook, Twitter...), `pac4j-cas` for CAS support, `pac4j-ldap` for LDAP authentication, etc.

All released artifacts are available in the [Maven central repository](http://search.maven.org/#search%7Cga%7C1%7Cpac4j).

---

### 2) Define the configuration (`Config` + `Client` + `Authorizer`)

The configuration (`org.pac4j.core.config.Config`) contains all the clients and authorizers required by the application to handle security.

It can be built via a configuration factory (`org.pac4j.core.config.ConfigFactory`) if the `configFactory` servlet parameter is used:

```java
public class DemoConfigFactory implements ConfigFactory {

    @Override
    public Config build(final Object... parameters) {
        final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks", "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:testshib-providers.xml");
        cfg.setMaximumAuthenticationLifetime(3600);
        cfg.setServiceProviderEntityId("http://localhost:8080/callback?client_name=SAML2Client");
        cfg.setServiceProviderMetadataPath(new File("sp-metadata.xml").getAbsolutePath());
        final SAML2Client saml2Client = new SAML2Client(cfg);

        final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
        final TwitterClient twitterClient = new TwitterClient("CoxUiYwQOSFDReZYdjigBA", "2kAzunH5Btc4gRSaMr7D7MkyoJ5u1VzbOOzE8rBofs");
        final FormClient formClient = new FormClient("http://localhost:8080/loginForm.jsp", new SimpleTestUsernamePasswordAuthenticator());

        ...

        final Clients clients = new Clients("http://localhost:8080/callback", oidcClient, saml2Client, facebookClient,
                twitterClient, formClient, indirectBasicAuthClient, casClient, parameterClient,
                directBasicAuthClient, new AnonymousClient(), casProxy);

        final Config config = new Config(clients);
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer<>("ROLE_ADMIN"));
        config.addAuthorizer("custom", new CustomAuthorizer());
        config.addAuthorizer("mustBeAnon", new IsAnonymousAuthorizer<>("/?mustBeAnon"));
        config.addAuthorizer("mustBeAuth", new IsAuthenticatedAuthorizer<>("/?mustBeAuth"));
        config.addMatcher("excludedPath", new PathMatcher().excludeRegex("^/facebook/notprotected\\.jsp$"));
        return config;
    }
}
```

See a [full example here](https://github.com/pac4j/j2e-pac4j-demo/blob/master/src/main/java/org/pac4j/demo/j2e/DemoConfigFactory.java).

Or produced via CDI:

```java
@Dependent
public class SecurityConfig {

    @Produces @ApplicationScoped
    private Config buildConfiguration() {
        logger.debug("building Security configuration...");

        final OidcConfiguration oidcConfiguration = new OidcConfiguration();
        oidcConfiguration.setClientId("167480702619-8e1lo80dnu8bpk3k0lvvj27noin97vu9.apps.googleusercontent.com");
        oidcConfiguration.setSecret("MhMme_Ik6IH2JMnAT6MFIfee");
        oidcConfiguration.setUseNonce(true);
        oidcConfiguration.addCustomParam("prompt", "consent");
        final GoogleOidcClient oidcClient = new GoogleOidcClient(oidcConfiguration);
        oidcClient.setAuthorizationGenerator((ctx, profile) -> { profile.addRole("ROLE_ADMIN"); return profile; });

        final FormClient jsfFormClient = new FormClient(
                "http://localhost:8080/jsfLoginForm.action",
                new SimpleTestUsernamePasswordAuthenticator()
        );
        jsfFormClient.setName("jsfFormClient");

        final IndirectBasicAuthClient indirectBasicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

        final CasConfiguration configuration = new CasConfiguration("http://localhost:8888/cas/login");
        final CasClient casClient = new CasClient(configuration);

        final List<SignatureConfiguration> signatures = new ArrayList<>();
        signatures.add(new SecretSignatureConfiguration(Constants.JWT_SALT));
        ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator(signatures));
        parameterClient.setSupportGetRequest(true);
        parameterClient.setSupportPostRequest(false);

        final DirectBasicAuthClient directBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

        ...

        final Clients clients = new Clients(
                "http://localhost:8080/callback",
                oidcClient,
                formClient,
                jsfFormClient,
                saml2Client, facebookClient, twitterClient, indirectBasicAuthClient, casClient,
                parameterClient, directBasicAuthClient, new AnonymousClient()
        );

        return new Config(clients);
    }
}
```

See a [full example here](https://github.com/pac4j/j2e-pac4j-cdi-demo/blob/master/src/main/java/org/pac4j/demo/j2e/SecurityConfig.java).

`http://localhost:8080/callback` is the url of the callback endpoint, which is only necessary for indirect clients.

Notice that you can define specific [matchers](http://www.pac4j.org/docs/matchers.html) via the `addMatcher(name, Matcher)` method.


---

### 3) Protect urls (`SecurityFilter`)

You can protect (authentication + authorizations) the urls of your J2E application by using the `SecurityFilter` and defining the appropriate mapping. It has the following behaviour:

1) If the HTTP request matches the `matchers` configuration (or no `matchers` are defined), the security is applied. Otherwise, the user is automatically granted access.

2) First, if the user is not authenticated (no profile) and if some clients have been defined in the `clients` parameter, a login is tried for the direct clients.

3) Then, if the user has a profile, authorizations are checked according to the `authorizers` configuration. If the authorizations are valid, the user is granted access. Otherwise, a 403 error page is displayed.

4) Finally, if the user is still not authenticated (no profile), he is redirected to the appropriate identity provider if the first defined client is an indirect one in the `clients` configuration. Otherwise, a 401 error page is displayed.


The following options are available:

1) `configFactory`: the class name of the factory to build the configuration (the configuration is shared across filters so it can be specified only once, but each filter can defined its own configuration if necessary) or `config`: the configuration itself

2) `clients` (optional): the list of client names (separated by commas) used for authentication:
- in all cases, this filter requires the user to be authenticated. Thus, if the `clients` is blank or not defined, the user must have been previously authenticated
- if the `client_name` request parameter is provided, only this client (if it exists in the `clients`) is selected.

3) `authorizers` (optional): the list of authorizer names (separated by commas) used to check authorizations:
- if the `authorizers` is blank or not defined, no authorization is checked
- the following authorizers are available by default (without defining them in the configuration):
  * `isFullyAuthenticated` to check if the user is authenticated but not remembered, `isRemembered` for a remembered user, `isAnonymous` to ensure the user is not authenticated, `isAuthenticated` to ensure the user is authenticated (not necessary by default unless you use the `AnonymousClient`)
  * `hsts` to use the `StrictTransportSecurityHeader` authorizer, `nosniff` for `XContentTypeOptionsHeader`, `noframe` for `XFrameOptionsHeader `, `xssprotection` for `XSSProtectionHeader `, `nocache` for `CacheControlHeader ` or `securityHeaders` for the five previous authorizers
  * `csrfToken` to use the `CsrfTokenGeneratorAuthorizer` with the `DefaultCsrfTokenGenerator` (it generates a CSRF token and saves it as the `pac4jCsrfToken` request attribute and in the `pac4jCsrfToken` cookie), `csrfCheck` to check that this previous token has been sent as the `pac4jCsrfToken` header or parameter in a POST request and `csrf` to use both previous authorizers.

4) `matchers` (optional): the list of matcher names (separated by commas) that the request must satisfy to check authentication / authorizations

5) `multiProfile` (optional): it indicates whether multiple authentications (and thus multiple profiles) must be kept at the same time (`false` by default).


The filter can be defined in the `web.xml` file:

```xml
<filter>
  <filter-name>FacebookAdminFilter</filter-name>
  <filter-class>org.pac4j.j2e.filter.SecurityFilter</filter-class>
  <init-param>
    <param-name>configFactory</param-name>
    <param-value>org.pac4j.demo.j2e.DemoConfigFactory</param-value>
  </init-param>
  <init-param>
    <param-name>clients</param-name>
    <param-value>FacebookClient</param-value>
  </init-param>
</filter>
<filter-mapping>
  <filter-name>FacebookAdminFilter</filter-name>
  <url-pattern>/facebook/*</url-pattern>
</filter-mapping>
```

or using CDI and the `FilterHelper`:

```java
@Named
@ApplicationScoped
public class WebConfig {

    @Inject
    private Config config;

    public void build(@Observes @Initialized(ApplicationScoped.class) ServletContext servletContext) {

        final FilterHelper filterHelper = new FilterHelper(servletContext);

        ...

        final SecurityFilter facebookAdminFilter = new SecurityFilter(config, "FacebookClient", "admin,securityHeaders");
        filterHelper.addFilterMapping("facebookAdminFilter", facebookAdminFilter, "/facebookadmin/*");

        ...
    }
}
```


---

### 4) Define the callback endpoint only for indirect clients (`CallbackFilter`)

For indirect clients (like Facebook), the user is redirected to an external identity provider for login and then back to the application.
Thus, a callback endpoint is required in the application. It is managed by the `CallbackFilter` which has the following behaviour:

1) the credentials are extracted from the current request to fetch the user profile (from the identity provider) which is then saved in the web session

2) finally, the user is redirected back to the originally requested url (or to the `defaultUrl`).


The following options are available:

1) `configFactory`: the class name of the factory to build the configuration (the configuration is shared across filters so it can be specified only once, but each filter can defined its own configuration if necessary) or `config`: the configuration itself

2) `defaultUrl` (optional): it's the default url after login if no url was originally requested (`/` by default)

3) `saveInSession` (optional) : it indicates whether the profile should be saved into the web session (`true` by default)

4) `multiProfile` (optional): it indicates whether multiple authentications (and thus multiple profiles) must be kept at the same time (`false` by default)

5) `renewSession` (optional): it indicates whether the web session must be renewed after login, to avoid session hijacking (`true` by default)

6) `defaultClient` (optional): it defines the default client to use to finish the login process if none is provided on the URL (not defined by default)


The filter can be defined in the `web.xml` file:

```xml
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
</filter-mapping>
```

or using CDI and the `FilterHelper`:

```java
@Named
@ApplicationScoped
public class WebConfig {

    @Inject
    private Config config;

    public void build(@Observes @Initialized(ApplicationScoped.class) ServletContext servletContext) {
        final FilterHelper filterHelper = new FilterHelper(servletContext);

        ...

        final CallbackFilter callbackFilter = new CallbackFilter(config, "/");
        callbackFilter.setRenewSession(true);
        callbackFilter.setMultiProfile(true);
        filterHelper.addFilterMapping("callbackFilter", callbackFilter, "/callback");

        ...
    }
}

```

or using dependency injection via Spring, you can define the callback filter as a `DelegatingFilterProxy` in the `web.xml` file:

```xml
<filter>
  <filter-name>callbackFilter</filter-name>
  <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
</filter>
<filter-mapping>
  <filter-name>callbackFilter</filter-name>
  <url-pattern>/callback</url-pattern>
</filter-mapping>
```
    
and the specific bean in the `application-context.xml` file:

```xml
<bean id="callbackFilter" class="org.pac4j.j2e.filter.CallbackFilter">
  <property name="defaultUrl" value="/" />
</bean>
```

---

### 5) Get the user profile (via `HttpServletRequest` or `ProfileManager`)

PAC4J takes care of populating the `HttpServletRequest` with security information.
In particular it is possible to retrieve a `Principal` via `getUserPrincipal()` (or simply its name, i.e., username or id, via `getRemoteUser()`) and checks the user's roles via `isUserInRole()`.

Alternatively, you can get the profile of the authenticated user using `profileManager.get(true)` (`false` not to use the session, but only the current HTTP request).
You can test if the user is authenticated using `profileManager.isAuthenticated()`.
You can get all the profiles of the authenticated user (if ever multiple ones are kept) using `profileManager.getAll(true)`.

Example:

```java
WebContext context = new J2EContext(request, response);
ProfileManager manager = new ProfileManager(context);
Optional<CommonProfile> profile = manager.get(true);
```

or

```java
@Named
@RequestScoped
public class ProfileView {

    @Inject
    private WebContext webContext;

    @Inject
    private ProfileManager profileManager;

    public Object getProfile() {
        return profileManager.get(true).orElse(null);
    }
}
```

The retrieved profile is at least a `CommonProfile`, from which you can retrieve the most common attributes that all profiles share. But you can also cast the user profile to the appropriate profile according to the provider used for authentication. For example, after a Facebook authentication:

```java
FacebookProfile facebookProfile = (FacebookProfile) commonProfile;
```

---

### 6) Logout (`LogoutFilter`)

The `LogoutFilter` can handle:
 
- the local logout by removing the pac4j profiles from the session (it can be used for the front-channel logout from the identity provider in case of a central logout)
- the central logout by calling the identity provider logout endpoint.


It has the following behaviour:

1) If the `localLogout` property is `true`, the pac4j profiles are removed from the web session (and the web session is destroyed if the `destroySession` property is `true`)

2) A post logout action is computed as the redirection to the `url` request parameter if it matches the `logoutUrlPattern` or to the `defaultUrl` if it is defined or as a blank page otherwise

3) If the `centralLogout` property is `true`, the user is redirected to the identity provider for a central logout and
then optionally to the post logout redirection URL (if it's supported by the identity provider and if it's an absolute URL).
If no central logout is defined, the post logout action is performed directly.


The following options are available:

1) `configFactory`: the class name of the factory to build the configuration (the configuration is shared across filters so it can be specified only once, but each filter can defined its own configuration if necessary) or `config`: the configuration itself

2) `defaultUrl` (optional): the default logout url if no `url` request parameter is provided or if the `url` does not match the `logoutUrlPattern` (not defined by default)

3) `logoutUrlPattern` (optional): the logout url pattern that the `url` parameter must match (only relative urls are allowed by default)

4) `localLogout` (optional): whether a local logout must be performed (`true` by default)

5) `destroySession` (optional):  whether we must destroy the web session during the local logout (`false` by default)

6) `centralLogout` (optional): whether a central logout must be performed (`false` by default).


It can be defined in the `web.xml` file:

```xml
<filter>
  <filter-name>logoutFilter</filter-name>
  <filter-class>org.pac4j.j2e.filter.LogoutFilter</filter-class>
  <init-param>
    <param-name>defaultUrl</param-name>
    <param-value>/urlAfterLogout</param-value>
  </init-param>
</filter>
<filter-mapping>
  <filter-name>logoutFilter</filter-name>
  <url-pattern>/logout</url-pattern>
</filter-mapping>
```

or using CDI and the `FilterHelper`:

```java
@Named
@ApplicationScoped
public class WebConfig {

    @Inject
    private Config config;

    public void build(@Observes @Initialized(ApplicationScoped.class) ServletContext servletContext) {
        final FilterHelper filterHelper = new FilterHelper(servletContext);

        ...

        final LogoutFilter logoutFilter = new LogoutFilter(config, "/?defaulturlafterlogout");
        logoutFilter.setDestroySession(true);
        filterHelper.addFilterMapping("logoutFilter", logoutFilter, "/logout");

        ...
    }
}
```

---

## Migration guide

### 2.0 -> 3.0

The `FilterHelper` can be used to programmatically define filters and mappings, using an injected `Config`.

The `WebContext` and the `ProfileManager` are automatically produced by the `Pac4jProducer` and the `HttpServletResponseProducer` (based on JSF) and can be injected wherever they are needed.

### 1.3 - > 2.0

The `ApplicationLogoutFilter` has been renamed as `LogoutFilter` and now handles both the application and identity provider logouts.

### 1.2 - > 1.3

The `RequiresAuthenticationFilter` is now named `SecurityFilter` with the `clients`, `authorizers` and `matchers` parameters instead of the previous `clientName`, `authorizerName` and `matcherName`.

The `ApplicationLogoutFilter` behaviour has slightly changed: even without any `url` request parameter, the user will be redirected to the `defaultUrl` if it has been defined.

### 1.1 -> 1.2

Authorizations are now handled by the library so the `ClientFactory` can now longer be used and is replaced by a `ConfigFactory` which builds a `Config` which gathers clients (for authentication) and authorizers (for authorizations).

The `isAjax` parameter is no longer available as AJAX requests are now automatically detected. The `stateless` parameter is no longer available as the stateless nature is held by the client itself.

The `requireAnyRole` and `requieAllRoles` parameters are no longer available and authorizers must be used instead (with the `authorizerName` parameter).

The application logout process can be managed with the `ApplicationLogoutFilter`.


## Demo

Two demo webapps: [j2e-pac4j-demo](https://github.com/pac4j/j2e-pac4j-demo) (a simple JSP/servlets demo) and [j2e-pac4j-cdi-demo](https://github.com/pac4j/j2e-pac4j-cdi-demo) (a more advanced demo using JSF and CDI) are available for tests and implements many authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...


## Release notes

See the [release notes](https://github.com/pac4j/j2e-pac4j/wiki/Release-Notes). Learn more by browsing the [j2e-pac4j Javadoc](http://www.javadoc.io/doc/org.pac4j/j2e-pac4j/4.0.0) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/3.0.0/index.html).


## Need help?

If you have any question, please use the following mailing lists:

- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)

## Development

The version 4.0.1-SNAPSHOT is under development.

Maven artifacts are built via Travis: [![Build Status](https://travis-ci.org/pac4j/j2e-pac4j.png?branch=master)](https://travis-ci.org/pac4j/j2e-pac4j) and available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j). This repository must be added in the Maven `pom.xml` file for example:

```xml
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
```

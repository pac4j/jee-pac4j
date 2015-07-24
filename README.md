## What is the j2e-pac4j library ? [![Build Status](https://travis-ci.org/pac4j/j2e-pac4j.png?branch=1.1.x)](https://travis-ci.org/pac4j/j2e-pac4j)

The **j2e-pac4j** library is a J2E multi-protocols authentication and authorization client.

It supports these 7 authentication mechanisms on client side (stateful, redirection back and forth to an identity provider for login):

1. OAuth (1.0 & 2.0)
2. CAS (1.0, 2.0, SAML, logout & proxy)
3. HTTP (form & basic auth authentications)
4. OpenID
5. SAML (2.0)
6. GAE UserService
7. OpenID Connect (1.0)

as well as stateless REST calls (direct access to the web application with credentials).

It's available under the Apache 2 license and based on the [pac4j](https://github.com/pac4j/pac4j) library.


## Providers supported

<table>
<tr><th>Provider</th><th>Protocol</th><th>Maven dependency</th><th>Client class</th><th>Profile class</th></tr>
<tr><td>CAS server</td><td>CAS</td><td>pac4j-cas</td><td>CasClient & CasProxyReceptor</td><td>CasProfile</td></tr>
<tr><td>CAS server using OAuth Wrapper</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>CasOAuthWrapperClient</td><td>CasOAuthWrapperProfile</td></tr>
<tr><td>DropBox</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>DropBoxClient</td><td>DropBoxProfile</td></tr>
<tr><td>Facebook</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>FacebookClient</td><td>FacebookProfile</td></tr>
<tr><td>GitHub</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>GitHubClient</td><td>GitHubProfile</td></tr>
<tr><td>Google</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>Google2Client</td><td>Google2Profile</td></tr>
<tr><td>LinkedIn</td><td>OAuth 1.0 & 2.0</td><td>pac4j-oauth</td><td>LinkedInClient & LinkedIn2Client</td><td>LinkedInProfile & LinkedIn2Profile</td></tr>
<tr><td>Twitter</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>TwitterClient</td><td>TwitterProfile</td></tr>
<tr><td>Windows Live</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>WindowsLiveClient</td><td>WindowsLiveProfile</td></tr>
<tr><td>WordPress</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>WordPressClient</td><td>WordPressProfile</td></tr>
<tr><td>Yahoo</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>YahooClient</td><td>YahooProfile</td></tr>
<tr><td>PayPal</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>PayPalClient</td><td>PayPalProfile</td></tr>
<tr><td>Vk</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>VkClient</td><td>VkProfile</td></tr>
<tr><td>Foursquare</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>FoursquareClient</td><td>FoursquareProfile</td></tr>
<tr><td>Bitbucket</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>BitbucketClient</td><td>BitbucketProfile</td></tr>
<tr><td>ORCiD</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>OrcidClient</td><td>OrcidProfile</td></tr>
<tr><td>Strava</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>StravaClient</td><td>StravaProfile</td></tr>
<tr><td>Web sites with basic auth authentication</td><td>HTTP</td><td>pac4j-http</td><td>BasicAuthClient</td><td>HttpProfile</td></tr>
<tr><td>Web sites with form authentication</td><td>HTTP</td><td>pac4j-http</td><td>FormClient</td><td>HttpProfile</td></tr>
<tr><td>Google - Deprecated</td><td>OpenID</td><td>pac4j-openid</td><td>GoogleOpenIdClient</td><td>GoogleOpenIdProfile</td></tr>
<tr><td>Yahoo</td><td>OpenID</td><td>pac4j-openid</td><td>YahooOpenIdClient</td><td>YahooOpenIdProfile</td></tr>
<tr><td>SAML Identity Provider</td><td>SAML 2.0</td><td>pac4j-saml</td><td>Saml2Client</td><td>Saml2Profile</td></tr>
<tr><td>Google App Engine User Service</td><td>Gae User Service Mechanism</td><td>pac4j-gae</td><td>GaeUserServiceClient</td><td>GaeUserServiceProfile</td></tr>
<tr><td>OpenID Connect Provider</td><td>OpenID Connect 1.0</td><td>pac4j-oidc</td><td>OidcClient</td><td>OidcProfile</td></tr>
</table>


## Technical description

This library has **only 5 classes** :

1. the **ClientConfiguration** class gathers all the clients configuration
2. the **ClientsConfigFilter** is an abstract J2E filter in charge of loading clients configuration
3. the **RequiresAuthenticationFilter** is a J2E filter to protect urls and requires authentication for them. The filter has a stateful or stateless mode.
4. the **CallbackFilter** is a J2E filter to handle the callback of the provider after authentication to finish the authentication process
5. the **UserUtils** is an helper class to know if the user is authenticated, his profile and log out him.

and is based on the <i>pac4j-*</i> libraries.

Learn more by browsing the [j2e-pac4j Javadoc](http://www.pac4j.org/apidocs/j2e-pac4j/index.html) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/index.html).

### Sequence diagram of the RequiresAuthenticationFilter

<img src="http://www.pac4j.org/img/pac4j-requires-authentication.jpg" />

### Sequence diagram of the CallbackFilter

<img src="http://www.pac4j.org/img/pac4j-callback.jpg" />

## How to use it ?

### Add the required dependencies

If you want to use a specific client support, you need to add the appropriate Maven dependency in the *pom.xml* file :

* for OAuth support, the *pac4j-oauth* dependency is required
* for CAS support, the *pac4j-cas* dependency is required
* for HTTP support, the *pac4j-http* dependency is required
* for OpenID support, the *pac4j-openid* dependency is required
* for SAML support, the *pac4j-saml* dependency is required
* for Google App Engine support, the *pac4j-gae* dependency is required
* for OpenID Connect support, the *pac4j-oidc* dependency is required.

For example, to add OAuth support, add the following XML snippet :

    <dependency>
      <groupId>org.pac4j</groupId>
      <artifactId>pac4j-oauth</artifactId>
      <version>1.7.0</version>
    </dependency>

As these snapshot dependencies are only available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j/), the appropriate repository must be added in the *pom.xml* file also :

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

### Define the clients

All the clients used to communicate with various providers (Facebook, Twitter, a CAS server...) must be defined in a specific class implementing the *org.pac4j.core.client.ClientsFactory* interface. For example :

    public class MyClientsFactory implements ClientsFactory {
      
      @Override
      public Clients build(final Object env) {
        final FacebookClient facebookClient = new FacebookClient("fbkey", "fbsecret");
        final TwitterClient twitterClient = new TwitterClient("twkey", "twsecret");
        // HTTP
        final FormClient formClient = new FormClient("http://localhost:8080/theForm.jsp", new SimpleTestUsernamePasswordAuthenticator());
        final BasicAuthClient basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());        
        // CAS
        final CasClient casClient = new CasClient();
        casClient.setCasLoginUrl("http://localhost:8888/cas/login");        
        // OpenID
        final GoogleOpenIdClient googleOpenIdClient = new GoogleOpenIdClient();
        final Clients clients = new Clients("http://localhost:8080/callback", facebookClient, twitterClient, formClient, basicAuthClient, casClient, googleOpenIdClient);
        return clients;
      }
    }
    
### Choose between stateful or stateless mode

Pac4j was initially designed to provide authentication flows for web applications. This means it relies on a session concept and on HTTP redirections: this is the stateful mode and it is activated by default.
In this mode, you need to configure what we call the callback filter in order to finish the authentication process.

Since **j2e-pac4j version 1.1.0**, we support now a stateless mode. This can be typically used to protect REST WS where a single HTTP call must be enough to retrieve the resource.
A good example of stateless authentication is the basic-auth method where the browser includes in all requests the HTTP Authorization header with the login and password base64 encoded.

## I. stateful mode

#### Define the "callback filter"

To handle callback from providers, you need to define the appropriate J2E filter and its mapping :

    <filter>
      <filter-name>CallbackFilter</filter-name>
      <filter-class>org.pac4j.j2e.filter.CallbackFilter</filter-class>
      <init-param>
      	<param-name>clientsFactory</param-name>
      	<param-value>org.leleuj.config.MyClientsFactory</param-value>
      </init-param>
      <init-param>
      	<param-name>defaultUrl</param-name>
      	<param-value>/</param-value>
      </init-param>
    </filter>
    <filter-mapping>
      <filter-name>CallbackFilter</filter-name>
      <url-pattern>/callback</url-pattern>
      <dispatcher>REQUEST</dispatcher>
    </filter-mapping>

#### Protect the urls

You can protect your urls and force the user to be authenticated by a client by using the appropriate filter and mapping. Key parameters are all the clients and the specific client (*clientName*) used by this filter.  
For example, for Facebook :

    <filter>
      <filter-name>FacebookFilter</filter-name>
      <filter-class>org.pac4j.j2e.filter.RequiresAuthenticationFilter</filter-class>
      <init-param>
       	<param-name>clientsFactory</param-name>
       	<param-value>org.leleuj.config.MyClientsFactory</param-value>
      </init-param>
      <init-param>
       	<param-name>clientName</param-name>
       	<param-value>FacebookClient</param-value>
      </init-param>
    </filter>
    <filter-mapping>
      <filter-name>FacebookFilter</filter-name>
      <url-pattern>/facebook/*</url-pattern>
      <dispatcher>REQUEST</dispatcher>
    </filter-mapping>

#### Get redirection urls

You can also explicitely compute a redirection url to a provider for authentication by using the *getRedirectionUrl* method and the *ClientsConfiguration* class. For example with Facebook :

    <%
	  WebContext context = new J2EContext(request, response);
	  Clients client = ClientsConfiguration.getClients();
	  FacebookClient fbClient = (FacebookClient) client.findClient("FacebookClient");
	  String redirectionUrl = Client.getRedirectionUrl(context, false, false);
	%>

#### Get the user profile

After successful authentication, you can test if the user is authenticated using ```UserUtils.isAuthenticated()``` or get the user profile using ```UserUtils.getUserProfile()```.

The profile returned is a *CommonProfile*, from which you can retrieve the most common properties that all profiles share. 
But you can also cast the user profile to the appropriate profile according to the provider used for authentication.
For example, after a Facebook authentication :
 
    // facebook profile
    FacebookProfile facebookProfile = (FacebookProfile) commonProfile;

Or for all the OAuth 1.0/2.0 profiles, to get the access token :
    
    OAuth10Profile oauthProfile = (OAuth10Profile) commonProfile
    String accessToken = oauthProfile.getAccessToken();
    // or
    String accessToken = facebookProfile.getAccessToken();

## II. Stateless mode

In this mode, you need just to protect your resources with the `ClientAuthenticationFilter` filter and configure the `stateless` parameter to true.

    <filter>
      <filter-name>StatelessBasicAuthFilter</filter-name>
      <filter-class>org.pac4j.j2e.filter.RequiresAuthenticationFilter</filter-class>
      <init-param>
       	<param-name>clientsFactory</param-name>
       	<param-value>org.leleuj.config.MyClientsFactory</param-value>
      </init-param>
      <init-param>
       	<param-name>clientName</param-name>
       	<param-value>BasicAuthClient</param-value>
      </init-param>
      <init-param>
       	<param-name>stateless</param-name>
       	<param-value>true</param-value>
      </init-param>
    </filter>
    <filter-mapping>
      <filter-name>StatelessBasicAuthFilter</filter-name>
      <url-pattern>/rest-basic-auth/*</url-pattern>
      <dispatcher>REQUEST</dispatcher>
    </filter-mapping>

#### Get the user profile

After successful authentication, you can test if the user is authenticated using ```UserUtils.isAuthenticated()``` or get the user profile using ```UserUtils.getUserProfile()```.

### Handling authorization

When you protect a resource with the `ClientAuthenticationFilter`, you can also restrict its access to predefined roles depending on the user profiles.
You can use two parameters for that:

1. **requireAnyRole**
2. **requireAllRoles**

For example if you want to restrict the access to the user having the role **ROLE_ADMIN**:

    <filter>
      <filter-name>StatelessBasicAuthFilter</filter-name>
      <filter-class>org.pac4j.j2e.filter.RequiresAuthenticationFilter</filter-class>
      <init-param>
       	<param-name>clientsFactory</param-name>
       	<param-value>org.leleuj.config.MyClientsFactory</param-value>
      </init-param>
      <init-param>
       	<param-name>clientName</param-name>
       	<param-value>BasicAuthClient</param-value>
      </init-param>
      <init-param>
       	<param-name>stateless</param-name>
       	<param-value>true</param-value>
      </init-param>
      <init-param>
        <param-name>requireAnyRole</param-name>
        <param-value>ROLE_ADMIN</param-value>
      </init-param>
    </filter>

Of course you need also to configure a correct AuthorizationGenerator (see [authorizations](https://github.com/pac4j/pac4j#authorizations)). 

### Demo

A demo with Facebook, Twitter, CAS, form authentication and basic auth authentication providers is available with [j2e-pac4j-demo](https://github.com/pac4j/j2e-pac4j-demo).

## Versions

The current version **1.1.2-SNAPSHOT** is under development. It's available on the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j) as a Maven dependency :

The last released version is the **1.1.1** :

    <dependency>
        <groupId>org.pac4j</groupId>
        <artifactId>j2e-pac4j</artifactId>
        <version>1.1.0</version>
    </dependency>

See the [release notes](https://github.com/pac4j/j2e-pac4j/wiki/Release-Notes).


## Contact

If you have any question, please use the following mailing lists :
- [pac4j users](https://groups.google.com/forum/?hl=en#!forum/pac4j-users)
- [pac4j developers](https://groups.google.com/forum/?hl=en#!forum/pac4j-dev)

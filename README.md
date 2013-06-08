## What is the j2e-pac4j library ?

The **j2e-pac4j** library is a J2E multi-protocols client.

It supports these 4 protocols on client side :

1. OAuth (1.0 & 2.0)
2. CAS (1.0, 2.0, SAML, logout & proxy)
3. HTTP (form & basic auth authentications)
4. OpenID.

It's available under the Apache 2 license and based on my [pac4j](https://github.com/leleuj/pac4j) library.


## Providers supported

<table>
<tr><th>Provider</th><th>Protocol</th><th>Maven dependency</th><th>Client class</th><th>Profile class</th></tr>
<tr><td>CAS server</td><td>CAS</td><td>pac4j-cas</td><td>CasClient & CasProxyReceptor</td><td>CasProfile</td></tr>
<tr><td>CAS server using OAuth Wrapper</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>CasOAuthWrapperClient</td><td>CasOAuthWrapperProfile</td></tr>
<tr><td>DropBox</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>DropBoxClient</td><td>DropBoxProfile</td></tr>
<tr><td>Facebook</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>FacebookClient</td><td>FacebookProfile</td></tr>
<tr><td>GitHub</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>GitHubClient</td><td>GitHubProfile</td></tr>
<tr><td>Google</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>Google2Client</td><td>Google2Profile</td></tr>
<tr><td>LinkedIn</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>LinkedInClient</td><td>LinkedInProfile</td></tr>
<tr><td>Twitter</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>TwitterClient</td><td>TwitterProfile</td></tr>
<tr><td>Windows Live</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>WindowsLiveClient</td><td>WindowsLiveProfile</td></tr>
<tr><td>WordPress</td><td>OAuth 2.0</td><td>pac4j-oauth</td><td>WordPressClient</td><td>WordPressProfile</td></tr>
<tr><td>Yahoo</td><td>OAuth 1.0</td><td>pac4j-oauth</td><td>YahooClient</td><td>YahooProfile</td></tr>
<tr><td>Web sites with basic auth authentication</td><td>HTTP</td><td>pac4j-http</td><td>BasicAuthClient</td><td>HttpProfile</td></tr>
<tr><td>Web sites with form authentication</td><td>HTTP</td><td>pac4j-http</td><td>FormClient</td><td>HttpProfile</td></tr>
<tr><td>MyOpenId</td><td>OpenID</td><td>pac4j-openid</td><td>MyOpenIdClient</td><td>MyOpenIdProfile</td></tr>
</table>


## Technical description

This library has **only 6 classes** :

1. the **ClientConfiguration** class gathers all the clients configuration
2. the **ClientFactory** is the interface to implement to define the clients
3. the **ClientsConfigFilter** is an abstract J2E filter in charge of loading clients configuration
4. the **RequiresAuthenticationFilter** is a J2E filter to protect urls and requires authentication for them (redirection to the appropriate provider)
5. the **CallbackFilter** is a J2E filter to handle the callback of the provider after authentication to finish the authentication process
6. the **UserUtils** is an helper class to know if the user is authenticated, his profile and log out him.

and is based on the <i>pac4j-*</i> libraries.

Learn more by browsing the [j2e-pac4j Javadoc](http://www.pac4j.org/apidocs/j2e-pac4j/index.html) and the [pac4j Javadoc](http://www.pac4j.org/apidocs/pac4j/index.html).


## How to use it ?

### Add the required dependencies

If you want to use a specific client support, you need to add the appropriate Maven dependency in the *pom.xml* file :

* for OAuth support, the *pac4j-oauth* dependency is required
* for CAS support, the *pac4j-cas* dependency is required
* for HTTP support, the *pac4j-http* dependency is required
* for OpenID support, the *pac4j-openid* dependency is required.

For example, to add OAuth support, add the following XML snippet :

```<dependency>
  <groupId>org.pac4j</groupId>
  <artifactId>pac4j-oauth</artifactId>
  <version>1.4.1-SNAPSHOT</version>
</dependency>
```

As these snapshot dependencies are only available in the [Sonatype snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/org/pac4j/), the appropriate repository must be added in the *pom.xml* file also :

    &lt;repositories&gt;
      &lt;repository&gt;
        &lt;id&gt;sonatype-nexus-snapshots&lt;/id&gt;
        &lt;name&gt;Sonatype Nexus Snapshots&lt;/name&gt;
        &lt;url&gt;https://oss.sonatype.org/content/repositories/snapshots&lt;/url&gt;
        &lt;releases&gt;
          &lt;enabled&gt;false&lt;/enabled&gt;
        &lt;/releases&gt;
        &lt;snapshots&gt;
          &lt;enabled&gt;true&lt;/enabled&gt;
        &lt;/snapshots&gt;
      &lt;/repository&gt;
    &lt;/repositories&gt;

### Define the clients

All the clients used to communicate with various providers (Facebook, Twitter, a CAS server...) must be defined in a specific class implementing the *org.pac4j.j2e.configuration.ClientsFactory* interface. For example :

    public class MyClientsFactory implements ClientsFactory {
      
      @Override
      public Clients build() {
        final FacebookClient facebookClient = new FacebookClient("fbkey", "fbsecret");
        final TwitterClient twitterClient = new TwitterClient("twkey", "twsecret");
        // HTTP
        final FormClient formClient = new FormClient("http://localhost:8080/theForm.jsp", new SimpleTestUsernamePasswordAuthenticator());
        final BasicAuthClient basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());        
        // CAS
        final CasClient casClient = new CasClient();
        casClient.setCasLoginUrl("http://localhost:8888/cas/login");        
        // OpenID
        final MyOpenIdClient myOpenIdClient = new MyOpenIdClient();
        final Clients clients = new Clients("http://localhost:8080/callback", facebookClient, twitterClient, formClient, basicAuthClient, casClient, myOpenIdClient);
        return clients;
      }
    }
    
### Define the "callback filter"

To handle callback from providers, you need to define the appropriate J2E filter and its mapping :

    &lt;filter&gt;
      &lt;filter-name&gt;CallbackFilter&lt;/filter-name&gt;
      &lt;filter-class&gt;org.pac4j.j2e.filter.CallbackFilter&lt;/filter-class&gt;
      &lt;init-param&gt;
      	&lt;param-name&gt;clientsFactory</param-name&gt;
      	&lt;param-value&gt;org.leleuj.config.MyClientsFactory</param-value&gt;
      &lt;/init-param&gt;
      &lt;init-param&gt;
      	&lt;param-name&gt;defaultUrl&lt;/param-name&gt;
      	&lt;param-value&gt;/&lt;/param-value&gt;
      &lt;/init-param&gt;
    &lt;/filter&gt;
    &lt;filter-mapping&gt;
      &lt;filter-name&gt;CallbackFilter&lt;/filter-name&gt;
      &lt;url-pattern&gt;/callback&lt;/url-pattern&gt;
      &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
    &lt;/filter-mapping&gt;

### Protect the urls

You can protect your urls and force the user to be authenticated by a client by using the appropriate filter and mapping. Key parameters are all the clients and the specific client (*clientName*) used by this filter.  
For example, for Facebook :

    &lt;filter&gt;
      &lt;filter-name&gt;FacebookFilter&lt;/filter-name&gt;
      &lt;filter-class&gt;org.pac4j.j2e.filter.RequiresAuthenticationFilter&lt;/filter-class&gt;
      &lt;init-param&gt;
       	&lt;param-name&gt;clientsFactory&lt;/param-name&gt;
       	&lt;param-value&gt;org.leleuj.config.MyClientsFactory&lt;/param-value&gt;
      &lt;/init-param&gt;
      &lt;init-param&gt;
       	&lt;param-name&gt;clientName&lt;/param-name&gt;
       	&lt;param-value&gt;FacebookClient&lt;/param-value&gt;
      &lt;/init-param&gt;
    &lt;/filter&gt;
    &lt;filter-mapping&gt;
      &lt;filter-name&gt;FacebookFilter&lt;/filter-name&gt;
      &lt;url-pattern&gt;/facebook/*&lt;/url-pattern&gt;
      &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
    &lt;/filter-mapping&gt;


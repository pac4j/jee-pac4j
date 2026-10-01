<p align="center">
  <img src="https://pac4j.github.io/pac4j/img/logo-j2e.png" width="300" />
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/org.pac4j/jakartaee-pac4j"><img src="https://img.shields.io/maven-central/v/org.pac4j/jakartaee-pac4j?label=Maven%20Central" alt="Maven Central" /></a>
  <a href="https://github.com/pac4j/jee-pac4j/actions/workflows/ci.yml"><img src="https://github.com/pac4j/jee-pac4j/actions/workflows/ci.yml/badge.svg" alt="Build status" /></a>
  <img src="https://img.shields.io/badge/Java-17%2B-blue" alt="Java 17+" />
  <img src="https://img.shields.io/badge/Jakarta%20EE-9%2B%20%7C%20Java%20EE%208%2B-blue" alt="Jakarta EE 9+ | Java EE 8+" />
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache%202.0-blue" alt="Apache 2 license" /></a>
</p>

> `jee-pac4j` is the Jakarta EE / Java EE implementation of **[pac4j](https://github.com/pac4j/pac4j)**, the security engine for Java.
> If it is useful to you, please ⭐ **[star pac4j on GitHub](https://github.com/pac4j/pac4j)**: it helps other developers discover it!

The `jee-pac4j` project is an **easy and powerful security library for JEE web applications and web services** which supports authentication and authorization, but also logout and advanced features like session fixation and CSRF protection.
It's based on the **[pac4j security engine](https://github.com/pac4j/pac4j)**. It's available under the Apache 2 license.

| jee-pac4j    | Module for JavaEE webapp | Module for Jakarta EE webapp | JDK | pac4j | Usage of Lombok | Status           |
|--------------|--------------------------|-----------------------------|-----|-------|-----------------|------------------|
| version >= 8 | javaee-pac4j             | jakartaee-pac4j             | 17  | v6    | Yes             | Production ready |
| version >= 7 | javaee-pac4j             | jakartaee-pac4j             | 11  | v5    | No              | Production ready |
| version >= 6 | jee-pac4j                |                             | 11  | v5    | No              | Production ready |
| version >= 5 | jee-pac4j                |                             | 8   | v4    | No              | Production ready |

[**Main concepts and components:**](https://www.pac4j.org/docs/main-concepts-and-components.html)

1) A [**client**](https://www.pac4j.org/docs/clients.html) represents an authentication mechanism. It performs the login process and returns a user profile. An indirect client is for web applications authentication while a direct client is for web services authentication:

&#9656; OpenID Connect - SAML - CAS - OAuth - HTTP - Kerberos - LDAP - SQL - JWT - MongoDB - IP address - REST API

2) An [**authorizer**](https://www.pac4j.org/docs/authorizers.html) is meant to check authorizations on the authenticated user profile(s) or on the current web context:

&#9656; Roles - Anonymous / remember-me / (fully) authenticated - Profile type, attribute -  CORS - CSRF - Security headers - IP address, HTTP method

3) A [**matcher**](https://www.pac4j.org/docs/matchers.html) defines whether the `SecurityFilter` must be applied and can be used for additional web processing

4) The `SecurityFilter` protects an url by checking that the user is authenticated and that the authorizations are valid, according to the clients and authorizers configuration. If the user is not authenticated, it performs authentication for direct clients or starts the login process for indirect clients

5) The `CallbackFilter` finishes the login process for an indirect client

6) The `LogoutFilter` logs out the user from the application and triggers the logout at the identity provider level

7) The `JEEContext` and the `ProfileManager` components can be injected

8) The `FilterHelper` handles the filters and their related mappings.


## Usage

### 1) [Add the required dependencies](https://github.com/pac4j/jee-pac4j/wiki/Dependencies)

### 2) Define:

### - the [security configuration](https://github.com/pac4j/jee-pac4j/wiki/Security-configuration)
### - the [callback configuration](https://github.com/pac4j/jee-pac4j/wiki/Callback-configuration), only for web applications
### - the [logout configuration](https://github.com/pac4j/jee-pac4j/wiki/Logout-configuration)

### 3) [Apply security](https://github.com/pac4j/jee-pac4j/wiki/Apply-security)

### 4) [Get the authenticated user profiles](https://github.com/pac4j/jee-pac4j/wiki/Get-the-authenticated-user-profiles)


### CDI outside JSF

`HttpServletResponseFilter` exposes the current servlet response to the CDI producers, so `WebContext`
and `ProfileManager` can also be injected in servlet and REST requests. The filter is discovered
through `@WebFilter` when the library is packaged in `WEB-INF/lib` and annotation scanning is enabled.
If scanning is disabled (for example, with `metadata-complete="true"`), register this filter explicitly
before components which inject the response or a pac4j web context. Existing JSF applications can
still use the FacesContext fallback.

To allow a servlet behind a filter to call `startAsync()`, enable asynchronous support when registering
that filter. `FilterHelper` enables it automatically for mappings containing `DispatcherType.ASYNC`,
or accepts an explicit Boolean among its parameters:

```java
filterHelper.addFilterMapping("security", securityFilter, true, "/protected/*");
```

All filters and the servlet in the request chain must support asynchronous operations.


## Demos

Two demo webapps: [jee-pac4j-demo](https://github.com/pac4j/jee-pac4j-demo) (a simple JSP/servlets demo) and [jee-pac4j-cdi-demo](https://github.com/pac4j/jee-pac4j-cdi-demo) (a more advanced demo using JSF and CDI) are available for tests and implements many authentication mechanisms: Facebook, Twitter, form, basic auth, CAS, SAML, OpenID Connect, JWT...


## Versions

The latest released version is the [![Maven Central](https://img.shields.io/maven-central/v/org.pac4j/jee-pac4j-parent.svg)](https://repo1.maven.org/maven2/org/pac4j/jee-pac4j-parent/). The [next version](https://github.com/pac4j/jee-pac4j/wiki/Next-version) is under development.

See the [release notes](https://github.com/pac4j/jee-pac4j/wiki/Release-Notes).

See the [migration guide](https://github.com/pac4j/jee-pac4j/wiki/Migration-guide) as well.


## Need help?

You can use the [mailing lists](https://www.pac4j.org/mailing-lists.html) or the [commercial support](https://www.pac4j.org/commercial-support.html).

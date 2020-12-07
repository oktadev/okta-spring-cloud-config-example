# Okta + Spring Cloud Config Example

This example shows how to construct a project in which several Spring Boot microservices can read their configuration settings from a central configuration server using Spring Cloud Config.

**Prerequisites**: [Java 11](https://adoptopenjdk.net)

> [Okta](https://developer.okta.com/) has Authentication and User Management APIs that reduce development time with instant-on, scalable user infrastructure. Okta's intuitive API and expert support make it easy for developers to authenticate, manage and secure users and roles in any application.

* [Getting Started](#getting-started)
* [Links](#links)
* [Help](#help)
* [License](#license)

## Getting Started

Clone this repository:

```bash
git clone https://github.com/oktadeveloper/okta-spring-cloud-config-example.git
cd okta-spring-cloud-config-example
```

This will get a copy of the project installed locally. Open the project in your IDE and update `src/main/resources/application.properties` with the following key-value pairs:

```properties
server.port=8888
spring.cloud.config.server.native.search-locations=/path/to/config/folder
spring.security.user.name=configUser
spring.security.user.password=configPass
```

The property `spring.cloud.config.server.native.search-locations` is the location where you store your configuration files. **Replace the value with a folder on your filesystem where these files will be saved.** For example, `file://${user.home}/config`.

Normally your configuration files would be stored in a remote location, for example, a GitHub repository or an Amazon S3 bucket. For instructions on how to store your config files in a git repository, see [this section in the Spring Cloud Config documentation](https://cloud.spring.io/spring-cloud-config/reference/html/#_git_backend). To keep this tutorial simple, you will use the "native" filesystem option above.

### Create an OpenID Connect Application in Okta

Sign up for a free developer account at <https://developer.okta.com/signup>. This will be used to secure your microservices using OAuth 2.0 and OpenID Connect (OIDC). After signing up, log in to your Okta account at `https://your-okta-domain.okta.com`.

Go to **Applications** >  **Add Application**.

Select **Web** and click **Next**.

In **Application Settings** fill in the following values:

- **Name**: `My Spring Cloud App` (or another name of your choosing)
- **Base URIs**: `http://localhost:8001` and `http://localhost:8002`
- **Login Redirect URIs**: `http://localhost:8001/login/oauth2/code/okta` and `http://localhost:8002/login/oauth2/code/okta`
- **Logout Redirect URIs**: `http://localhost:8001` and `http://localhost:8002`
- **Group Assignments**: `Everyone` (should be selected by default)
- **Grant type allowed**: `Authorization Code`

Click **Done**.

Take note of the values for **Client ID** and **Client secret**. These will be necessary for securing your microservices with OAuth 2.0.

### Configure Security for Your Microservices Architecture

See [repo's blog post](https://developer.okta.com/blog/2020/12/07/spring-cloud-config) for how to configure your security settings with Spring Cloud Config.

## Links

This example uses the following open source libraries from Okta:

* [Okta Spring Boot Starter](https://github.com/okta/okta-spring-boot)

## Help

Please post any questions as comments on the [blog post](https://developer.okta.com/blog/2020/12/07/spring-cloud-config), or visit our [Okta Developer Forums](https://devforum.okta.com/).

## License

Apache 2.0, see [LICENSE](LICENSE).

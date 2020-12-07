# Okta + Spring Cloud Config Example

This example shows how to construct a project in which several Spring Boot microservices can read their configuration settings from a central configuration server using Spring Cloud Config.

**Prerequisites**
* [Java 11](https://adoptopenjdk.net)

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

## Create an OpenID Connect Application in Okta

Sign up for a free developer account at <https://developer.okta.com/signup>. This will be used to secure your microservices using OAuth 2.0 and OpenID Connect (OIDC). After signing up, log in to your Okta account at `\https://your-okta-domain.okta.com`.

Click **Applications** in the top nav menu.

{% img blog/spring-cloud-config-okta/01.png alt:"Click the Applications button" width:"800" %}{: .center-image }

Click **Add Application**.

{% img blog/spring-cloud-config-okta/02.png alt:"Click Add Application" width:"800" %}{: .center-image }

Select **Web** and click **Next**.

{% img blog/spring-cloud-config-okta/03.png alt:"Select Web and click Next" width:"800" %}{: .center-image }

In **Application Settings** fill in the following values:

- **Name**: `My Spring Cloud App` (or another name of your choosing)
- **Base URIs**: `http://localhost:8001` and `http://localhost:8002`
- **Login Redirect URIs**: `http://localhost:8001/login/oauth2/code/okta` and `http://localhost:8002/login/oauth2/code/okta`
- **Logout Redirect URIs**: `http://localhost:8001` and `http://localhost:8002`
- **Group Assignments**: `Everyone` (should be selected by default)
- **Grant type allowed**: `Authorization Code`

{% img blog/spring-cloud-config-okta/04_2.png alt:"Application settings" width:"800" %}{: .center-image }

Click **Done**.

{% img blog/spring-cloud-config-okta/05.png alt:"Click Done" width:"800" %}{: .center-image }

Take note of the values for **Client ID** and **Client secret**. These will be necessary for securing your microservices with OAuth 2.0.

{% img blog/spring-cloud-config-okta/06.png alt:"Client ID and Secret" width:"800" %}{: .center-image }

## Configure Security for Your Microservices Architecture

Next, you'll need to create the configuration files which will be used by your microservices. Create or open the directory specified above for `spring.cloud.config.server.native.search-locations` and add the following files:

`service-one.yml`
```yaml
server:
  port: 8001

okta:
  oauth2:
    issuer: https://YOUR_DOMAIN.okta.com/oauth2/default
    clientId: YOUR_CLIENT_ID
    clientSecret: YOUR_CLIENT_SECRET

management:
  endpoints:
    web:
      exposure:
        include: "refresh"
```

`service-one-profile1.yml`
```yaml
hello:
  message: "Service One Profile One"
```

`service-one-profile2.yml`
```yaml
hello:
  message: "Service One Profile Two"
```

`service-two.yml`
```yaml
server:
  port: 8002

okta:
  oauth2:
    issuer: https://YOUR_DOMAIN.okta.com/oauth2/default
    clientId: YOUR_CLIENT_ID
    clientSecret: YOUR_CLIENT_SECRET
    
management:
  endpoints:
    web:
      exposure:
        include: "refresh"
```

`service-two-profile1.yml`
```yaml
hello:
  message: "Service Two Profile One"
```

`service-two-profile2.yml`
```yaml
hello:
  message: "Service Two Profile Two"
```

* Replace `YOUR_DOMAIN` with your Okta account's domain. It should look something like `dev-0123456`, e.g. `https://dev-0123456.okta.com/oauth2/default`,
* Replace `YOUR_CLIENT_ID` with the value of `Client ID` noted earlier under `My Application > General > Client Credentials`.
* Replace `YOUR_CLIENT_SECRET` with the value of `Client Secret` noted earlier under `My Application > General > Client Credentials`.

Enter your config server's project directory and run the application:

```shell
cd config-server
./mvnw spring-boot:run -Dspring-boot.run.profiles=native
```

The `native` profile tells the application to server configuration files from the filesystem directory you populated above.

## Run Spring Boot Microservice #1

Let's run the first of the two microservices.

Enter the directory for `service-one` and run the application with `profile1` set:

```shell
cd ../service-one
./mvnw spring-boot:run -Dspring-boot.run.profiles=profile1
```

Open a browser and navigate to `http://localhost:8001/secure`. You should be redirected to Okta for authentication. After successfully authenticating, you should see the following message:

```text
Service One Profile One
```

This is the same message defined in the `service-one-profile.yml` file you created earlier. Neat!

Next, you will switch your microservice's active profile to `profile2` and observe a different message. Stop your application and re-run with `profile2` active:

```shell
./mvnw spring-boot:run -Dspring-boot.run.profiles=profile2
```

Navigate to `http://localhost:8001/secure`. You should now see the message defined in `service-one-profile2.yml`:

```text
Service One Profile Two
```

## Refresh the Configuration in Your Spring Cloud Config Server

Spring Cloud Config provides the ability to "live" reload your service's configuration without stopping or re-deploying. Examine the `@RefreshScope` annotation to your REST controller:

```java
import org.springframework.cloud.context.config.annotation.RefreshScope;
...

@RefreshScope
@RestController
@RequestMapping("/secure")
public static class SecureController {

    @Value("${hello.message}")
    private String helloMessage;

    @GetMapping
    public String secure(Principal principal) {
        return helloMessage;
    }
}
```

When this annotation is applied to a Spring component (i.e., a `@Component`, `@Service`, `@RestController`, etc.), the component is re-created when a configuration refresh occurs, in this case giving an updated value for `${hello.message}`.

You can refresh an application's configuration by including the **Spring Boot Actuator** dependency, exposing the `/actuator/refresh` endpoint, and sending an empty `POST` request.

Open your configuration file at `/path/to/config/folder/service-one-profile1.yml` and edit the message:

`service-one-profile1.yml`
```yaml
hello:
  message: "Things have changed"
```

Save the file and refresh the page at `http://localhost:8001/secure`. Note that the message has not changed yet and still says `Service One Profile One`. To have your application receive the updated configuration, you must call the `/actuator/refresh` endpoint:

```shell
curl -u serviceOneUser:serviceOnePassword -X POST http://localhost:8001/actuator/refresh
```

Refresh the page at `http://localhost:8001/secure`, and you should see the updated message!

## Run Spring Boot Microservice #2

Enter the directory for `service-two` and run the application:

```shell
cd ../service-two
./mvnw spring-boot:run -Dspring-boot.run.profiles=profile1
```

Navigate to `http://localhost:8002/secure` and authenticate with Okta. When you are redirected back to your application you will see the welcome message for `service-two`:

```text
Service Two Profile One
```

You're done! You've created two microservices, secured by Okta and OAuth 2.0, which receive their configuration settings from a shared Spring Cloud Config server. Very cool! 😎

## Learn more about Spring Cloud Config and Microservices

For in-depth examples and use cases not covered in this tutorial, see Spring's official documentation for Spring Cloud Config [here](https://cloud.spring.io/spring-cloud-config/reference/html/).

Check out these other articles on integrating Spring Boot with Okta:

- [A Quick Guide to OAuth 2.0 with Spring Security](https://developer.okta.com/blog/2019/03/12/oauth2-spring-security-guide)
- [Easy Single Sign-On with Spring Boot and OAuth 2.0](https://developer.okta.com/blog/2019/05/02/spring-boot-single-sign-on-oauth-2)
- [Use PKCE with OAuth 2.0 and Spring Boot for Better Security](https://developer.okta.com/blog/2020/01/23/pkce-oauth2-spring-boot)
- [Spring Security SAML and Database Authentication](https://developer.okta.com/blog/2020/10/14/spring-security-saml-database-authentication)

Follow us on social media ([Twitter](https://twitter.com/oktadev), [Facebook](https://www.facebook.com/oktadevelopers), [LinkedIn](https://www.linkedin.com/company/oktadev/)) to know when we've posted more articles like this, and please [subscribe](https://youtube.com/c/oktadev?sub_confirmation=1) to our [YouTube channel](https://youtube.com/c/oktadev) for tutorials and screencasts!

_We're also streaming on Twitch, [follow us](https://www.twitch.tv/oktadev) to be notified when we're live._

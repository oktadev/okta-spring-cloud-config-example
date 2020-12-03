package com.okta.dev.servicetwo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@SpringBootApplication
public class ServiceTwoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceTwoApplication.class, args);
    }

    @Order(1)
    @Configuration
    public static class ActuatorSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .antMatcher("/actuator/*")
                .authorizeRequests()
                .antMatchers("/actuator/*").authenticated()
                .and()
                .httpBasic();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                .withUser("serviceTwoUser")
                .password("{noop}serviceTwoPassword")
                .roles("USER");
        }
    }

    @Order(2)
    @Configuration
    public static class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .oauth2Login();
        }
    }

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
}

package com.firstProject.Homely_Spices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.firstProject.Homely_Spices.CustomAuthenticationSuccessHandler;

import com.firstProject.Homely_Spices.service.UserService;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    private final CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    private final UserService customUserDetailsService;

    public SecurityConfig(UserService customUserDetailsService, CustomOAuth2UserService customOAuth2UserService) {
        this.customUserDetailsService = customUserDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/images/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/login","/signup","/home","/products","/productDetails/**","/api/**","/verifyOtp","/resendOtp","/filter","/forgetPassword","/forget-password","/verifyotp-resetpassword").permitAll()
                        .requestMatchers(HttpMethod.POST, "/verify-and-create-password").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated()

                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler())
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/home")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        ))

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            System.out.println("AuthenticationFailureHandler invoked. Exception: " + exception.getClass().getName()); // Debug log

            String errorMessage = "Invalid login credentials"; // Default error message

            if (exception instanceof InternalAuthenticationServiceException) {
                // Get the root cause of the exception
                Throwable rootCause = exception.getCause();
                if (rootCause instanceof LockedException) {
                    // Use the block reason from LockedException
                    errorMessage = rootCause.getMessage();
                    if (errorMessage == null || errorMessage.isEmpty()) {
                        errorMessage = "Your account has been blocked.";
                    }
                    System.out.println("Blocked user detected. Error message: " + errorMessage); // Debug log
                } else {
                    // Log other root causes
                    System.err.println("Root cause of InternalAuthenticationServiceException: " + rootCause.getMessage());
                    rootCause.printStackTrace();
                }
            } else {
                // Log other exceptions
                System.err.println("Authentication error: " + exception.getMessage());
                exception.printStackTrace();
            }

            // Set the error message in the session
            request.getSession().setAttribute("errorMessage", errorMessage);
            System.out.println("Setting errorMessage in session: " + errorMessage); // Debug log
            response.sendRedirect("/login?error");
        };
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }
}

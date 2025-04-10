package org.fbonacina.customerorders.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final UnauthorizedHandler unauthorizedHandler;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter, UnauthorizedHandler unauthorizedHandler) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.unauthorizedHandler = unauthorizedHandler;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/login")
                    .permitAll()
                    .requestMatchers("/api/v1/**")
                    .authenticated())
        .exceptionHandling(configurer -> configurer.authenticationEntryPoint(unauthorizedHandler))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}

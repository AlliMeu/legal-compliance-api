package com.allisonmeunier.legalcompliance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * GET  /cases/**             -> needs authority SCOPE_case:read
 * POST /cases/{ref}/access   -> needs authority SCOPE_case:write
 *
 * Wrong scope   -> 403
 * No auth       -> 401
 * Correct scope -> success
 *
 * Two demo users are provisioned below (paralegal = read only, attorney = read +
 * write) purely so the app is runnable end to end without standing up a real
 * identity provider. The actual authorization rules are what's under test.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/cases/**").hasAuthority("SCOPE_case:read")
                        .requestMatchers(HttpMethod.POST, "/cases/*/access").hasAuthority("SCOPE_case:write")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        UserDetails paralegal = User.withUsername("paralegal")
                .password(encoder.encode("demo"))
                .authorities("SCOPE_case:read")
                .build();
        UserDetails attorney = User.withUsername("attorney")
                .password(encoder.encode("demo"))
                .authorities("SCOPE_case:read", "SCOPE_case:write")
                .build();
        return new InMemoryUserDetailsManager(paralegal, attorney);
    }
}

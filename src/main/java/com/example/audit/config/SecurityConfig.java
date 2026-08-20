package com.example.audit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${audit.admin.password}")
    private String adminPassword;

    @Value("${audit.user.password}")
    private String userPassword;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/audit/events").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/audit/events").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/audit/events/*/redact").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/audit/archive").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/audit/export").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/audit/verify").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/audit/compliance/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode(adminPassword))
            .roles("ADMIN")
            .build();
            
        UserDetails user = User.builder()
            .username("service_client")
            .password(passwordEncoder.encode(userPassword))
            .roles("USER")
            .build();
            
        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
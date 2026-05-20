package com.love.Backend.config;

import com.love.Backend.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Profile("dev")
public class SpringSecurity {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/auth/refresh",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/speak",
                                "/auth/oauth2/callback",
                                "/public/**"
                        ).permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .userDetailsService(userDetailsService);

        
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}

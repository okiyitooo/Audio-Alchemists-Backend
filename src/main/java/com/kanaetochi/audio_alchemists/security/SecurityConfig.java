package com.kanaetochi.audio_alchemists.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthEntryPoint jwtAuthEntryPoint, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exceptionHandling -> 
                exceptionHandling.authenticationEntryPoint(jwtAuthEntryPoint))
            .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**").permitAll()
                        // User related endpoints:
                        .requestMatchers(HttpMethod.GET, "/users/{id}").authenticated() // Any authenticated user can get a user by ID.
                        .requestMatchers(HttpMethod.PUT, "/users/me").authenticated()  // allow any authenticated user to update their own profile
                        // Project related endpoints:
                        .requestMatchers(HttpMethod.GET,"/projects").authenticated()  // any authenticated user can get all projects
                        .requestMatchers(HttpMethod.GET,"/projects/{id}").authenticated()  // any authenticated user can get a project by ID
                        .requestMatchers(HttpMethod.POST, "/projects").hasAnyAuthority("COMPOSER", "ADMIN") // only composer can create projects.
                        .requestMatchers(HttpMethod.PUT, "/projects/{id}").hasAnyAuthority("COMPOSER", "ADMIN")// only composers can update their projects.
                        .requestMatchers(HttpMethod.DELETE, "/projects/{id}").hasAnyAuthority("COMPOSER", "ADMIN") // Only composers can delete projects.

                        // Track related endpoints:
                        .requestMatchers(HttpMethod.GET,"/projects/{projectId}/tracks").authenticated() // Any authenticated user can get tracks for a project.
                        .requestMatchers(HttpMethod.GET, "/projects/{projectId}/tracks/{id}").authenticated()// Any authenticated user can get a track.
                        .requestMatchers(HttpMethod.POST, "/projects/{projectId}/tracks").hasAnyAuthority("COMPOSER", "ADMIN") // only composers can add tracks
                        .requestMatchers(HttpMethod.PUT, "/projects/{projectId}/tracks/{id}").hasAnyAuthority("COMPOSER", "ADMIN") // Only composers can update their tracks.
                        .requestMatchers(HttpMethod.DELETE,"/projects/{projectId}/tracks/{id}").hasAnyAuthority("COMPOSER", "ADMIN")// only composers can delete their tracks.
                        // Admin related endpoints:
                        .requestMatchers(HttpMethod.GET,"/users").hasAnyAuthority("ADMIN")// only admins can get all users.
                        .requestMatchers(HttpMethod.PUT,"/users/{id}").hasAnyAuthority("ADMIN") // only admins can update users.
                        .requestMatchers(HttpMethod.DELETE,"/users/{id}").hasAnyAuthority("ADMIN")
                        .anyRequest().authenticated() // any authenticated user can access any other endpoint.
                )
            .sessionManagement(session ->  session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(@Autowired UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }
}

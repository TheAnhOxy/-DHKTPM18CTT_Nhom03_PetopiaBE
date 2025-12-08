package com.pet.config;

import com.pet.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        //  PUBLIC ENDPOINTS (Cho GUEST) - Không cần token
                        .requestMatchers("/api/auth/**", "/auth/**").permitAll() // Đăng nhập, Đăng ký
                        .requestMatchers("/api/payment/sepay-webhook").permitAll() // Webhook thanh toán
                        .requestMatchers("/api/chat", "/api/chat/**").permitAll()
                        .requestMatchers("/api/reviews").permitAll()
                        // Cho phép xem danh sách (GET) mà không cần login
                        .requestMatchers(HttpMethod.GET, "/api/pets/**", "/api/categories/**", "/api/services/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/services/**", "/api/services").permitAll()

                        // ADMIN ENDPOINTS
                        .requestMatchers("/admin/**", "/categories/").hasRole(UserRole.ADMIN.name())

                        //  CUSTOMER / USER ENDPOINTS - Cần đăng nhập
                        .requestMatchers("/api/orders","/api/orders/**").authenticated()
                        .requestMatchers("/api/bookings/**", "/api/bookings").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reviews/**").authenticated()
                        .requestMatchers("/api/users","/api/users/**").authenticated()
                        .requestMatchers("/api/contact","/api/contact/**").authenticated()
                        .requestMatchers("/api/pets/orders/**","/api/pets/deliveries/**").authenticated()
                        .requestMatchers("/api/pre-bookings","/api/pre-bookings/**").authenticated()
                        .requestMatchers("/api/wishlists","/api/wishlists/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public  CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.addAllowedOriginPattern("*");
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
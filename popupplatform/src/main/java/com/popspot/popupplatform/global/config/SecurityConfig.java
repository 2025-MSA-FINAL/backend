// src/main/java/com/popspot/popupplatform/global/config/SecurityConfig.java
package com.popspot.popupplatform.global.config;

import com.popspot.popupplatform.global.security.CustomAccessDeniedHandler;
import com.popspot.popupplatform.global.security.CustomAuthenticationEntryPoint;
import com.popspot.popupplatform.global.security.JwtAuthenticationFilter;
import com.popspot.popupplatform.global.security.OAuth2SuccessHandler;
import com.popspot.popupplatform.global.utils.JwtTokenProvider;
import com.popspot.popupplatform.service.auth.NaverOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final NaverOAuth2UserService naverOAuth2UserService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        /* ===== WebSocket 허용 (핸드셰이크만 필요) ===== */
                        .requestMatchers("/ws-stomp").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/ws-stomp").permitAll()
                        /* ===== STOMP 내부 경로 ===== */
                        .requestMatchers("/pub/**", "/sub/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger",
                                "/health"
                        ).permitAll()
                        .requestMatchers(
                                "/api/auth/phone/**",
                                "/api/reservations",
                                "/api/auth/**",
                                "/oauth2/**",
                                "/api/files/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/popups", "/api/popups/**").permitAll()
                        .requestMatchers("/api/managers/**",
                                "/api/popup/*/reservation-setting").hasRole("MANAGER")
                        .requestMatchers(
                                "/api/users/me",
                                "/api/chat",
                                "/api/chat/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(naverOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowCredentials(true);
        cfg.setAllowedOriginPatterns(List.of("http://localhost:5173", "https://pop-spot.store" ));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

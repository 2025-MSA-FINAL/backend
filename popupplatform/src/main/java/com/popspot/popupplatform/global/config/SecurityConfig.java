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
                                "/health",
                                "/test/postgres",
                                "/api/admin/ai-reports/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/main/*",
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

                /*

                    /login/oauth2/code/{registrationId}
                    /oauth2/authorization/{registrationId}
                    이 두 엔드포인트는 시큐리티 oauth설정으로 자동으로 만들어짐
                    프론트에서 /oauth2/authorization/{registrationId} 호출하면
                    registationId에 해당하는 제공자(네이버,구글,카카오) 로그인 페이지로 이동
                    로그인 완료후에
                    /login/oauth2/code/{registrationId}?code=XXXX&state=YYYY GET요청을 보내게 되고
                    url에 포함된 code로 제공자(네이버,구글,카카오) 서버에 accessToken 요청
                    accessToken 등이 포함된 OAuth2UserRequest를 spring security에서 만들어
                    oauth2UserService에서 사용 후 요청 성공하면
                    oauth2ScuuessHadler로 요청이감
                    실패하면 아래 exceptionHadling을 타게됨
                 */

                /*
    [OAuth2 Login Flow - Spring Security]
    같은 말인데 gpt 정리본
    - /oauth2/authorization/{registrationId}
      - Spring Security OAuth2 설정으로 자동 생성되는 엔드포인트
      - 프론트에서 해당 URL로 이동하면
        registrationId(naver, google, kakao 등)에 해당하는
        OAuth 제공자의 로그인 페이지로 리다이렉트됨

    - 로그인 및 사용자 동의 완료 후,
      OAuth 제공자는 아래 callback URL로 리다이렉트함
        /login/oauth2/code/{registrationId}?code=XXXX&state=YYYY

    - 이 요청 역시 컨트롤러 없이
      Spring Security OAuth2LoginAuthenticationFilter가 처리함

    - URL 파라미터로 전달된 code를 사용해
      Spring Security가 서버 간 통신으로
      OAuth 제공자 서버에 access token을 요청함

    - access token 발급이 성공하면
      ClientRegistration + AccessToken 정보를 담은
      OAuth2UserRequest 객체를 생성하고,
      설정된 OAuth2UserService(loadUser)를 호출함

    - OAuth2UserService 처리 성공 시
      Authentication 객체가 생성되고
      OAuth2SuccessHandler로 흐름이 전달됨

    - 인증 실패 시에는
      아래 exceptionHandling 설정(authenticationEntryPoint / accessDeniedHandler)을 타게 됨
*/
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

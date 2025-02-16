package kr.co.lotteon.security;

import kr.co.lotteon.oauth2.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService oauth2UserService;
    private final SecurityUserService securityUserService;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        // 로그인 설정
        httpSecurity.formLogin(login -> login
                                        .loginPage("/member/login")
                                        .defaultSuccessUrl("/")
                                        .failureUrl("/member/login?success=100")
                                        .usernameParameter("uid")
                                        .passwordParameter("pass"));

        // 자동로그인 설정
        // rememberMe 쿠키 확인
        httpSecurity.rememberMe(config -> config.userDetailsService(securityUserService)
                .rememberMeParameter("rememberMe")
                .key("uniqueAndSecret")
                .tokenValiditySeconds(259200));// 자동 로그인 유효 기간 (초)) 3일



        // 로그아웃 설정
        httpSecurity.logout(logout -> logout
                                        .invalidateHttpSession(true)
                                        .logoutRequestMatcher(new AntPathRequestMatcher("/member/logout"))
                                        .logoutSuccessUrl("/member/login?success=300"));

        // OAuth 설정
        httpSecurity.oauth2Login(oauth -> oauth
                .loginPage("/member/login") 
                .defaultSuccessUrl("/")
                .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))); //소셜로그인 완료된 후 후처리

        ///////////배포전 인가 수정하기/////////
        httpSecurity.authorizeHttpRequests(authorize -> authorize
                                                        .requestMatchers("/").permitAll()
                                                        .requestMatchers("/member/**").permitAll()
                                                        .requestMatchers("/seller/**").hasAuthority("ROLE_5")
                                                        .requestMatchers("/my/**").hasAnyAuthority("ROLE_1","ROLE_2","ROLE_3","ROLE_4","ROLE_5","ROLE_6","ROLE_7")
                                                        .requestMatchers("/product/cart/**").hasAnyAuthority("ROLE_1","ROLE_2","ROLE_3","ROLE_4","ROLE_5","ROLE_6","ROLE_7")
                                                        .requestMatchers("/product/cart/**","/order/**" ).hasAnyAuthority("ROLE_1","ROLE_2","ROLE_3","ROLE_4","ROLE_5","ROLE_6","ROLE_7")
                                                        .requestMatchers("/product/complete/**").hasAnyAuthority("ROLE_1","ROLE_2","ROLE_3","ROLE_4","ROLE_5","ROLE_6","ROLE_7")
                                                        .requestMatchers("/admin/images/**").hasAnyAuthority("ROLE_7","ROLE_5")
                                                        .requestMatchers("/admin/js/**").hasAnyAuthority("ROLE_7","ROLE_5")
                                                        .requestMatchers("/admin/**").hasAuthority("ROLE_7")
                                                        .anyRequest().permitAll());

        // 사이트 위변조 방지 설정
        httpSecurity.csrf(CsrfConfigurer::disable);

        return httpSecurity.build();
    }

    // Security 인증 암호화 인코더 설정
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}

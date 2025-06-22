package com.example.javaTeamG.config;

import com.example.javaTeamG.interceptor.LoginInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    /**
     * インターセプターをSpring MVCに登録します。
     * @param registry インターセプターレジストリ
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 全てのパスに対してインターセプターを適用
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 全てのパス
                // 除外パス: ログインページ、静的リソース、faviconなど
                .excludePathPatterns(
                    "/login", "/login/**", "/access-denied",// ログインフォームとログイン処理
                    "/css/**", "/js/**", "/images/**", "/webjars/**", // 静的リソース
                    "/favicon.ico",
                    "/error" // エラーページ（Spring Bootが提供するデフォルト）
                );
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
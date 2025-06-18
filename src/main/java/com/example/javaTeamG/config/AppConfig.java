package com.example.javaTeamG.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration // このクラスがSpringの設定クラスであることを示す
public class AppConfig {

    // BCryptPasswordEncoderのBeanを定義する
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
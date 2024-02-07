package com.toss.tosspaybackend.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Getter
@Setter
@Primary
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private String[] authWhitelist;
    private int accessTokenValidationMillisecond;
    private int refreshTokenValidationMillisecond;
    private String accessHeader;
    private String refreshHeader;
    private String tokenHeader;
    private String encryptSecretKey;
    private String encryptSecretSalt;
    private String encryptTokenValidationMillisecond;
}

package com.ecommerce.user_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class JwtProperties {

    private Jwt jwt = new Jwt();
    private Cookie cookie = new Cookie();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpiryMs;
        private long refreshTokenExpiryMs;
    }

    @Getter
    @Setter
    public static class Cookie {
        private String accessTokenName;
        private String refreshTokenName;
        private boolean secure;
        private String sameSite;
        private String path;
    }
}
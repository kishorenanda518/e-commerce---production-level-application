package com.ecommerce.order_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class JwtProperties {

    private Jwt    jwt    = new Jwt();
    private Cookie cookie = new Cookie();

    // ── Getters & Setters ─────────────────────────────────────────────
    public Jwt getJwt()       { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public Cookie getCookie()          { return cookie; }
    public void setCookie(Cookie cookie) { this.cookie = cookie; }

    // ── JWT nested class ──────────────────────────────────────────────
    public static class Jwt {
        private String secret;
        private long   accessTokenExpiryMs;
        private long   refreshTokenExpiryMs;

        public String getSecret()                    { return secret; }
        public void   setSecret(String secret)       { this.secret = secret; }

        public long   getAccessTokenExpiryMs()               { return accessTokenExpiryMs; }
        public void   setAccessTokenExpiryMs(long ms)        { this.accessTokenExpiryMs = ms; }

        public long   getRefreshTokenExpiryMs()              { return refreshTokenExpiryMs; }
        public void   setRefreshTokenExpiryMs(long ms)       { this.refreshTokenExpiryMs = ms; }
    }

    // ── Cookie nested class ───────────────────────────────────────────
    public static class Cookie {
        private String  accessTokenName;
        private String  refreshTokenName;
        private boolean secure;
        private String  sameSite;
        private String  path;

        public String  getAccessTokenName()                      { return accessTokenName; }
        public void    setAccessTokenName(String accessTokenName){ this.accessTokenName = accessTokenName; }

        public String  getRefreshTokenName()                     { return refreshTokenName; }
        public void    setRefreshTokenName(String name)          { this.refreshTokenName = name; }

        public boolean isSecure()                                { return secure; }
        public void    setSecure(boolean secure)                 { this.secure = secure; }

        public String  getSameSite()                             { return sameSite; }
        public void    setSameSite(String sameSite)              { this.sameSite = sameSite; }

        public String  getPath()                                 { return path; }
        public void    setPath(String path)                      { this.path = path; }
    }
}
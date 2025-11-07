package com.clinic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private RateLimiting rateLimiting = new RateLimiting();
    private Admin admin = new Admin();
    private Email email = new Email();

    public Jwt jwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Cors cors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public RateLimiting rateLimiting() {
        return rateLimiting;
    }

    public void setRateLimiting(RateLimiting rateLimiting) {
        this.rateLimiting = rateLimiting;
    }

    public Admin admin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Email email() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public record Jwt(
            Token patient,
            Token staff,
            Refresh refresh
    ) {
        public Jwt() {
            this(new Token(), new Token(), new Refresh());
        }
    }

    public record Token(
            String issuer,
            String audience,
            String publicKey,
            String privateKey,
            Duration accessTtl,
            Duration clockSkew
    ) {
        public Token() {
            // Default: 30 days for patient tokens, can be overridden in application.yaml
            this("", "", "", "", Duration.ofDays(30), Duration.ofSeconds(30));
        }
    }

    public record Refresh(
            Duration ttl
    ) {
        public Refresh() {
            this(Duration.ofMinutes(30));
        }
    }

    public record Cors(
            List<String> publicOrigins,
            List<String> adminOrigins
    ) {
        public Cors() {
            this(List.of("http://localhost:3000"), List.of("http://localhost:3001"));
        }
    }

    public record RateLimiting(
            Bucket publicAuth,
            Bucket publicBooking,
            Bucket adminAuth
    ) {
        public RateLimiting() {
            this(new Bucket(5, Duration.ofMinutes(1)),
                    new Bucket(10, Duration.ofMinutes(1)),
                    new Bucket(5, Duration.ofMinutes(1)));
        }
    }

    public record Bucket(
            long capacity,
            Duration refillPeriod
    ) {
    }

    public record Admin(
            List<String> ipAllowlist
    ) {
        public Admin() {
            this(List.of("127.0.0.1/32", "::1/128"));
        }
    }

    public record Email(
            String sendgridApiKey,
            String fromEmail,
            String fromName,
            boolean enabled,
            String googleMeetLink,
            String clinicAddress
    ) {
        public Email() {
            this("${SENDGRID_API_KEY}",
                 "clinic.notifier@gmail.com",
                 "Qadri's Clinic",
                 true,
                 "https://meet.google.com/abc-defg-hij",
                 "123 Medical Center, Amman, Jordan");
        }
    }
}

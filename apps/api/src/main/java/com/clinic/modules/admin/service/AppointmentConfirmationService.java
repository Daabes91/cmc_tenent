package com.clinic.modules.admin.service;

import com.clinic.modules.core.appointment.AppointmentEntity;
import com.clinic.modules.core.appointment.AppointmentRepository;
import com.clinic.modules.core.tenant.TenantEntity;
import com.clinic.modules.core.tenant.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentConfirmationService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentConfirmationService.class);
    private static final String HMAC_ALGO = "HmacSHA256";

    private final AppointmentRepository appointmentRepository;
    private final TenantService tenantService;
    private final String signingSecret;
    private final String apiBaseUrl;
    private final String baseDomain;

    public AppointmentConfirmationService(
            AppointmentRepository appointmentRepository,
            TenantService tenantService,
            @Value("${appointment.confirmation.secret:change-me}") String signingSecret,
            @Value("${app.public-api-url:http://localhost:8080}") String apiBaseUrl,
            @Value("${security.cors.base-domain:}") String baseDomain
    ) {
        this.appointmentRepository = appointmentRepository;
        this.tenantService = tenantService;
        this.signingSecret = signingSecret;
        this.apiBaseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
        this.baseDomain = baseDomain;
    }

    public String generateConfirmationLink(AppointmentEntity appointment) {
        String token = generateToken(appointment);
        String host = resolveTenantHost(appointment.getTenant());
        if (host != null) {
            return "https://" + host + "/appointments/confirm?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        }
        // Fallback to API host if we cannot resolve tenant host
        return apiBaseUrl + "/public/appointments/confirm?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    public Optional<AppointmentEntity> confirmFromToken(String token) {
        try {
            Map<String, String> payload = verifyToken(token);
            Long appointmentId = Long.parseLong(payload.get("aid"));
            Long tenantId = Long.parseLong(payload.get("tid"));
            Optional<AppointmentEntity> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                return Optional.empty();
            }
            AppointmentEntity appt = appointmentOpt.get();
            if (appt.getTenant() == null || !appt.getTenant().getId().equals(tenantId)) {
                return Optional.empty();
            }
            if (!appt.isPatientConfirmed()) {
                appt.setPatientConfirmed(true);
                appt.setPatientConfirmedAt(Instant.now());
                appointmentRepository.save(appt);
            }
            return Optional.of(appt);
        } catch (Exception ex) {
            log.warn("Failed to confirm appointment from token: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public String buildSuccessRedirect(TenantEntity tenant) {
        String host = resolveTenantHost(tenant);
        if (host == null) {
            return "https://cliniqax.com/appointments/confirmation-success";
        }
        return "https://" + host + "/appointments/confirmation-success";
    }

    private String resolveTenantHost(TenantEntity tenant) {
        if (tenant == null) {
            return null;
        }
        if (StringUtils.hasText(tenant.getCustomDomain())) {
            return tenant.getCustomDomain().trim();
        }
        if (StringUtils.hasText(baseDomain)) {
            return tenant.getSlug() + "." + baseDomain;
        }
        return null;
    }

    private String generateToken(AppointmentEntity appointment) {
        Instant exp = Instant.now().plusSeconds(7 * 24 * 60 * 60); // 7 days
        String payload = "aid=" + appointment.getId() +
                "&tid=" + (appointment.getTenant() != null ? appointment.getTenant().getId() : "") +
                "&exp=" + exp.getEpochSecond();
        String signature = sign(payload);
        String combined = payload + "&sig=" + signature;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(combined.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, String> verifyToken(String token) throws Exception {
        String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        String[] parts = decoded.split("&");
        StringBuilder data = new StringBuilder();
        String sig = null;
        for (String part : parts) {
            if (part.startsWith("sig=")) {
                sig = part.substring(4);
            } else {
                if (!data.isEmpty()) data.append("&");
                data.append(part);
            }
        }
        if (sig == null || !sign(data.toString()).equals(sig)) {
            throw new IllegalArgumentException("Invalid signature");
        }
        String[] kvPairs = data.toString().split("&");
        long now = Instant.now().getEpochSecond();
        java.util.Map<String, String> map = new java.util.HashMap<>();
        for (String kv : kvPairs) {
            String[] arr = kv.split("=", 2);
            if (arr.length == 2) {
                map.put(arr[0], arr[1]);
            }
        }
        if (!map.containsKey("exp") || Long.parseLong(map.get("exp")) < now) {
            throw new IllegalArgumentException("Token expired");
        }
        return map;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign token", e);
        }
    }
}

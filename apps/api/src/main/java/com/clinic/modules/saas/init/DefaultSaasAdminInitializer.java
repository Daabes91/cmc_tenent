package com.clinic.modules.saas.init;

import com.clinic.modules.saas.config.DefaultSaasAdminProperties;
import com.clinic.modules.saas.model.SaasManager;
import com.clinic.modules.saas.model.SaasManagerStatus;
import com.clinic.modules.saas.repository.SaasManagerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Ensures a default SAAS administrator account exists whenever the API starts.
 * This makes new deployments usable without having to seed the database manually.
 */
@Component
public class DefaultSaasAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultSaasAdminInitializer.class);

    private final SaasManagerRepository saasManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultSaasAdminProperties properties;

    public DefaultSaasAdminInitializer(
            SaasManagerRepository saasManagerRepository,
            PasswordEncoder passwordEncoder,
            DefaultSaasAdminProperties properties
    ) {
        this.saasManagerRepository = saasManagerRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            log.info("Default SAAS admin creation is disabled via configuration");
            return;
        }

        String email = normalize(properties.getEmail());
        String password = properties.getPassword();
        String fullName = properties.getFullName();

        if (email == null || email.isBlank()) {
            log.warn("saas.default-admin.email is not set; skipping default SAAS admin creation");
            return;
        }

        if (password == null || password.isBlank()) {
            log.warn("saas.default-admin.password is not set; cannot create default SAAS admin '{}'", email);
            return;
        }

        saasManagerRepository.findByEmailIgnoreCase(email)
                .ifPresentOrElse(existing -> ensureManagerIsActive(existing, fullName),
                        () -> createDefaultManager(email, password, fullName));
    }

    private void ensureManagerIsActive(SaasManager existing, String desiredFullName) {
        boolean updated = false;

        if (!existing.isActive()) {
            existing.setStatus(SaasManagerStatus.ACTIVE);
            updated = true;
        }

        if (desiredFullName != null && !desiredFullName.isBlank()
                && !desiredFullName.equals(existing.getFullName())) {
            existing.setFullName(desiredFullName.trim());
            updated = true;
        }

        if (updated) {
            saasManagerRepository.save(existing);
            log.info("Ensured default SAAS admin '{}' is active and up to date", existing.getEmail());
        } else {
            log.info("Default SAAS admin '{}' already exists and is active", existing.getEmail());
        }
    }

    private void createDefaultManager(String email, String password, String fullName) {
        String safeFullName = (fullName == null || fullName.isBlank())
                ? "SAAS Administrator"
                : fullName.trim();

        SaasManager manager = new SaasManager(
                email,
                safeFullName,
                passwordEncoder.encode(password)
        );
        manager.setStatus(SaasManagerStatus.ACTIVE);

        saasManagerRepository.save(manager);
        log.info("Created default SAAS admin account with email '{}'", email);
    }

    private String normalize(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

package com.homecloud.planner.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The only way an admin account ever gets created. There is no default
 * username or password baked into the application — if ADMIN_USERNAME and
 * ADMIN_PASSWORD aren't both set, no account exists and login simply
 * doesn't work until they are. See ADR-0008.
 */
@Component
class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String configuredUsername;
    private final String configuredPassword;

    AdminAccountInitializer(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_USERNAME:}") String configuredUsername,
            @Value("${ADMIN_PASSWORD:}") String configuredPassword) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.configuredUsername = configuredUsername;
        this.configuredPassword = configuredPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminUserRepository.count() > 0) {
            return;
        }
        if (configuredUsername.isBlank() || configuredPassword.isBlank()) {
            log.warn(
                    "No admin account exists and ADMIN_USERNAME/ADMIN_PASSWORD are not set. "
                            + "Login will not work until an admin account is created by setting both "
                            + "environment variables and restarting.");
            return;
        }
        adminUserRepository.save(new AdminUser(configuredUsername, passwordEncoder.encode(configuredPassword)));
        log.info("Created initial admin account '{}'.", configuredUsername);
    }
}

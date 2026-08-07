package com.deutschhub.infrastructure.config;

import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.domain.identity.model.aggregate.User;
import com.deutschhub.domain.identity.model.enums.RoleType;
import com.deutschhub.domain.identity.model.valueobject.Email;
import com.deutschhub.domain.identity.model.valueobject.FullName;
import com.deutschhub.domain.identity.model.valueobject.Password;
import com.deutschhub.domain.identity.model.valueobject.Username;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminAccountInitializer implements CommandLineRunner {

    UserRepositoryPort userRepositoryPort;
    PasswordEncoder passwordEncoder;
    AdminAccountProperties adminProperties;

    @Override
    public void run(String... args) {
        Email adminEmail = new Email(adminProperties.email());

        boolean adminExists = userRepositoryPort.existsByEmail(adminEmail);

        if (adminExists) {
            return;
        }

        User admin = User.register(
                new Username(adminProperties.username()),
                adminEmail,
                Password.fromHashed(passwordEncoder.encode(adminProperties.password())),
                FullName.of(adminProperties.firstName(), adminProperties.lastName()),
                null);

        admin.replaceRoles(Set.of(RoleType.ADMIN));

        userRepositoryPort.save(admin);
    }
}
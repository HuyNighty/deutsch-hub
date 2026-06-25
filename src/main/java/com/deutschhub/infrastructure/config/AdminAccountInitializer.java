package com.deutschhub.infrastructure.config;

import com.deutschhub.domain.identity.model.aggregate.User;
import com.deutschhub.domain.identity.model.enumtype.RoleType;
import com.deutschhub.infrastructure.identity.persistence.entity.UserJpaEntity;
import com.deutschhub.infrastructure.identity.persistence.repository.SpringDataUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminAccountInitializer implements CommandLineRunner {

    SpringDataUserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AdminAccountProperties adminProperties;

    @Override
    public void run(String... args) {
        boolean adminExists = userRepository.existsByEmail(adminProperties.email());

        if (adminExists) {
            return;
        }

        UserJpaEntity admin = UserJpaEntity.builder()
                .id(UUID.randomUUID())
                .username(adminProperties.username())
                .email(adminProperties.email())
                .password(passwordEncoder.encode(adminProperties.password()))
                .firstName(adminProperties.firstName())
                .lastName(adminProperties.lastName())
                .isActive(true)
                .roles(Set.of(RoleType.ADMIN))
                .build();

        userRepository.save(admin);
    }
}
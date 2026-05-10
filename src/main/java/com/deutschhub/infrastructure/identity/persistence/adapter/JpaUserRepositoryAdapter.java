package com.deutschhub.infrastructure.identity.persistence.adapter;

import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.domain.identity.model.aggregate.User;
import com.deutschhub.domain.identity.model.valueobject.Email;
import com.deutschhub.domain.identity.model.valueobject.FullName;
import com.deutschhub.domain.identity.model.valueobject.Password;
import com.deutschhub.domain.identity.model.valueobject.Username;
import com.deutschhub.infrastructure.identity.persistence.entity.UserJpaEntity;
import com.deutschhub.infrastructure.identity.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    private UserJpaEntity toEntity(User user) {

        return UserJpaEntity.builder()
                .id(user.getId())
                .username(user.getUsername().getValue())
                .email(user.getEmail().getValue())
                .password(user.getPassword().getHashedValue())
                .firstName(user.getFullName().getFirstName())
                .lastName(user.getFullName().getLastName())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .roles(user.getRoles())
                .build();
    }

    private User toDomain(UserJpaEntity entity) {

        return User.restore(
                entity.getId(),
                new Username(entity.getUsername()),
                new Email(entity.getEmail()),
                new Password(entity.getPassword()),
                new FullName(entity.getFirstName(), entity.getLastName()),
                entity.getPhoneNumber(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastLoginAt(),
                entity.getRoles()
        );
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);

        UserJpaEntity saved = repository.save(entity);

        return toDomain(saved);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmail(email.getValue());
    }

    @Override
    public boolean existsByUsername(Username username) {
        return repository.existsByUsername(username.getValue());
    }
}
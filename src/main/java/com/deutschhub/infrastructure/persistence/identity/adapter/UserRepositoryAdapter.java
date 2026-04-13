package com.deutschhub.infrastructure.persistence.identity.adapter;

import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.domain.identity.model.aggregate.User;
import com.deutschhub.infrastructure.persistence.identity.jpa.entity.JpaUserEntity;
import com.deutschhub.infrastructure.persistence.identity.jpa.repository.JpaUserRepository;
import com.deutschhub.application.identity.dto.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaRepository;
    private final UserMapper mapper;

    @Override
    public User save(User user) {
        JpaUserEntity entity = mapper.toJpaEntity(user);
        JpaUserEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
}
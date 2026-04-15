package com.deutschhub.application.identity.dto.mapper;

import com.deutschhub.application.identity.dto.request.RegisterUserCommand;
import com.deutschhub.application.identity.dto.request.RegisterUserRequest;
import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.domain.identity.model.aggregate.User;
import com.deutschhub.domain.identity.model.valueobject.Email;
import com.deutschhub.domain.identity.model.valueobject.FullName;
import com.deutschhub.domain.identity.model.valueobject.Password;
import com.deutschhub.infrastructure.persistence.identity.jpa.entity.JpaUserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "fullName", source = "fullName", qualifiedByName = "fullNameToString")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToRoleNames")
    UserResponse toResponse(User user);

    @Mapping(target = "email", source = "email", qualifiedByName = "stringToEmail")
    @Mapping(target = "password", source = "password", qualifiedByName = "stringToPassword")
    @Mapping(target = "fullName", expression = "java(new com.deutschhub.domain.identity.model.valueobject.FullName(request.getFirstName(), request.getLastName()))")
    RegisterUserCommand toCommand(RegisterUserRequest request);

    JpaUserEntity toJpaEntity(User user);

    User toDomain(JpaUserEntity entity);

    @Named("stringToEmail")
    default Email stringToEmail(String email) {
        return email != null ? new Email(email) : null;
    }

    @Named("stringToPassword")
    default Password stringToPassword(String plainPassword) {
        return plainPassword != null ? Password.create(plainPassword) : null;
    }

    @Named("fullNameToString")
    default String fullNameToString(FullName fullName) {
        return fullName != null ? fullName.getFullName() : null;
    }

    @Named("rolesToRoleNames")
    default java.util.Set<String> rolesToRoleNames(java.util.Set<com.deutschhub.domain.identity.model.entity.Role> roles) {
        if (roles == null) return java.util.Set.of();
        return roles.stream()
                .map(com.deutschhub.domain.identity.model.entity.Role::getName)
                .collect(java.util.stream.Collectors.toSet());
    }
}
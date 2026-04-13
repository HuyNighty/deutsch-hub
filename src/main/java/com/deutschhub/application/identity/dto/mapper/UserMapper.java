package com.deutschhub.application.identity.dto.mapper;

import com.deutschhub.application.identity.dto.request.RegisterUserCommand;
import com.deutschhub.application.identity.dto.request.RegisterUserRequest;
import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.domain.identity.model.aggregate.User;
import com.deutschhub.domain.identity.model.valueobject.FullName;
import com.deutschhub.infrastructure.persistence.identity.jpa.entity.JpaUserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "password", source = "password")
    @Mapping(target = "fullName", expression = "java(new com.deutschhub.domain.identity.model.valueobject.FullName(request.getFirstName(), request.getLastName()))")
    RegisterUserCommand toCommand(RegisterUserRequest request);

    JpaUserEntity toJpaEntity(User user);

    User toDomain(JpaUserEntity entity);

    @Named("fullNameToString")
    default String fullNameToString(FullName fullName) {
        return fullName != null ? fullName.getFullName() : null;
    }
}
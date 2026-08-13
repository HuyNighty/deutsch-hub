package com.deutschhub.application.identity.service;

import com.deutschhub.application.identity.dto.request.GetUsersQuery;
import com.deutschhub.application.identity.dto.response.UserSummaryResponse;
import com.deutschhub.application.identity.port.in.GetUsersUseCase;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.common.util.PageResponse;
import com.deutschhub.domain.identity.model.aggregate.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetUsersService implements GetUsersUseCase {

    UserRepositoryPort userRepositoryPort;

    @Override
    public PageResponse<UserSummaryResponse> getUsers(GetUsersQuery query) {
        PageResponse<User> users = userRepositoryPort.findAll(query.keyword(), query.page(), query.size());

        return PageResponse.<UserSummaryResponse>builder()
                .items(users.items().stream()
                        .map(this::toResponse)
                        .toList())
                .page(users.page())
                .size(users.size())
                .totalElements(users.totalElements())
                .totalPages(users.totalPages())
                .build();
    }

    private UserSummaryResponse toResponse(User user) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserSummaryResponse(
                user.getId(),
                user.getUsername().getValue(),
                user.getEmail().getValue(),
                user.getFullName().getFullName(),
                user.isActive(),
                roles
        );
    }
}
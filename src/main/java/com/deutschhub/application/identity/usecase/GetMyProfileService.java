package com.deutschhub.application.identity.usecase;

import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.application.identity.port.in.GetMyProfileUseCase;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.model.aggregate.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetMyProfileService implements GetMyProfileUseCase {

    UserRepositoryPort userRepositoryPort;

    @Override
    public UserResponse getMyProfile(UUID userId) {

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new UserResponse(
                user.getId(),
                user.getUsername().getValue(),
                user.getEmail().getValue(),
                user.getFullName().getFullName()
        );
    }
}

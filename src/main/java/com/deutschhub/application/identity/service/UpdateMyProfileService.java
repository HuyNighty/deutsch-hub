package com.deutschhub.application.identity.service;

import com.deutschhub.application.identity.dto.request.UpdateMyProfileCommand;
import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.application.identity.port.in.UpdateMyProfileUseCase;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.aggregate.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UpdateMyProfileService implements UpdateMyProfileUseCase {

    UserRepositoryPort userRepositoryPort;

    @Override
    public UserResponse updateMyProfile(UpdateMyProfileCommand command) {
        User user = userRepositoryPort.findById(command.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.validateCanLogin();

        user.updateMyProfile(
                command.firstName(),
                command.lastName(),
                command.phoneNumber()
        );

        User savedUser = userRepositoryPort.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername().getValue(),
                savedUser.getEmail().getValue(),
                savedUser.getFullName().getFullName(),
                savedUser.getPhoneNumber()
        );
    }
}
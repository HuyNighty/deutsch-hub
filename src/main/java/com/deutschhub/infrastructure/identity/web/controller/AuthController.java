package com.deutschhub.infrastructure.identity.web.controller;

import com.deutschhub.application.identity.dto.request.RegisterUserCommand;
import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.application.identity.port.in.RegisterUserUseCase;
import com.deutschhub.infrastructure.identity.web.request.RegisterUserRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    RegisterUserUseCase registerUserUseCase;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterUserRequest request) {

        RegisterUserCommand command = new RegisterUserCommand(request.username(),
                request.email(), request.password(), request.firstName(), request.lastName(),
                        request.phoneNumber());

        UserResponse response = registerUserUseCase.register(command);

        return ResponseEntity.ok(response);
    }
}

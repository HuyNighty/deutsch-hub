package com.deutschhub.application.identity.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

     UUID id;
     String username;
     String email;
     String fullName;
     String phoneNumber;
     boolean isActive;
     LocalDateTime createdAt;
     Set<String> roles;
}
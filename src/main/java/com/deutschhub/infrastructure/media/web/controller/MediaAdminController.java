package com.deutschhub.infrastructure.media.web.controller;

import com.deutschhub.application.media.dto.request.UploadMediaCommand;
import com.deutschhub.application.media.dto.response.MediaContentResponse;
import com.deutschhub.application.media.dto.response.MediaResponse;
import com.deutschhub.application.media.port.in.GetMediaContentUseCase;
import com.deutschhub.application.media.port.in.GetMediaUseCase;
import com.deutschhub.application.media.port.in.UploadMediaUseCase;
import com.deutschhub.common.util.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v2/admin/media")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MediaAdminController {

    UploadMediaUseCase uploadMediaUseCase;
    GetMediaUseCase getMediaUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MediaResponse>> upload(@RequestParam("file")MultipartFile file, @AuthenticationPrincipal Jwt jwt)
            throws IOException {
        UUID userId = UUID.fromString(jwt.getSubject());

        UploadMediaCommand command = new UploadMediaCommand(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream(),
                userId);

        return ResponseEntity.ok(
                ApiResponse.<MediaResponse>builder()
                        .result(uploadMediaUseCase.upload(command))
                        .build()
        );
    }

    @GetMapping("/{mediaId}")
    public ApiResponse<MediaResponse> getById(@PathVariable UUID mediaId,
                                              @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        boolean isAdmin = jwt.getClaimAsStringList("roles").contains("ADMIN");

        return ApiResponse.<MediaResponse>builder()
                .result(getMediaUseCase.getById(mediaId, currentUserId, isAdmin))
                .build();
    }
}

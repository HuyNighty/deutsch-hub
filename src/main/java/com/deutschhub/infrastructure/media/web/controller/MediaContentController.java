package com.deutschhub.infrastructure.media.web.controller;

import com.deutschhub.application.media.dto.response.MediaContentResponse;
import com.deutschhub.application.media.port.in.GetMediaContentUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/media")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MediaContentController {

    GetMediaContentUseCase getMediaContentUseCase;

    @GetMapping("/{mediaId}/content")
    public ResponseEntity<Resource> getContent(@PathVariable UUID mediaId) {

        MediaContentResponse media = getMediaContentUseCase.getContent(mediaId);

        InputStreamResource resource = new InputStreamResource(media.inputStream());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.mimeType()))
                .contentLength(media.sizeBytes())
                .body(resource);
    }
}

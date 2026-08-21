package com.deutschhub.infrastructure.content.article.web.controller;

import com.deutschhub.application.content.article.dto.request.CreateDraftCommand;
import com.deutschhub.application.content.article.dto.response.CreateDraftResponse;
import com.deutschhub.application.content.article.port.in.CreateDraftUseCase;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.infrastructure.content.article.web.request.CreateDraftRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/editor/articles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArticleEditorController {

    CreateDraftUseCase createDraftUseCase;

    @PreAuthorize("hasRole('CONTENT_EDITOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateDraftResponse>> createDraft(@RequestBody @Valid CreateDraftRequest request) {

        CreateDraftCommand command = new CreateDraftCommand(request.title());

        CreateDraftResponse response = createDraftUseCase.createDraft(command);

        return ResponseEntity.ok(
                ApiResponse.<CreateDraftResponse>builder()
                        .message("Create draft successfully")
                        .result(response)
                        .build()
        );
    }
}

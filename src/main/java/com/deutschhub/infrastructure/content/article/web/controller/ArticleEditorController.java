package com.deutschhub.infrastructure.content.article.web.controller;

import com.deutschhub.application.content.article.dto.request.*;
import com.deutschhub.application.content.article.dto.response.*;
import com.deutschhub.application.content.article.port.in.*;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.infrastructure.content.article.web.request.CreateDraftRequest;
import com.deutschhub.infrastructure.content.article.web.request.UpdateDraftRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/editor/articles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArticleEditorController {

    CreateDraftUseCase createDraftUseCase;
    UpdateDraftUseCase updateDraftUseCase;
    SubmitReviewUseCase submitReviewUseCase;
    WithdrawReviewUseCase withdrawReviewUseCase;
    CreateNewDraftUseCase createNewDraftUseCase;
    GetArticleDetailUseCase getArticleDetailUseCase;

    @PreAuthorize("hasRole('CONTENT_EDITOR')")
    @PostMapping("/draft")
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

    @PreAuthorize("hasAnyRole('CONTENT_EDITOR', 'ADMIN')")
    @PatchMapping("/{articleId}/draft")
    public ResponseEntity<ApiResponse<UpdateDraftResponse>> updateDraft(
            @PathVariable UUID articleId,
            @RequestBody @Valid UpdateDraftRequest request) {
        UpdateDraftCommand command = new UpdateDraftCommand(articleId, request.title(), request.summary(), request.body(),
                request.primaryCategoryId(), request.topicIds(), request.coverMediaId(),
                request.sources() == null ? null : request.sources()
                        .stream()
                        .map(source -> new SourceCommand(source.title(), source.url())).toList());

        UpdateDraftResponse response = updateDraftUseCase.updateDraft(command);

        return ResponseEntity.ok(
                ApiResponse.<UpdateDraftResponse>builder()
                        .message("Update draft successfully")
                        .result(response)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('CONTENT_EDITOR', 'ADMIN')")
    @PostMapping("/{articleId}/submit")
    public ResponseEntity<ApiResponse<SubmitReviewResponse>> submitReview(@PathVariable UUID articleId) {

        SubmitReviewCommand command = new SubmitReviewCommand(articleId);

        SubmitReviewResponse response = submitReviewUseCase.submitReview(command);

        return ResponseEntity.ok(
                ApiResponse.<SubmitReviewResponse>builder()
                        .message("Submit review successfully")
                        .result(response)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('CONTENT_EDITOR', 'ADMIN')")
    @PostMapping("/{articleId}/withdraw-review")
    public ResponseEntity<ApiResponse<WithdrawReviewResponse>> withdrawReview(@PathVariable UUID articleId) {

        WithdrawReviewCommand command = new WithdrawReviewCommand(articleId);

        WithdrawReviewResponse response = withdrawReviewUseCase.withdrawReview(command);

        return ResponseEntity.ok(
                ApiResponse.<WithdrawReviewResponse>builder()
                        .message("Withdraw review successfully")
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/{articleId}/new-draft")
    public ResponseEntity<ApiResponse<CreateNewDraftResponse>> createNewDraft(@PathVariable UUID articleId) {
        CreateNewDraftCommand command = new CreateNewDraftCommand(articleId);

        CreateNewDraftResponse response = createNewDraftUseCase.createNewDraft(command);

        return ResponseEntity.ok(
                ApiResponse.<CreateNewDraftResponse>builder()
                        .message("Create new draft successfully")
                        .result(response)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('CONTENT_EDITOR', 'ADMIN')")
    @GetMapping("/{articleId}")
    public ResponseEntity<ApiResponse<ArticleDetailResponse>> getArticleDetail(@PathVariable UUID articleId) {

        ArticleDetailResponse response = getArticleDetailUseCase.getById(articleId);

        return ResponseEntity.ok(
                ApiResponse.<ArticleDetailResponse>builder()
                        .message("Get article detail successfully")
                        .result(response)
                        .build()
        );
    }
}

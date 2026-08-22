package com.deutschhub.infrastructure.content.article.web.controller;

import com.deutschhub.application.content.article.dto.request.ArchiveArticleCommand;
import com.deutschhub.application.content.article.dto.request.PublishArticleCommand;
import com.deutschhub.application.content.article.dto.request.RequestChangesCommand;
import com.deutschhub.application.content.article.dto.request.TransferOwnershipCommand;
import com.deutschhub.application.content.article.dto.response.ArchiveArticleResponse;
import com.deutschhub.application.content.article.dto.response.PublishArticleResponse;
import com.deutschhub.application.content.article.dto.response.RequestChangesResponse;
import com.deutschhub.application.content.article.dto.response.TransferOwnershipResponse;
import com.deutschhub.application.content.article.port.in.ArchiveArticleUseCase;
import com.deutschhub.application.content.article.port.in.PublishArticleUseCase;
import com.deutschhub.application.content.article.port.in.RequestChangesUseCase;
import com.deutschhub.application.content.article.port.in.TransferOwnershipUseCase;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.infrastructure.content.article.web.request.RequestChangesRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/admin/articles")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ArticleAdminController {

    RequestChangesUseCase requestChangesUseCase;
    PublishArticleUseCase publishArticleUseCase;
    ArchiveArticleUseCase archiveArticleUseCase;
    TransferOwnershipUseCase transferOwnershipUseCase;

    @PostMapping("/{articleId}/request-changes")
    public ResponseEntity<ApiResponse<RequestChangesResponse>> requestChanges(@PathVariable UUID articleId,
                                                                             @RequestBody @Valid RequestChangesRequest requestChanges) {
        RequestChangesCommand command = new RequestChangesCommand(articleId, requestChanges.feedback());

        RequestChangesResponse response = requestChangesUseCase.requestChanges(command);

        return ResponseEntity.ok(
                ApiResponse.<RequestChangesResponse>builder()
                        .message("Request changes successfully")
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/{articleId}/publish")
    public ResponseEntity<ApiResponse<PublishArticleResponse>> publish(@PathVariable UUID articleId) {

        PublishArticleCommand command = new PublishArticleCommand(articleId);

        PublishArticleResponse response = publishArticleUseCase.publish(command);

        return ResponseEntity.ok(
                ApiResponse.<PublishArticleResponse>builder()
                        .message("Publish article successfully")
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/{articleId}/archive")
    public ResponseEntity<ApiResponse<ArchiveArticleResponse>> archive(@PathVariable UUID articleId) {
        ArchiveArticleCommand command = new ArchiveArticleCommand(articleId);

        ArchiveArticleResponse response = archiveArticleUseCase.archive(command);

        return ResponseEntity.ok(
                ApiResponse.<ArchiveArticleResponse>builder()
                        .message("Archive article successfully")
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/{articleId}/transfer-owner/{newOwnerId}")
    public ResponseEntity<ApiResponse<TransferOwnershipResponse>> transferOwnership(@PathVariable UUID articleId, @PathVariable UUID newOwnerId) {

        TransferOwnershipCommand command = new TransferOwnershipCommand(articleId, newOwnerId);

        TransferOwnershipResponse response = transferOwnershipUseCase.transferOwnership(command);

        return ResponseEntity.ok(
                ApiResponse.<TransferOwnershipResponse>builder()
                        .message("Transfer ownership successfully")
                        .result(response)
                        .build()
        );
    }
}

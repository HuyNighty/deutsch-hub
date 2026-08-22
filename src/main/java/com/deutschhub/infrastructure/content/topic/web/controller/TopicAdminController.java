package com.deutschhub.infrastructure.content.topic.web.controller;

import com.deutschhub.application.content.topic.dto.request.CreateTopicCommand;
import com.deutschhub.application.content.topic.dto.request.DeactivateTopicCommand;
import com.deutschhub.application.content.topic.dto.request.ReactivateTopicCommand;
import com.deutschhub.application.content.topic.dto.request.RenameTopicCommand;
import com.deutschhub.application.content.topic.dto.response.TopicResponse;
import com.deutschhub.application.content.topic.dto.response.TopicSummaryResponse;
import com.deutschhub.application.content.topic.port.in.*;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.infrastructure.content.topic.web.request.CreateTopicRequest;
import com.deutschhub.infrastructure.content.topic.web.request.RenameTopicRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/admin/topics")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TopicAdminController {

    CreateTopicUseCase createTopicUseCase;
    ReactivateTopicUseCase reactivateTopicUseCase;
    DeactivateTopicUseCase deactivateTopicUseCase;
    RenameTopicUseCase renameTopicUseCase;
    GetActiveTopicsUseCase getActiveTopicsUseCase;

    @PostMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<TopicResponse>> createTopic(
            @PathVariable UUID categoryId,
            @RequestBody @Valid CreateTopicRequest createTopicRequest) {

        CreateTopicCommand command = new CreateTopicCommand(categoryId, createTopicRequest.topicName());

        TopicResponse response = createTopicUseCase.create(command);

        return ResponseEntity.ok(
                ApiResponse.<TopicResponse>builder()
                        .message("Create topic successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<List<TopicSummaryResponse>>> getActiveTopics(@PathVariable UUID categoryId) {
        List<TopicSummaryResponse> responses = getActiveTopicsUseCase.getActiveTopicsByCategoryId(categoryId);

        return ResponseEntity.ok(
                ApiResponse.<List<TopicSummaryResponse>>builder()
                        .message("Get active topics successfully")
                        .result(responses)
                        .build()
        );
    }

    @PatchMapping("/{topicId}")
    public ResponseEntity<ApiResponse<TopicResponse>> renameTopic(
            @PathVariable UUID topicId,
            @RequestBody @Valid RenameTopicRequest renameTopicRequest) {

        RenameTopicCommand command = new RenameTopicCommand(topicId, renameTopicRequest.topicName());

        TopicResponse response = renameTopicUseCase.rename(command);

        return ResponseEntity.ok(
                ApiResponse.<TopicResponse>builder()
                        .message("Rename topic successfully")
                        .result(response)
                        .build()
        );
    }

    @PatchMapping("/{topicId}/deactivate")
    public ResponseEntity<ApiResponse<TopicResponse>> deactivateTopic(@PathVariable UUID topicId) {
        DeactivateTopicCommand command = new DeactivateTopicCommand(topicId);

        TopicResponse response = deactivateTopicUseCase.deactivate(command);

        return ResponseEntity.ok(
                ApiResponse.<TopicResponse>builder()
                        .message("Deactivate topic successfully")
                        .result(response)
                        .build()
        );
    }

    @PatchMapping("/{topicId}/reactivate")
    public ResponseEntity<ApiResponse<TopicResponse>> reactivateTopic(@PathVariable UUID topicId) {
        ReactivateTopicCommand command = new ReactivateTopicCommand(topicId);

        TopicResponse response = reactivateTopicUseCase.reactivate(command);

        return ResponseEntity.ok(
                ApiResponse.<TopicResponse>builder()
                        .message("Reactivate topic successfully")
                        .result(response)
                        .build()
        );
    }
}

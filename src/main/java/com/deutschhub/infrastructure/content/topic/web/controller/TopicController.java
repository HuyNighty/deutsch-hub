package com.deutschhub.infrastructure.content.topic.web.controller;

import com.deutschhub.application.content.topic.dto.response.TopicSummaryResponse;
import com.deutschhub.application.content.topic.port.in.GetActiveTopicsUseCase;
import com.deutschhub.common.util.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/topics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TopicController {

    GetActiveTopicsUseCase getActiveTopicsUseCase;

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<List<TopicSummaryResponse>>> getTopics(@PathVariable UUID categoryId) {

        List<TopicSummaryResponse> responses = getActiveTopicsUseCase.getActiveTopicsByCategoryId(categoryId);

        return ResponseEntity.ok(
                ApiResponse.<List<TopicSummaryResponse>>builder()
                        .message("Get topics successfully")
                        .result(responses)
                        .build()
        );
    }
}

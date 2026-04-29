package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Certificate implements Auditable {
    private final UUID id;
    private final UUID userId;
    private final UUID courseId;
    private final LocalDateTime issuedAt;
    private final String certificateNumber;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Certificate(UUID id, UUID userId, UUID courseId, LocalDateTime issuedAt, String certificateNumber) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.courseId = Objects.requireNonNull(courseId);
        this.issuedAt = Objects.requireNonNull(issuedAt);
        this.certificateNumber = Objects.requireNonNull(certificateNumber);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Certificate issue(UUID userId, UUID courseId, boolean courseCompleted,
                                    boolean quizRequirementMet, boolean isQuizRequired) {
        return issue(userId, courseId, courseCompleted, quizRequirementMet, isQuizRequired, userId, false);
    }

    public static Certificate issue(UUID userId, UUID courseId, boolean courseCompleted,
                                    boolean quizRequirementMet, boolean isQuizRequired,
                                    UUID actorId, boolean isAdmin) {
        ensureCanIssueFor(userId, actorId, isAdmin);
        if (!courseCompleted) {
            throw new BusinessException(ErrorCode.COURSE_NOT_COMPLETED);
        }
        if (isQuizRequired && !quizRequirementMet) {
            throw new BusinessException(ErrorCode.QUIZ_REQUIREMENT_NOT_MET);
        }

        String certNumber = generateCertificateNumber(userId, courseId);
        return new Certificate(UUID.randomUUID(), userId, courseId, LocalDateTime.now(), certNumber);
    }

    private static void ensureCanIssueFor(UUID userId, UUID actorId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (actorId == null || !actorId.equals(userId)) {
            throw new BusinessException(ErrorCode.CERTIFICATE_FORBIDDEN_ACTION);
        }
    }

    private static String generateCertificateNumber(UUID userId, UUID courseId) {
        return "CERT-" + userId.toString().substring(0,8) + "-" + courseId.toString().substring(0,8) + "-" + System.currentTimeMillis();
    }

    public boolean isIssuedFor(UUID userId, UUID courseId) {
        return this.userId.equals(userId) && this.courseId.equals(courseId);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
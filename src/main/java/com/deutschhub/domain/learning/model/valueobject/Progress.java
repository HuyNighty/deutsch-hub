package com.deutschhub.domain.learning.model.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Progress {

    private final int completedLessons;
    private final int totalLessons;
    private final BigDecimal completionPercentage;
    private final int totalStudyMinutes;
    private final LocalDateTime lastUpdatedAt;

    private Progress(int completedLessons, int totalLessons, int totalStudyMinutes, LocalDateTime lastUpdatedAt) {
        this.completedLessons = validateCompletedLessons(completedLessons, totalLessons);
        this.totalLessons = validateTotalLessons(totalLessons);
        this.completionPercentage = calculateCompletionPercentage(completedLessons, totalLessons);
        this.totalStudyMinutes = Math.max(0, totalStudyMinutes);
        this.lastUpdatedAt = lastUpdatedAt == null ? LocalDateTime.now() : lastUpdatedAt;
    }

    public static Progress create(int completedLessons, int totalLessons, int totalStudyMinutes) {
        return new Progress(completedLessons, totalLessons, totalStudyMinutes, LocalDateTime.now());
    }

    public static Progress createInitial(int totalLessons) {
        return new Progress(0, totalLessons, 0, LocalDateTime.now());
    }

    public Progress updateProgress(int newCompletedLessons, int newTotalStudyMinutes) {
        return new Progress(newCompletedLessons, this.totalLessons, newTotalStudyMinutes, LocalDateTime.now());
    }

    public static Progress restore(int completedLessons, int totalLessons, int totalStudyMinutes, LocalDateTime lastUpdatedAt) {
        return new Progress(completedLessons, totalLessons, totalStudyMinutes, lastUpdatedAt);
    }

    public boolean isCompleted() {
        return completionPercentage.compareTo(BigDecimal.valueOf(100)) >= 0;
    }

    public boolean isInProgress() {
        return  completionPercentage.compareTo(BigDecimal.ZERO) > 0
                && completionPercentage.compareTo(BigDecimal.valueOf(100)) < 0;
    }

    public boolean hasStarted() {
        return completedLessons > 0 || totalStudyMinutes > 0;
    }

    private int validateCompletedLessons(int completed, int total) {
        if (completed < 0) {
            throw new BusinessException(ErrorCode.INVALID_PROGRESS_DATA, "Completed lessons cannot be negative");
        }
        if (completed > total) {
            throw new BusinessException(ErrorCode.INVALID_PROGRESS_DATA, "Completed lessons cannot exceed total lessons");
        }
        return completed;
    }

    private int validateTotalLessons(int total) {
        if (total <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PROGRESS_DATA, "Total lessons must be greater than 0");
        }
        return total;
    }

    private BigDecimal calculateCompletionPercentage(int completed, int total) {
        if (total == 0) return BigDecimal.valueOf(0);
        return BigDecimal.valueOf(Math.round(((double) completed / total) * 10000.0) / 100.0);
    }

    public int getCompletedLessons() {
        return completedLessons;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public BigDecimal getCompletionPercentage() {
        return completionPercentage;
    }

    public int getTotalStudyMinutes() {
        return totalStudyMinutes;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

}

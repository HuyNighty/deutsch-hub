package com.deutschhub.domain.learning.model.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.time.LocalDateTime;

public final class Progress {

    private final int completedLessons;
    private final int totalLessons;
    private final double completionPercentage;
    private final int totalStudyMinutes;
    private final LocalDateTime lastUpdatedAt;

    private Progress(int completedLessons, int totalLessons, int totalStudyMinutes) {
        this.completedLessons = validateCompletedLessons(completedLessons, totalLessons);
        this.totalLessons = validateTotalLessons(totalLessons);
        this.completionPercentage = calculateCompletionPercentage(completedLessons, totalLessons);
        this.totalStudyMinutes = Math.max(0, totalStudyMinutes);
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public static Progress create(int completedLessons, int totalLessons, int totalStudyMinutes) {
        return new Progress(completedLessons, totalLessons, totalStudyMinutes);
    }

    public static Progress createInitial(int totalLessons) {
        return new Progress(0, totalLessons, 0);
    }

    public Progress updateProgress(int newCompletedLessons, int newTotalStudyMinutes) {
        return new Progress(newCompletedLessons, this.totalLessons, newTotalStudyMinutes);
    }

    public boolean isCompleted() {
        return completionPercentage >= 100.0;
    }

    public boolean isInProgress() {
        return completionPercentage > 0.0 && completionPercentage < 100.0;
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

    private double calculateCompletionPercentage(int completed, int total) {
        if (total == 0) return 0.0;
        return Math.round(((double) completed / total) * 10000.0) / 100.0;
    }

    public int getCompletedLessons() {
        return completedLessons;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public int getTotalStudyMinutes() {
        return totalStudyMinutes;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

}

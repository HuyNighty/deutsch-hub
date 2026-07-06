package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.AdminEnrollmentDetailResponse;

import java.util.UUID;

public interface GetEnrollmentDetailUseCase {

    AdminEnrollmentDetailResponse getEnrollmentDetail(UUID enrollmentId);
}

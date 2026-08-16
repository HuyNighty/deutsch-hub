package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.request.SubmitReviewCommand;
import com.deutschhub.application.content.article.dto.response.SubmitReviewResponse;

public interface SubmitReviewUseCase {

    SubmitReviewResponse submitReview(SubmitReviewCommand command);

}

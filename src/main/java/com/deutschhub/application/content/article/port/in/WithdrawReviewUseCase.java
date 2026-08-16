package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.request.WithdrawReviewCommand;
import com.deutschhub.application.content.article.dto.response.WithdrawReviewResponse;

public interface WithdrawReviewUseCase {

    WithdrawReviewResponse withdrawReview(WithdrawReviewCommand command);
}

package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.request.PublishArticleCommand;
import com.deutschhub.application.content.article.dto.response.PublishArticleResponse;

public interface PublishArticleUseCase {

    PublishArticleResponse publish(PublishArticleCommand command);

}

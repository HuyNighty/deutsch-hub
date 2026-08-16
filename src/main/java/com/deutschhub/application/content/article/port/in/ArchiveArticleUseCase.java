package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.request.ArchiveArticleCommand;
import com.deutschhub.application.content.article.dto.response.ArchiveArticleResponse;

public interface ArchiveArticleUseCase {

    ArchiveArticleResponse archive(ArchiveArticleCommand command);
}
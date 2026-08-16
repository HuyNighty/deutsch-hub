package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.request.TransferOwnershipCommand;
import com.deutschhub.application.content.article.dto.response.TransferOwnershipResponse;
import com.deutschhub.application.content.article.port.in.TransferOwnershipUseCase;
import com.deutschhub.application.content.article.port.out.ArticleRepositoryPort;
import com.deutschhub.application.content.article.port.out.CurrentUserPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.aggregate.Article;
import com.deutschhub.domain.shared.valueobject.UserId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class TransferOwnershipService implements TransferOwnershipUseCase {

    ArticleRepositoryPort articleRepositoryPort;
    CurrentUserPort currentUserPort;

    @Override
    public TransferOwnershipResponse transferOwnership(TransferOwnershipCommand command) {

        if (command == null || command.articleId() == null || command.newOwnerId() == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_OWNERSHIP_TRANSFER_DATA);
        }

        UserId transferredBy = currentUserPort.getCurrentUserId();
        UserId newOwner = UserId.of(command.newOwnerId());

        Article article = articleRepositoryPort.findById(command.articleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        Instant now = Instant.now();

        article.transferOwnership(newOwner, transferredBy, now);

        articleRepositoryPort.save(article);

        return toResponse(article);
    }

    private TransferOwnershipResponse toResponse(
            Article article
    ) {
        return new TransferOwnershipResponse(
                article.getId(),
                article.getOwnerId().value(),
                article.getOwnershipTransferredBy().value(),
                article.getOwnershipTransferredAt()
        );
    }
}
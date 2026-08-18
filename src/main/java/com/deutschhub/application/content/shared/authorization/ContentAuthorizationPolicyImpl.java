package com.deutschhub.application.content.shared.authorization;

import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.aggregate.Article;
import com.deutschhub.domain.identity.enums.RoleType;
import org.springframework.stereotype.Component;

@Component
public class ContentAuthorizationPolicyImpl implements ContentAuthorizationPolicy {

    @Override
    public void requireContentEditorOrAdmin(CurrentActor actor) {
        if (actor == null) {
            throw new BusinessException(ErrorCode.CONTENT_FORBIDDEN_ACTION);
        }

        if (actor.roleType() != RoleType.CONTENT_EDITOR && actor.roleType() != RoleType.ADMIN) {
            throw new BusinessException(ErrorCode.CONTENT_FORBIDDEN_ACTION);
        }
    }

    @Override
    public void requireArticleOwnerOrAdmin(Article article, CurrentActor actor) {
        if (actor == null) {
            throw new BusinessException(ErrorCode.CONTENT_FORBIDDEN_ACTION);
        }

        if (actor.roleType() == RoleType.ADMIN) {
            return;
        }

        if (actor.roleType() != RoleType.CONTENT_EDITOR) {
            throw new BusinessException(ErrorCode.CONTENT_FORBIDDEN_ACTION);
        }

        if (!article.getOwnerId().equals(actor.userId())) {
            throw new BusinessException(
                    ErrorCode.ARTICLE_NOT_OWNED_BY_ACTOR
            );
        }
    }

    @Override
    public void requireAdmin(CurrentActor actor) {
        if (actor == null || actor.roleType() != RoleType.ADMIN) {
            throw new BusinessException(
                    ErrorCode.CONTENT_FORBIDDEN_ACTION
            );
        }
    }
}

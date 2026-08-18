package com.deutschhub.application.content.shared.authorization;

import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.domain.content.article.aggregate.Article;
import com.deutschhub.domain.shared.valueobject.UserId;

public interface ContentAuthorizationPolicy {

    void requireContentEditorOrAdmin(CurrentActor actor);

    void requireArticleOwnerOrAdmin(Article article, CurrentActor actor);

    void requireAdmin(CurrentActor actor);

    void requireArticleOwnerOrAdmin(UserId ownerId, CurrentActor actor);
}

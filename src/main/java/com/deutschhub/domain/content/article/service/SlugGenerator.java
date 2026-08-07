package com.deutschhub.domain.content.article.service;

import com.deutschhub.domain.content.article.valueobject.ArticleTitle;
import com.deutschhub.domain.content.article.valueobject.Slug;

public interface SlugGenerator {

    Slug generateTemporary();

    Slug generateFromTitle(ArticleTitle title);
}

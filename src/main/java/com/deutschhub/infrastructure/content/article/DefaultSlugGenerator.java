package com.deutschhub.infrastructure.content.article;

import com.deutschhub.domain.content.article.service.SlugGenerator;
import com.deutschhub.domain.content.article.valueobject.ArticleTitle;
import com.deutschhub.domain.content.article.valueobject.Slug;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

public class DefaultSlugGenerator implements SlugGenerator {

    @Override
    public Slug generateTemporary() {
        return new Slug("draft-" + UUID.randomUUID());
    }

    @Override
    public Slug generateFromTitle(ArticleTitle title) {
        String slug = Normalizer.normalize(title.value(), Normalizer.Form.NFD);

        slug = slug.replaceAll("\\p{M}", "");
        slug = slug.replace("đ", "d");
        slug = slug.replace("Đ", "D");

        slug = slug.toLowerCase(Locale.ROOT);

        slug = slug.replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("-{2,}", "-");
        slug = slug.replaceAll("^-|-$", "");

        return new Slug(slug);
    }
}

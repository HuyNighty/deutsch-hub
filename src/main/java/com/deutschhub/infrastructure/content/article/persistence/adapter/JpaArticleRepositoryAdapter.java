package com.deutschhub.infrastructure.content.article.persistence.adapter;

import com.deutschhub.application.content.article.port.out.ArticleRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.aggregate.Article;
import com.deutschhub.domain.content.article.valueobject.Slug;
import com.deutschhub.infrastructure.content.article.persistence.entity.ArticleJpaEntity;
import com.deutschhub.infrastructure.content.article.persistence.mapper.ArticlePersistenceMapper;
import com.deutschhub.infrastructure.content.article.persistence.repository.SpringDataArticleRepository;
import com.deutschhub.infrastructure.content.article.persistence.repository.SpringDataArticleVersionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaArticleRepositoryAdapter implements ArticleRepositoryPort {

    SpringDataArticleRepository springDataArticleRepository;
    SpringDataArticleVersionRepository springDataArticleVersionRepository;
    ArticlePersistenceMapper articlePersistenceMapper;

    @Override
    public boolean existsBySlug(Slug slug) {
        return springDataArticleRepository.existsBySlug(slug.value());
    }

    @Override
    public void save(Article article) {
        if (article == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        if (!springDataArticleRepository.existsById(article.getId())) {
            ArticleJpaEntity entity = articlePersistenceMapper.toJpa(article);
            saveNewArticle(entity);
            return;
        }

        updateExistingArticle(article);
    }

    @Override
    public Optional<Article> findById(UUID id) {
        return springDataArticleRepository.findById(id)
                .map(articlePersistenceMapper::toDomain);
    }

    private void saveNewArticle(ArticleJpaEntity entity) {
        UUID draftVersionId = entity.getDraftVersionId();
        UUID publishedVersionId = entity.getPublishedVersionId();

        entity.setDraftVersionId(null);
        entity.setPublishedVersionId(null);

        springDataArticleRepository.saveAndFlush(entity);

        entity.setDraftVersionId(draftVersionId);
        entity.setPublishedVersionId(publishedVersionId);

        springDataArticleRepository.saveAndFlush(entity);
    }

    private void updateExistingArticle(Article article) {
        ArticleJpaEntity entity = springDataArticleRepository
                .findById(article.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        articlePersistenceMapper.updateJpa(entity, article);
    }
}

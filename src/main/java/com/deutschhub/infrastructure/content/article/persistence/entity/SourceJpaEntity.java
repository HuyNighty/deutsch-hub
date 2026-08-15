package com.deutschhub.infrastructure.content.article.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SourceJpaEntity {

    @Column(name = "title", nullable = false, length = 255)
    String title;

    @Column(name = "url", nullable = false, length = 2048)
    String url;
}
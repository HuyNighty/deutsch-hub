package com.deutschhub.infrastructure.content.topic.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class TopicJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "category_id", nullable = false)
    UUID categoryId;

    @Column(name = "topic_name", nullable = false, length = 100)
    String topicName;

    @Column(name = "topic_name_normalized", nullable = false, length = 100)
    String topicNameNormalized;

    @Column(name = "topic_status", nullable = false, length = 20)
    String topicStatus;
}

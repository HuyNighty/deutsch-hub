package com.deutschhub.common.domain;

import java.time.LocalDateTime;

public interface Auditable {

    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();

    void touch();
}

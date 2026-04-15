package com.deutschhub.common.domain;

import java.time.LocalDateTime;

public interface SoftDeletable {

    LocalDateTime getDeletedAt();

    boolean isDeleted();

    void softDelete();
}

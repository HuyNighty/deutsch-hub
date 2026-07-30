package com.deutschhub.infrastructure.media.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.media")
public record MediaProperties(
        @NotBlank
        String storageRoot,

        @NotNull
        DataSize maxUploadSize
) {
    public MediaProperties {
        if (maxUploadSize != null && maxUploadSize.toBytes() <= 0 ) {
            throw new IllegalArgumentException("Media max upload size must be greater than zero");
        }
    }
}

package com.deutschhub.infrastructure.media.persistence.generator;

import com.deutschhub.domain.media.model.valueobject.MediaType;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StorageKeyGenerator {

    public String generate(MediaType mediaType, String originalFileName) {

        LocalDate today = LocalDate.now();

        String directory = "%s/%d/%02d".formatted(mediaType.name().toLowerCase(Locale.ROOT),
                today.getYear(), today.getMonthValue());

        String filename = UUID.randomUUID() + getExtension(originalFileName);

        return directory + "/" + filename;
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');

        if (index < 0) {
            return "";
        }

        return fileName.substring(index).toLowerCase(Locale.ROOT);
    }
}

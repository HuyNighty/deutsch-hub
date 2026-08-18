package com.deutschhub.application.content.article.port.out;

import java.util.UUID;

public interface MediaLookupPort {

    boolean exists(UUID mediaId);

    boolean isUsableAsArticleCover(UUID mediaId);
}

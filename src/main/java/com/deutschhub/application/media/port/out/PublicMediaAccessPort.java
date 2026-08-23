package com.deutschhub.application.media.port.out;

import java.util.UUID;

public interface PublicMediaAccessPort {

    boolean isPubliclyAccessible(UUID mediaId);
}

package com.deutschhub.application.media.policy;

import com.deutschhub.application.media.port.out.PublicMediaAccessPort;
import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.application.shared.authorization.CurrentActorPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.enums.RoleType;
import com.deutschhub.domain.media.model.aggregate.Media;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MediaAccessPolicy {

    PublicMediaAccessPort publicMediaAccessPort;
    CurrentActorPort currentActorPort;

    public void requireCanRead(Media media) {

        if (publicMediaAccessPort.isPubliclyAccessible(media.getId())) {
            return;
        }

        CurrentActor actor = currentActorPort.getCurrentActor();

        if (actor == null) {
            throw new BusinessException(ErrorCode.MEDIA_ACCESS_DENIED);
        }

        if (actor.hasRole(RoleType.ADMIN)) {
            return;
        }

        if (actor.hasRole(RoleType.CONTENT_EDITOR)
                && media.getUploadedBy().equals(actor.userId().value())) {
            return;
        }

        throw new BusinessException(ErrorCode.MEDIA_ACCESS_DENIED);
    }
}

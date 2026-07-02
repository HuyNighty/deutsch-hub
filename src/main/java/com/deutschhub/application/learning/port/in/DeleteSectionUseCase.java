package com.deutschhub.application.learning.port.in;
import java.util.UUID;

public interface DeleteSectionUseCase {

    void deleteSection(UUID courseId, UUID sectionId, UUID actorId, boolean admin);
}

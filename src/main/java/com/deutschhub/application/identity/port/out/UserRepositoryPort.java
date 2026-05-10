package com.deutschhub.application.identity.port.out;

import com.deutschhub.domain.identity.model.aggregate.User;
import com.deutschhub.domain.identity.model.valueobject.Email;
import com.deutschhub.domain.identity.model.valueobject.Username;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    boolean existsByEmail(Email email);

    boolean existsByUsername(Username username);
}

package com.deutschhub.application.identity.port.out;

import com.deutschhub.domain.identity.model.aggregate.User;

public interface TokenGenerator {

    GeneratedToken generateAccessToken(User user);
}

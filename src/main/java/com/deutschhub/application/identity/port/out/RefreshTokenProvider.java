package com.deutschhub.application.identity.port.out;

public interface RefreshTokenProvider {

    GeneratedRefreshToken generate();

    String hash(String refreshToken);
}
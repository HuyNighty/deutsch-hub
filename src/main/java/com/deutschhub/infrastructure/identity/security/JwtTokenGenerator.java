package com.deutschhub.infrastructure.identity.security;

import com.deutschhub.application.identity.port.out.GeneratedToken;
import com.deutschhub.application.identity.port.out.TokenGenerator;
import com.deutschhub.domain.identity.aggregate.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class JwtTokenGenerator implements TokenGenerator {

    JwtProperties jwtProperties;

    @Override
    public GeneratedToken generateAccessToken(User user) {

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(jwtProperties.accessTokenExpiration());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
//                .claim(JwtClaims.EMAIL, user.getEmail().getValue())
                .claim(JwtClaims.ROLES, user.getRoles().stream().map(Enum::name).toList())
//                .claim(JwtClaims.USERNAME, user.getUsername().getValue())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();

        String token = jwtEncoder().encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new GeneratedToken(token, jwtProperties.accessTokenExpiration());
    }

    private JwtEncoder jwtEncoder() {
        SecretKey key = new SecretKeySpec(
                jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8),
                "HmacSHA512"
        );

        return new NimbusJwtEncoder(new ImmutableSecret<>(key.getEncoded()));
    }
}

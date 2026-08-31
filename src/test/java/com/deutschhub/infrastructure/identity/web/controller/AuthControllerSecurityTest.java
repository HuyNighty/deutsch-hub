package com.deutschhub.infrastructure.identity.web.controller;

import com.deutschhub.application.identity.dto.response.LoginResponse;
import com.deutschhub.application.identity.dto.response.RefreshTokenResponse;
import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.application.identity.port.in.GetMyProfileUseCase;
import com.deutschhub.application.identity.port.in.LoginUserUseCase;
import com.deutschhub.application.identity.port.in.LogoutUseCase;
import com.deutschhub.application.identity.port.in.RefreshTokenUseCase;
import com.deutschhub.application.identity.port.in.RegisterUserUseCase;
import com.deutschhub.common.util.MessageUtils;
import com.deutschhub.infrastructure.config.SecurityConfig;
import com.deutschhub.infrastructure.identity.security.JwtProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, properties = {
        "jwt.secret-key=0123456789012345678901234567890123456789012345678901234567890123",
        "jwt.access-token-expiration=900000",
        "jwt.refresh-token-expiration=604800000"
})
@Import({SecurityConfig.class, AuthControllerSecurityTest.JwtPropertiesConfiguration.class})
class AuthControllerSecurityTest {

    private static final String JWT_SECRET = "0123456789012345678901234567890123456789012345678901234567890123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private LoginUserUseCase loginUserUseCase;

    @MockitoBean
    private GetMyProfileUseCase getMyProfileUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @MockitoBean
    private MessageUtils messageUtils;

    @Test
    void meWithoutAuthenticationReturnsUnauthorizedAndDoesNotInvokeUseCase() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());

        then(getMyProfileUseCase).shouldHaveNoInteractions();
    }

    @Test
    void meWithAuthenticatedJwtReturnsProfileForJwtSubject() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(
                userId, "huy", "huy@example.com", "Nguyen Quang Huy", null
        );
        given(getMyProfileUseCase.getMyProfile(userId)).willReturn(userResponse);

        mockMvc.perform(get("/api/v1/auth/me")
                        .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(userId.toString()));

        then(getMyProfileUseCase).should().getMyProfile(userId);
    }

    @Test
    void meWithInvalidOrExpiredBearerTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken()))
                .andExpect(status().isUnauthorized());

        then(getMyProfileUseCase).shouldHaveNoInteractions();
    }

    @Test
    void otherAuthEndpointsRemainPublic() throws Exception {
        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(), "huy", "huy@example.com", "Nguyen Quang Huy", null
        );
        given(registerUserUseCase.register(any())).willReturn(userResponse);
        given(loginUserUseCase.login(any())).willReturn(new LoginResponse(userResponse, "access", "refresh", 900_000));
        given(refreshTokenUseCase.refresh(any())).willReturn(new RefreshTokenResponse("access", "refresh", 900_000));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"huy","email":"huy@example.com","password":"password","firstName":"Huy","lastName":"Nguyen","phoneNumber":""}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usernameOrEmail":"huy","password":"password"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"refresh"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"refresh"}
                                """))
                .andExpect(status().isOk());

        then(logoutUseCase).should().logout(any());
    }

    private String expiredToken() {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now().minusSeconds(120))
                .expiresAt(Instant.now().minusSeconds(60))
                .build();

        return jwtEncoder().encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS512).build(), claims
        )).getTokenValue();
    }

    private JwtEncoder jwtEncoder() {
        SecretKey secretKey = new SecretKeySpec(
                JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA512"
        );
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey.getEncoded()));
    }

    @TestConfiguration
    @EnableConfigurationProperties(JwtProperties.class)
    static class JwtPropertiesConfiguration {
    }
}

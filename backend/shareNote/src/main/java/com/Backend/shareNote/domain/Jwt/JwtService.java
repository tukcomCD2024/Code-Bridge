package com.Backend.shareNote.domain.Jwt;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class JwtService {
    private SecretKey secretKey;
    public JwtService(@Value("${spring.jwt.invitation.secret}") String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String createInvitationToken(String organizationId) {
        return Jwts.builder()
                .claim("organizationId", organizationId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3)) //3일
                .signWith(secretKey)
                .compact();
    }

    public Optional<String> extractOrganizationId(String token) {

        try {
            log.info("token: {}",token);
            String organizationId;
            return Optional.of(Jwts.parser().verifyWith(secretKey).build().
                    parseSignedClaims(token).getPayload().get("organizationId", String.class));
        }catch (Exception e){
            e.printStackTrace();
            return Optional.empty();
        }

    }
}

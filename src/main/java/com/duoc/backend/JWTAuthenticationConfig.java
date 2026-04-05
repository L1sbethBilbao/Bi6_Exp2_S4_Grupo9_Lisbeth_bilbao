package com.duoc.backend;

import com.duoc.backend.security.JwtSigningKeyProvider;
import io.jsonwebtoken.Jwts;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class JWTAuthenticationConfig {

    private static final long TOKEN_TTL_MS = 1000L * 60 * 60 * 24;

    private final JwtSigningKeyProvider jwtSigningKeyProvider;

    public JWTAuthenticationConfig(JwtSigningKeyProvider jwtSigningKeyProvider) {
        this.jwtSigningKeyProvider = jwtSigningKeyProvider;
    }

    public String getJWTToken(String username) {
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("ROLE_USER");

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", grantedAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        String token = Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TOKEN_TTL_MS))
                .and()
                .signWith(jwtSigningKeyProvider.getSecretKey())
                .compact();

        return "Bearer " + token;
    }
}

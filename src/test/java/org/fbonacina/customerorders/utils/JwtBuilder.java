package org.fbonacina.customerorders.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public interface JwtBuilder {

  String secret = "your-super-secret-key-that-is-256-bits-long!!"; // must match JwtService
  Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

  long EXPIRATION = 1000 * 60 * 60;

  default String createJwt(String username, Key key) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
        .claim("roles", List.of("USER"))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  default Authentication createAuthentication(String username) {
    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            createJwt(username, key), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    // auth.setAuthenticated(true);
    return auth;
  }

  default SecurityContext createSecurityContext() {
    var securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(createAuthentication("test-username"));
    return securityContext;
  }
}

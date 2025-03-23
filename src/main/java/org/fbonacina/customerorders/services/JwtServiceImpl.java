package org.fbonacina.customerorders.services;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {
  private static final long EXPIRATION = 1000 * 60 * 60;
  private static final Key KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

  @Override
  public String generateToken(String username, String role) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
        .signWith(KEY)
        .claim("roles", List.of(role))
        .compact();
  }

  @Override
  public String extractUsername(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(KEY)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  @Override
  public boolean isTokenValid(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }

  public List<String> extractRoles(String token) {
    var claims = Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token).getBody();
    return claims.get("roles", List.class);
  }
}

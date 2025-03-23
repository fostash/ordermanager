package org.fbonacina.customerorders.services;

import java.util.List;

public interface JwtService {

  String generateToken(String username, String role);

  String extractUsername(String token);

  boolean isTokenValid(String token);

  List<String> extractRoles(String token);
}

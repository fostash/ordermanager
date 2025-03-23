package org.fbonacina.customerorders.auth;

import org.fbonacina.customerorders.dto.LoginRequest;
import org.fbonacina.customerorders.services.JwtService;
import org.fbonacina.customerorders.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final JwtService jwtService;
  private final UserService userService;

  @Autowired
  public AuthController(JwtService jwtService, UserService userService) {
    this.jwtService = jwtService;
    this.userService = userService;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    return userService
        .findByUsernameAndPassword(request.username(), request.password())
        .map(
            user -> {
              var token = jwtService.generateToken(request.username(), user.getRole());
              return ResponseEntity.ok(token);
            })
        .orElse(ResponseEntity.status(401).body("Invalid credentials"));
  }
}

package org.fbonacina.customerorders.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import org.fbonacina.customerorders.services.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthFilter implements Filter {

  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    var request = (HttpServletRequest) req;
    var authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      var token = authHeader.substring(7);

      if (jwtService.isTokenValid(token)) {
        var username = jwtService.extractUsername(token);
        var roles = jwtService.extractRoles(token);

        var authorities =
            roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
      } else {
        ((HttpServletResponse) res)
            .sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header");
        return;
      }
    } else {
      ((HttpServletResponse) res)
          .sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
      return;
    }

    chain.doFilter(req, res);
  }
}

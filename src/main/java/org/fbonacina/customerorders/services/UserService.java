package org.fbonacina.customerorders.services;

import java.util.Optional;
import org.fbonacina.customerorders.model.User;

public interface UserService {

  Optional<User> findByUsernameAndPassword(String username, String password);

  Optional<User> findById(Long userId);
}

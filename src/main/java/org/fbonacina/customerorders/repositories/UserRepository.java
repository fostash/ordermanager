package org.fbonacina.customerorders.repositories;

import java.util.Optional;
import org.fbonacina.customerorders.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsernameAndPassword(String username, String password);
}

package org.fbonacina.customerorders.services;

import java.util.Optional;
import org.fbonacina.customerorders.model.User;
import org.fbonacina.customerorders.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<User> findByUsernameAndPassword(String username, String password) {
    try {
      return userRepository.findByUsernameAndPassword(username, password);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<User> findById(Long userId) {
    try {
      return userRepository.findById(userId);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

package com.wms.ems.security.service;

import com.wms.ems.security.model.User;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    Optional<User> authenticate(String username, String password);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
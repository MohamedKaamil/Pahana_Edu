package com.pahana.bookshop.dao.interfaces;

import com.pahana.bookshop.model.User;
import java.util.Optional;

public interface UserDAO extends GenericDAO<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPassword(String username, String password);
    boolean existsByUsername(String username);
}
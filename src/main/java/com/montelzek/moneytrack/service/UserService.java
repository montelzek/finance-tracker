package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Optional<User> findById(Long id);

    Long getCurrentUserId();

    List<User> findAll();

    void deleteById(Long id);

    User save(User user);

    Boolean existsByEmail(String email);

    void grantPremiumRole(Long userId);

    void revokePremiumRole(Long userId);
}

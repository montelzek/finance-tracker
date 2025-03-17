package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findById(Long id);

    Long getCurrentUserId();
}

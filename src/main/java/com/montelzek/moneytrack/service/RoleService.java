package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Role;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface RoleService {

    Optional<Role> findByName(Role.ERole name);
}

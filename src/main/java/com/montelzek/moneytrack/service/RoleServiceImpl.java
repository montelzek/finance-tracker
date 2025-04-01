package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Role;
import com.montelzek.moneytrack.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService{

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> findByName(Role.ERole name) {
        return roleRepository.findByName(name);

    }
}

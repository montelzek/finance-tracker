package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Role;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleService roleService;

    public UserServiceImpl(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new IllegalArgumentException("Admin cannot delete themselves.");
        }

        userRepository.deleteById(id);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void grantPremiumRole(Long userId) {
        updateUserRole(userId, true);
    }

    @Override
    @Transactional
    public void revokePremiumRole(Long userId) {
        updateUserRole(userId, false);
    }

    private Role getRole(Role.ERole roleName) {
        return roleService.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found."));
    }

    private void updateUserRole(Long userId, boolean addRole) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        Set<Role> roles = user.getRoles();

        if (roles.contains(getRole(Role.ERole.ROLE_ADMIN))) {
            return;
        }

        Role targetRole = getRole(Role.ERole.ROLE_PREMIUM);

        boolean changed = addRole ? roles.add(targetRole) : roles.remove(targetRole);

        if (changed) {
            userRepository.save(user);
        }
    }
}
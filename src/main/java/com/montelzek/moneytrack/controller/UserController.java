package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.model.Role;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin-panel")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("ERole", Role.ERole.class);
        return "admin-panel";
    }

    @GetMapping("/user/delete")
    public String deleteUser(@RequestParam("userId") Long id) {
        userService.deleteById(id);
        return "redirect:/admin-panel";
    }

    @PostMapping("/user/grant-premium")
    public String grantPremium(@RequestParam("userId") Long id) {
        userService.grantPremiumRole(id);
        return "redirect:/admin-panel";
    }

    @PostMapping("/user/revoke-premium")
    public String revokePremium(@RequestParam("userId") Long id) {
        userService.revokePremiumRole(id);
        return "redirect:/admin-panel";
    }
}

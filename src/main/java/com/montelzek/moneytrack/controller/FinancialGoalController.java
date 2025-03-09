package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.FinancialGoalDTO;
import com.montelzek.moneytrack.model.FinancialGoal;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.service.FinancialGoalService;
import com.montelzek.moneytrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/financialGoals")
public class FinancialGoalController {

    private final FinancialGoalService financialGoalService;
    private final UserService userService;

    public FinancialGoalController(FinancialGoalService financialGoalService, UserService userService) {
        this.financialGoalService = financialGoalService;
        this.userService = userService;
    }

    @GetMapping
    public String listFinancialGoal(Model model) {

        FinancialGoalDTO financialGoalDTO = new FinancialGoalDTO();
        model.addAttribute("financialGoal", financialGoalDTO);
        prepareFinancialGoalModel(model);
        return "financialGoals/list";
    }

    @PostMapping("/save")
    public String saveFinancialGoal(@Valid @ModelAttribute("financialGoal") FinancialGoalDTO financialGoalDTO,
                             BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("financialGoal", financialGoalDTO);
            prepareFinancialGoalModel(model);
            return "financialGoals/list";
        }

        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        FinancialGoal financialGoal = new FinancialGoal(
                financialGoalDTO.getName(),
                financialGoalDTO.getTargetAmount()
        );
        financialGoal.setUser(user);
        financialGoal.setCurrentAmount(0.0);
        financialGoal.setIsAchieved(false);

        financialGoalService.save(financialGoal);

        return "redirect:/financialGoals";
    }

    private void prepareFinancialGoalModel(Model model) {

        Long id = userService.getCurrentUserId();
        List<FinancialGoal> financialGoalList = financialGoalService.findUsersFinancialGoals(id);
        model.addAttribute("financialGoals", financialGoalList);
    }
}

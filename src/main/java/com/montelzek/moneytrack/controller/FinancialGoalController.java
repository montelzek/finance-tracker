package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.FinancialGoalDTO;
import com.montelzek.moneytrack.model.FinancialGoal;
import com.montelzek.moneytrack.service.FinancialGoalService;
import com.montelzek.moneytrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

        try {
            financialGoalService.saveFinancialGoal(financialGoalDTO);
            return "redirect:/financialGoals";
        } catch (IllegalArgumentException | IllegalStateException e) {
            result.rejectValue("", "error.general", e.getMessage());
            prepareFinancialGoalModel(model);
            return "financialGoals/list";
        }
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public FinancialGoalDTO getFinancialGoalForEdit(@PathVariable Long id) {

        FinancialGoal financialGoal = financialGoalService.findById(id);
        return financialGoalService.convertToDTO(financialGoal);
    }

    @GetMapping("/delete")
    public String deleteFinancialGoal(@RequestParam("financialGoalId") Long id) {
        financialGoalService.deleteById(id);
        return "redirect:/financialGoals";
    }

    private void prepareFinancialGoalModel(Model model) {

        Long id = userService.getCurrentUserId();
        List<FinancialGoal> financialGoalList = financialGoalService.findUsersFinancialGoals(id);
        model.addAttribute("financialGoals", financialGoalList);
    }
}
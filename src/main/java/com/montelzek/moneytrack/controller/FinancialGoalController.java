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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @PostMapping("/create")
    public String saveFinancialGoal(@Valid @ModelAttribute("createFinancialGoal") FinancialGoalDTO financialGoalDTO,
                             BindingResult result, Model model) {

        if (result.hasErrors()) {
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
        financialGoal.setCurrentAmount(BigDecimal.ZERO);
        financialGoal.setIsAchieved(false);

        financialGoalService.save(financialGoal);

        return "redirect:/financialGoals";
    }

    @PostMapping("/update")
    public String updateFinancialGoal(@Valid @ModelAttribute("editFinancialGoal") FinancialGoalDTO financialGoalDTO,
                                      BindingResult result, Model model) {

        if (result.hasErrors()) {
            prepareFinancialGoalModel(model);
            return "financialGoals/list";
        }

        FinancialGoal financialGoal = financialGoalService.findById(financialGoalDTO.getId());

        financialGoal.setName(financialGoalDTO.getName());
        financialGoal.setTargetAmount(financialGoalDTO.getTargetAmount());

        if (financialGoal.getCurrentAmount().compareTo(financialGoal.getTargetAmount()) >= 0 && !financialGoal.getIsAchieved()) {
            financialGoal.setIsAchieved(true);
        } else if (financialGoal.getCurrentAmount().compareTo(financialGoal.getTargetAmount()) < 0 && financialGoal.getIsAchieved()) {
            financialGoal.setIsAchieved(false);
        }

        financialGoalService.save(financialGoal);

        return "redirect:/financialGoals";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public FinancialGoalDTO getFinancialGoalForEdit(@PathVariable Long id) {

        FinancialGoal financialGoal = financialGoalService.findById(id);

        FinancialGoalDTO financialGoalDTO = new FinancialGoalDTO();
        financialGoalDTO.setId(financialGoal.getId());
        financialGoalDTO.setName(financialGoal.getName());
        financialGoalDTO.setTargetAmount(financialGoal.getTargetAmount());
        return financialGoalDTO;
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

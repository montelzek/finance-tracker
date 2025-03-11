package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.CreateFinancialGoalDTO;
import com.montelzek.moneytrack.dto.EditFinancialGoalDTO;
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

        CreateFinancialGoalDTO createFinancialGoalDTO = new CreateFinancialGoalDTO();
        EditFinancialGoalDTO editFinancialGoalDTO = new EditFinancialGoalDTO();

        model.addAttribute("createFinancialGoal", createFinancialGoalDTO);
        model.addAttribute("editFinancialGoal", editFinancialGoalDTO);

        prepareFinancialGoalModel(model);
        return "financialGoals/list";
    }

    @PostMapping("/create")
    public String saveFinancialGoal(@Valid @ModelAttribute("createFinancialGoal") CreateFinancialGoalDTO createFinancialGoalDTO,
                             BindingResult result, Model model) {

        if (result.hasErrors()) {
            prepareFinancialGoalModel(model);
            return "financialGoals/list";
        }

        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));


        FinancialGoal financialGoal = new FinancialGoal(
                createFinancialGoalDTO.getName(),
                createFinancialGoalDTO.getTargetAmount()
        );
        financialGoal.setUser(user);
        financialGoal.setCurrentAmount(BigDecimal.ZERO);
        financialGoal.setIsAchieved(false);

        financialGoalService.save(financialGoal);

        return "redirect:/financialGoals";
    }

    @PostMapping("/update")
    public String updateFinancialGoal(@Valid @ModelAttribute("editFinancialGoal") EditFinancialGoalDTO editFinancialGoalDTO,
                                      BindingResult result, Model model) {

        if (result.hasErrors()) {
            prepareFinancialGoalModel(model);
            return "financialGoals/list";
        }

        FinancialGoal financialGoal = financialGoalService.findById(editFinancialGoalDTO.getId());
        financialGoal.setName(editFinancialGoalDTO.getName());
        financialGoal.setTargetAmount(editFinancialGoalDTO.getTargetAmount());
        financialGoal.setCurrentAmount(editFinancialGoalDTO.getCurrentAmount());

        if (editFinancialGoalDTO.getCurrentAmount().compareTo(editFinancialGoalDTO.getTargetAmount()) >= 0 && !financialGoal.getIsAchieved()) {
            financialGoal.setIsAchieved(true);
        } else if (editFinancialGoalDTO.getCurrentAmount().compareTo(editFinancialGoalDTO.getTargetAmount()) < 0 && financialGoal.getIsAchieved()) {
            financialGoal.setIsAchieved(false);
        }

        financialGoalService.save(financialGoal);

        return "redirect:/financialGoals";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public EditFinancialGoalDTO getFinancialGoalForEdit(@PathVariable Long id) {

        FinancialGoal financialGoal = financialGoalService.findById(id);

        EditFinancialGoalDTO editFinancialGoalDTO = new EditFinancialGoalDTO();
        editFinancialGoalDTO.setId(financialGoal.getId());
        editFinancialGoalDTO.setName(financialGoal.getName());
        editFinancialGoalDTO.setTargetAmount(financialGoal.getTargetAmount());
        editFinancialGoalDTO.setCurrentAmount(financialGoal.getCurrentAmount());
        return editFinancialGoalDTO;
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

package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.BudgetDTO;
import com.montelzek.moneytrack.model.Budget;
import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.service.BudgetService;
import com.montelzek.moneytrack.service.CategoryService;
import com.montelzek.moneytrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserService userService;
    private final CategoryService categoryService;

    public BudgetController(BudgetService budgetService, UserService userService, CategoryService categoryService) {
        this.budgetService = budgetService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listBudget(Model model) {

        BudgetDTO budgetDTO = new BudgetDTO();
        model.addAttribute("budget", budgetDTO);
        prepareBudgetModel(model);
        return "budgets/list";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public BudgetDTO getBudgetForEdit(@PathVariable Long id) {

        Budget budget = budgetService.findById(id);
        return budgetService.convertToDTO(budget);
    }

    @PostMapping("/save")
    public String saveBudget(@Valid @ModelAttribute("budget") BudgetDTO budgetDTO,
                             BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("budget", budgetDTO);
            prepareBudgetModel(model);
            return "budgets/list";
        }

        try {
            budgetService.saveBudget(budgetDTO);
            return "redirect:/budgets";
        } catch (IllegalArgumentException | IllegalStateException e) {
            result.rejectValue("", "error.general", e.getMessage());
            prepareBudgetModel(model);
            return "budgets/list";
        }
    }

    @GetMapping("/delete")
    public String deleteBudget(@RequestParam("budgetId") Long id) {
        budgetService.deleteById(id);
        return "redirect:/budgets";
    }

    private void prepareBudgetModel(Model model) {

        Long id = userService.getCurrentUserId();
        List<Budget> budgetList = budgetService.findUsersBudgets(id);
        List<Category> expenseCategories = categoryService.findByType("EXPENSE");
        model.addAttribute("budgets", budgetList);
        model.addAttribute("expenseCategories", expenseCategories);
    }
}

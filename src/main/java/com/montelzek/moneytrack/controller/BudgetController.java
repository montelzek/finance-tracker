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

        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setId(budget.getId());
        budgetDTO.setName(budget.getName());
        budgetDTO.setStartDate(budget.getStartDate());
        budgetDTO.setEndDate(budget.getEndDate());
        budgetDTO.setBudgetSize(budget.getBudgetSize());
        budgetDTO.setCategoryId(budget.getCategory().getId());
        return budgetDTO;
    }

    @PostMapping("/save")
    public String saveBudget(@Valid @ModelAttribute("budget") BudgetDTO budgetDTO,
                             BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("budget", budgetDTO);
            prepareBudgetModel(model);
            return "budgets/list";
        }

        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Category category = categoryService.findById(budgetDTO.getCategoryId());

        Budget budget;
        if (budgetDTO.getId() != null) {
            budget = budgetService.findById(budgetDTO.getId());
            budget.setName(budgetDTO.getName());
            budget.setStartDate(budgetDTO.getStartDate());
            budget.setEndDate(budgetDTO.getEndDate());
            budget.setBudgetSize(budgetDTO.getBudgetSize());
            budget.setCategory(category);
        } else {
            budget = new Budget(
                    budgetDTO.getName(),
                    budgetDTO.getStartDate(),
                    budgetDTO.getEndDate(),
                    budgetDTO.getBudgetSize()
            );
            budget.setUser(user);
            budget.setCategory(category);
            budget.setBudgetSpent(0.0);
        }
        budgetService.save(budget);

        return "redirect:/budgets";
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

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

        Long id = userService.getCurrentUserId();
        List<Budget> budgetList = budgetService.findUsersBudgets(id);
        List<Category> expenseCategories = categoryService.findByType("EXPENSE");
        BudgetDTO budgetDTO = new BudgetDTO();

        model.addAttribute("budget", budgetDTO);
        model.addAttribute("budgets", budgetList);
        model.addAttribute("expenseCategories", expenseCategories);

        return "budgets/list";
    }

    @PostMapping("/save")
    public String saveBudget(@Valid @ModelAttribute("budget") BudgetDTO budgetDTO,
                             BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "accounts/list";
        }

        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Category category = categoryService.findById(budgetDTO.getCategoryId());

        Budget budget = new Budget(
                budgetDTO.getName(),
                budgetDTO.getStartDate(),
                budgetDTO.getEndDate(),
                budgetDTO.getBudgetSize()
        );
        budget.setUser(user);
        budget.setCategory(category);
        budget.setBudgetSpent(0.0);

        budgetService.save(budget);

        return "redirect:/budgets";
    }
}

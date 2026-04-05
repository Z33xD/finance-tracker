package com.fintrack.finance_tracker.budgets;

import com.fintrack.finance_tracker.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(path = "api/budgets")
public class BudgetController {
    private final BudgetService budgetService;

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // TODO: GET /api/budgets (Retrieve all budgets for the authenticated user)

    @GetMapping
    public List<Budget> getBudgets(
            @RequestParam (required = false) Integer month,
            @RequestParam (required = false) Integer year
    ) {
        User currentUser = getAuthenticatedUser();

        if ((month != null) && (year != null)) {
            return budgetService.getBudgetsByUserIdAndMonthAndYear(currentUser.getId(), month, year);
        }

        return budgetService.getBudgetsByUserId(currentUser.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(@PathVariable int id) {
        User currentUser = getAuthenticatedUser();

        Budget budget = budgetService.getBudgetById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (budget.getUser_id() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(budget);
    }

    @PostMapping
    public ResponseEntity<Budget> addBudget(@RequestBody Budget budget) {
        User currentUser = getAuthenticatedUser();
        budget.setUser_id(currentUser.getId());
        Budget createdBudget = budgetService.addBudget(budget);
        return new ResponseEntity<>(createdBudget, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudgetById(@PathVariable int id, @RequestBody Budget budget) {
        User currentUser = getAuthenticatedUser();
        Budget existing = budgetService.getBudgetById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (existing.getUser_id() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        budget.setUser_id(currentUser.getId());
        Budget updatedBudget = budgetService.updateBudget(id, budget);
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBudget(@PathVariable int id) {
        User currentUser = getAuthenticatedUser();

        Budget existing = budgetService.getBudgetById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (existing.getUser_id() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        budgetService.deleteBudget(id);
        return new ResponseEntity<>("Budget deleted successfully!", HttpStatus.OK);
    }

    // Helper method to get the authenticated user
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return (User) authentication.getPrincipal();
    }
}

package com.fintrack.finance_tracker.budgets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "api/budgets")
public class BudgetController {
    private final BudgetService budgetService;

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // TODO: GET /api/budgets (Retrieve all budgets for the authenticated user)

    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(@PathVariable int id) {
        return budgetService.getBudgetById(id)
                .map(budget -> new ResponseEntity<>(budget, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Budget> addBudget(@RequestBody Budget budget) {
        Budget createdBudget = budgetService.addBudget(budget);
        return new ResponseEntity<>(createdBudget, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudgetById(@PathVariable int id, @RequestBody Budget budget) {
        Budget updatedBudget = budgetService.updateBudget(id, budget);

        if (updatedBudget != null) {
            return new ResponseEntity<>(updatedBudget, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBudget(@PathVariable int id) {
        budgetService.deleteBudget(id);
        return new ResponseEntity<>("Budget deleted successfully!", HttpStatus.OK);
    }
}

package com.fintrack.finance_tracker.categories;

import com.fintrack.finance_tracker.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/categories/")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Category> getCategories(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String name
    ) {
        User currentUser = getAuthenticatedUser();

        if (id != null) {
            Category category = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            if (category.getUser_id() != currentUser.getId()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            return List.of(category);
        }

        else if (name != null) {
            return categoryService.getCategoriesByUserId(currentUser.getId()).stream()
                    .filter(category -> category.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }

        else {
            return categoryService.getCategoriesByUserId(currentUser.getId());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoriesById(@PathVariable int id) {
        User currentUser = getAuthenticatedUser();
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (category.getUser_id() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        User currentUser = getAuthenticatedUser();
        category.setUser_id(currentUser.getId());

        Category createdCategory = categoryService.addCategory(category);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Category> updateCategoryById(@PathVariable int id, @RequestBody Category category) {
        User currentUser = getAuthenticatedUser();

        Category existing = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (existing.getUser_id() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Category updatedCategory = categoryService.updateCategory(id, category);
        if (updatedCategory != null) {
            return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable int id) {
        User currentUser = getAuthenticatedUser();

        Category existing = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (existing.getUser_id() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        categoryService.deleteCategory(id);
        return new ResponseEntity<>("Category deleted successfully!", HttpStatus.OK);
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

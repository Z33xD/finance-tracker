package com.fintrack.finance_tracker.categories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(path = "/api/categories")
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
        if (id != null) {
            return categoryService.getCategoryById(id)
                    .map(List::of)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        else if (name != null) {
            return categoryService.getCategoriesByName(name);
        }

        else {
            return categoryService.getCategories();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoriesById(@PathVariable int id) {
        return categoryService.getCategoryById(id)
                .map(category -> new ResponseEntity<>(category, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        Category createdCategory = categoryService.addCategory(category);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Category> updateCategoryById(@PathVariable int id, @RequestBody Category category) {
        Category updatedCategory = categoryService.updateCategory(id, category);
        if (category != null) {
            return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(updatedCategory, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable int id) {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>("Category deleted successfully!", HttpStatus.OK);
    }
}
package com.fintrack.finance_tracker.categories;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(int searchKey) {
        return categoryRepository.findById(searchKey);
    }

    public List<Category> getCategoriesByName(String searchText) {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getName().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Category addCategory(Category category) {
        categoryRepository.save(category);
        return category;
    }

    public Category updateCategory(int id, Category updatedCategory) {
        Optional<Category> existingCategory = categoryRepository.findById(id);

        if (existingCategory.isPresent()) {
            Category categoryToUpdate = existingCategory.get();

            if (updatedCategory.getName() != null) {
                categoryToUpdate.setName(updatedCategory.getName());
            }

            if (updatedCategory.getType() != null) {
                categoryToUpdate.setType(updatedCategory.getType());
            }

            if (updatedCategory.getIcon() != null) {
                categoryToUpdate.setIcon(updatedCategory.getIcon());
            }

            if (updatedCategory.getColour() != null) {
                categoryToUpdate.setColour(updatedCategory.getColour());
            }

            categoryRepository.save(categoryToUpdate);
            return categoryToUpdate;
        }
        return null;
    }

    @Transactional
    public void deleteCategory(int id) {
        categoryRepository.deleteById(id);
    }
}

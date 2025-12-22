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

    public Category updateCategory(Category updatedCategory) {
        Optional<Category> existingCategory = categoryRepository.findById(updatedCategory.getId());

        if (existingCategory.isPresent()) {
            Category categoryToUpdate = existingCategory.get();
            categoryToUpdate.setName(updatedCategory.getName());
            categoryToUpdate.setType(updatedCategory.getType());
            categoryToUpdate.setIcon(updatedCategory.getIcon());
            categoryToUpdate.setColour(updatedCategory.getColour());

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

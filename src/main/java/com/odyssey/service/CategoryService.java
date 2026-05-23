package com.odyssey.service;

import com.odyssey.model.Category;
import com.odyssey.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category save(Category category) {
        if (categoryRepository.existsByName(category.getName()))
            throw new RuntimeException("Category already exists: " + category.getName());
        return categoryRepository.save(category);
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }

    public Category update(Long id, Category updated) {
        Category existing = findById(id);
        existing.setName(updated.getName());
        existing.setIcon(updated.getIcon());
        existing.setColor(updated.getColor());
        return categoryRepository.save(existing);
    }

    public void delete(Long id) {
        findById(id);
        categoryRepository.deleteById(id);
    }
}

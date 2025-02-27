package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;


    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<Category> findByType(String type) {
        return categoryRepository.findByType(type);
    }

    public Category findById(Integer id) {

        Optional<Category> result = categoryRepository.findById(id);
        Category category = null;

        if (result.isPresent()) {
            category = result.get();
        } else {
            throw new RuntimeException("Did not find account of id: " + id);
        }

        return category;
    }
}

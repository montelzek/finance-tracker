package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Category;
import java.util.List;

public interface CategoryService {
    List<Category> findByType(String type);
    Category findById(Integer id);
    List<Category> findAll();
}

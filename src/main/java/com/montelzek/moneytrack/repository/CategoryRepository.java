package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByType(String type);
}

package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}

package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {

    List<FinancialGoal> findByUserId_OrderByCreatedAt(Long userId);
}

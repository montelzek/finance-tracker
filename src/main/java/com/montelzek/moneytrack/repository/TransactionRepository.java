package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount_User_Id_OrderByCreatedAt(Long userId);
}

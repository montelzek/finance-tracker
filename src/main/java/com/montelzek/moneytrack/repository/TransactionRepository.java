package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount_User_Id_OrderByCreatedAt(Long userId);
}

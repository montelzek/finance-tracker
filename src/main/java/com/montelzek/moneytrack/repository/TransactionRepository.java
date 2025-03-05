package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount_User_Id_OrderByCreatedAt(Long userId);

    @Query("SELECT t FROM Transaction t " +
            "JOIN t.account a " +
            "JOIN t.category c " +
            "JOIN a.user u " +
            "WHERE c.type = 'INCOME' " +
            "AND u.id = :userId " +
            "AND t.date >= CURRENT_DATE - 30 DAY")
    List<Transaction> findIncomeTransactionsFromPastMonth(Long userId);

    @Query("SELECT t FROM Transaction t " +
            "JOIN t.account a " +
            "JOIN t.category c " +
            "JOIN a.user u " +
            "WHERE c.type = 'EXPENSE' " +
            "AND u.id = :userId " +
            "AND t.date >= CURRENT_DATE - 30 DAY")
    List<Transaction> findExpenseTransactionsFromPastMonth(Long userId);
}

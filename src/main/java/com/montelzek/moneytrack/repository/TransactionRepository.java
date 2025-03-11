package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccount_User_Id_OrderByDateDescCreatedAtDesc(Long userId, Pageable pageable);

    List<Transaction> findTop6ByAccount_User_Id_OrderByDateDesc(Long userId);

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

    @Query("SELECT t FROM Transaction t " +
            "JOIN t.account a " +
            "JOIN t.category c " +
            "JOIN a.user u " +
            "WHERE u.id = :userId " +
            "AND (c.type = 'INCOME' OR c.type = 'EXPENSE') " +
            "AND t.date >= :startDate")
    List<Transaction> findTransactionsFromLastSixMonths(Long userId, LocalDate startDate);
}

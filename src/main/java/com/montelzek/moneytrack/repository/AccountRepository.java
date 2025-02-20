package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);

    @Query("SELECT a FROM Account a WHERE a.user.id = :userId ORDER BY a.createdAt ASC")
    List<Account> findByUserIdOrderByCreatedAt(Long userId);
}

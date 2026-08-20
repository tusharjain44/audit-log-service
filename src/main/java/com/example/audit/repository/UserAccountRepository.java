package com.example.audit.repository;
import com.example.audit.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserAccountRepository extends JpaRepository<UserAccount, String> {}
package com.example.app.repository;

import com.example.app.entity.AccountActivity;
import com.example.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountActivityRepository extends JpaRepository<AccountActivity, Long> {
    List<AccountActivity> findByUser(User user);
}

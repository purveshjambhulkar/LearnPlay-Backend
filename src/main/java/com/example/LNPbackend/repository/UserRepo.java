package com.example.LNPbackend.repository;

import com.example.LNPbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User , Long> {
    User findByUsername(String username);

    List<User> findAllByOrderByTotalScoreDesc();
}

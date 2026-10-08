package com.ty.userservice.repository;

import com.ty.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User,Integer> {
    boolean existsByPhone(String phone);
    User findByEmail(String email);
    boolean existsByEmail(String email);
}


package com.ty.userservice.service;

import com.ty.userservice.entity.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    List<User> getAllUser();
    User getUserById(Integer id);
    User deleteUser(Integer id);
    User updateUser(Integer id);
    User login(String email, String password);
}

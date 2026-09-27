package com.ty.userservice.implementation;

import com.ty.userservice.entity.User;
import com.ty.userservice.repository.UserRepo;
import com.ty.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserImpl implements UserService {
    @Autowired
    private UserRepo repo;
    @Override
    public User createUser(User user) {

        if (repo.existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone number already registered");
        }

        return repo.save(user);
    }

    @Override
    public List<User> getAllUser() {
        return repo.findAll();
    }

    @Override
    public User getUserById(Integer id) {
        return repo.findById(id).get();
    }

    @Override
    public User deleteUser(Integer id) {

        User user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        repo.deleteById(id);

        return user;
    }

    @Override
    public User updateUser(Integer id) {
        return null;
    }

    @Override
    public User login(String email, String password) {
        User user = repo.findByEmail(email);
        if(user != null && user.getPassword().equals(password)){
            return user;
        }
        return null;
    }
}

package com.ty.userservice.controller;


import com.ty.userservice.entity.User;
import com.ty.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/user")
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/create")
    public ResponseEntity<User> create(@RequestBody User user){
        User u = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    @GetMapping("/getall")
    public ResponseEntity<List<User>> getall(){
        List<User> getall=userService.getAllUser();
        return ResponseEntity.status(HttpStatus.OK).body(getall);
    }
    @GetMapping("/getbyid/{Id}")
    public ResponseEntity<User> getbyId(@PathVariable Integer id){
        User user = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestParam String email,@RequestParam String password){
        User user = userService.login(email, password);
        if(user != null){
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}

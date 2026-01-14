package com.example.todoapp.controller;

import com.example.todoapp.entity.User;
import com.example.todoapp.repository.UserRepository;
import com.example.todoapp.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtService jwt;

    @Autowired
    private PasswordEncoder passwordEncoder; // use Spring bean

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // encode with bean
        userRepo.save(user);
        return "Registered successfully!";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        User u = userRepo.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(passwordEncoder.matches(user.getPassword(), u.getPassword())) {
            return jwt.generate(u.getUsername()); // this now works
        }

        throw new RuntimeException("Invalid credentials");
    }

}


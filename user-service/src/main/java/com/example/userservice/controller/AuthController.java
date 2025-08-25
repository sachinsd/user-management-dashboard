package com.example.userservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> loginRequest) {
        // TODO: Implement login
        return new HashMap<>();
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh(@RequestBody Map<String, Object> refreshRequest) {
        // TODO: Implement token refresh
        return new HashMap<>();
    }
}

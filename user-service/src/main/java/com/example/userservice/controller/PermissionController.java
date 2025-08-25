package com.example.userservice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permissions")
public class PermissionController {
    @GetMapping
    public List<Map<String, Object>> listPermissions() {
        // TODO: Implement permission listing
        return new ArrayList<>();
    }
}

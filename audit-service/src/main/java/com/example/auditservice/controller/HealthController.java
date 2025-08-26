package com.example.auditservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/healthz")
    public Map<String, String> healthz() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        return status;
    }
}

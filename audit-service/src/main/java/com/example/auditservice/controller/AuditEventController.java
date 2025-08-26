package com.example.auditservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit/events")
public class AuditEventController {
    @GetMapping
    public List<Map<String, Object>> listAuditEvents() {
        // TODO: Implement audit event listing
        /*
         * @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
         */
        return List.of(Map.of("audit", "event"));
    }
}

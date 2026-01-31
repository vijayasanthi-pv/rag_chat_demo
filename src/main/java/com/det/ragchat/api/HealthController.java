package com.det.ragchat.api;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @Operation(summary = "Simple health endpoint (also see /actuator/health)")
    @GetMapping("/api/v1/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok");
    }
}

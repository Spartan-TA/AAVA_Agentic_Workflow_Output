package com.example.mobile;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mobile")
public class MobileController {
    @GetMapping("/status")
    public String getStatus() {
        return "Mobile API is running";
    }
}
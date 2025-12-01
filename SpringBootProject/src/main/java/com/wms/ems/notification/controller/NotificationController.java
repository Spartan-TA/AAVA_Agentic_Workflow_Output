package com.wms.ems.notification.controller;

import com.wms.ems.notification.dto.NotificationDto;
import com.wms.ems.notification.dto.PreferencesDto;
import com.wms.ems.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for notifications management")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "Get notifications for user")
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public ResponseEntity<List<NotificationDto>> getNotifications(Principal principal) {
        return ResponseEntity.ok(notificationService.getNotifications(principal.getName()));
    }

    @Operation(summary = "Update notification preferences")
    @PostMapping("/preferences")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public ResponseEntity<?> updatePreferences(@Valid @RequestBody PreferencesDto dto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        notificationService.updatePreferences(principal.getName(), dto);
        return ResponseEntity.ok().build();
    }
}

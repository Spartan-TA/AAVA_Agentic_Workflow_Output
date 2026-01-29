package com.warehouse.employee.management.controller;

import com.warehouse.employee.management.dto.LeaveDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/leaves")
@Validated
public class LeaveController {
    private final List<LeaveDto> leaves = new ArrayList<>();

    @PreAuthorize("hasAuthority('LEAVE_REQUEST')")
    @PostMapping("/request")
    public LeaveDto requestLeave(@Valid @RequestBody LeaveDto leaveDto) {
        leaveDto.setStatus("PENDING");
        leaves.add(leaveDto);
        return leaveDto;
    }

    @PreAuthorize("hasAuthority('LEAVE_APPROVE')")
    @PutMapping("/approve/{index}")
    public LeaveDto approveLeave(@PathVariable int index, @RequestParam Long approverId) {
        if (index < 0 || index >= leaves.size()) throw new IllegalArgumentException("Invalid index");
        LeaveDto leave = leaves.get(index);
        leave.setStatus("APPROVED");
        leave.setApproverId(approverId);
        return leave;
    }

    @PreAuthorize("hasAuthority('LEAVE_READ')")
    @GetMapping
    public List<LeaveDto> getLeaves() {
        return Collections.unmodifiableList(leaves);
    }
}

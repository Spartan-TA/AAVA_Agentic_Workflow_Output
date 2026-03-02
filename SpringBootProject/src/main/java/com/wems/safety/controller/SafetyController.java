package com.wems.safety.controller;

import com.wems.safety.domain.SafetyIncident;
import com.wems.safety.service.SafetyService;
import com.wems.employee.domain.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/safety")
public class SafetyController {
    @Autowired
    private SafetyService safetyService;

    @PostMapping("/incidents")
    public SafetyIncident reportIncident(@RequestBody SafetyIncident incident, @RequestParam Long reporterId) {
        Employee reporter = null; // TODO: resolve reporter
        return safetyService.reportIncident(reporter, incident);
    }

    @PostMapping("/{id}/investigate")
    public SafetyIncident startInvestigation(@PathVariable Long id, @RequestParam Long investigatorId) {
        Employee investigator = null; // TODO: resolve investigator
        return safetyService.startInvestigation(id, investigator);
    }

    @PostMapping("/{id}/resolve")
    public SafetyIncident completeInvestigation(@PathVariable Long id, @RequestParam String rootCause, @RequestParam String correctiveActions) {
        return safetyService.completeInvestigation(id, rootCause, correctiveActions);
    }

    @GetMapping("/osha-summary")
    public List<SafetyIncident> getOshaSummary() {
        return safetyService.generateOshaSummary();
    }
}

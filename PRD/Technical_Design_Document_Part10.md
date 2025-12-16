Section: E15-Reporting - Safety KPI Dashboard (User Story 46)
Description: Provide a dashboard displaying key safety performance indicators (KPIs) such as incidents, near-misses, and compliance rates.
Design Specification:
- Package: com.company.reporting.safety
- Entity: SafetyIncident, SafetyKpi
- Service: SafetyKpiService
- Controller: SafetyKpiController
- Dashboard: REST endpoints for frontend consumption
Sample Implementation:
@GetMapping("/reports/safety-kpi")
public ResponseEntity<SafetyKpiDto> getSafetyKpis(...) {
    // Aggregate and return KPIs
}

Section: E15-Reporting - Role-Based Report Access (User Story 47)
Description: Restrict access to reports based on user roles (e.g., Manager, Supervisor, Employee).
Design Specification:
- Security: Spring Security method-level @PreAuthorize
- Service: ReportAccessService
- Controller: Report endpoints with role checks
Sample Implementation:
@PreAuthorize("hasRole('MANAGER') or hasRole('SUPERVISOR')")
@GetMapping("/reports/secure")
public ResponseEntity<?> getSecureReport(...) {
    // Return report if authorized
}

Section: E16-Mobile PWA - Mobile Clock-In/Out (User Story 48)
Description: Enable employees to clock in and out via a mobile Progressive Web App (PWA), with geolocation capture.
Design Specification:
- Package: com.company.mobile.attendance
- Controller: MobileAttendanceController
- Service: MobileAttendanceService
- Entity: AttendanceRecord (reused), Geolocation
- Security: JWT/OAuth2 for mobile
Sample Implementation:
@PostMapping("/mobile/clock-in")
public ResponseEntity<?> clockIn(@RequestBody ClockInRequest req) {
    // Validate, save attendance with location
}

Section: E16-Mobile PWA - View Schedule on Mobile (User Story 49)
Description: Allow employees to view their work schedules on the mobile PWA.
Design Specification:
- Controller: MobileScheduleController
- Service: MobileScheduleService
- Entity: Schedule (reused)
- Endpoint: /mobile/schedule
Sample Implementation:
@GetMapping("/mobile/schedule")
public ResponseEntity<List<ScheduleDto>> getSchedule(...) {
    // Return schedules for user
}

Section: E16-Mobile PWA - Installable PWA (User Story 50)
Description: Make the mobile web app installable as a PWA, supporting offline access and push notifications.
Design Specification:
- Frontend: manifest.json, service worker
- Backend: Endpoints for push notifications
- Security: Token-based auth for push
Sample Implementation:
// Backend endpoint for push subscription
@PostMapping("/mobile/push/subscribe")
public ResponseEntity<?> subscribePush(@RequestBody PushSubscriptionDto dto) {
    // Store subscription
}

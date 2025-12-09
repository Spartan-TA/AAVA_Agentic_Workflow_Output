Title: Attendance Clock-In/Out Endpoint

User Story: As an employee I want to clock in and out via a dedicated API endpoint So that my attendance is accurately recorded in real time

Acceptance Criteria:
Scenario: Clocking in and out
Given I am authenticated
When I send a clock-in or clock-out request
Then my attendance status is updated and a timestamp is recorded

Additional Details:
- Priority: High
- Story Points: 5
- Dependencies: Attendance module, authentication
- Notes: Endpoint should validate duplicate clock-ins/outs and return confirmation
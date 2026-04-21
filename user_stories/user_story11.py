"""
Title: Leave Management â Request Time Off

User Story: As a warehouse employee, I want to request time off so that my absences are tracked and approved.

Acceptance Criteria:
- Scenario: Submitting a leave request
  Given I am logged in
  When I submit a time-off request
  Then my supervisor should be notified for approval
- Scenario: Supervisor approval
  Given a leave request is pending
  When the supervisor approves the request
  Then the employee's schedule should be updated

Additional Details:
- Priority: Medium
- Story Points: 8
- Dependencies: Employee profiles, shift scheduling
- Notes: Should support different leave types (sick, vacation).
"""
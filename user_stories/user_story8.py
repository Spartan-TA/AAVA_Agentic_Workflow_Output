"""
Title: Time and Attendance â Clock In/Out

User Story: As a warehouse employee, I want to clock in and out of my shifts so that my working hours are accurately recorded.

Acceptance Criteria:
- Scenario: Clocking in
  Given my shift is scheduled
  When I clock in at the start of my shift
  Then my attendance record should be updated with the clock-in time
- Scenario: Clocking out
  Given I am clocked in
  When I clock out at the end of my shift
  Then my attendance record should be updated with the clock-out time

Additional Details:
- Priority: High
- Story Points: 8
- Dependencies: Shift scheduling
- Notes: Should prevent early/late clock-ins based on company policy.
"""
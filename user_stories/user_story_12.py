Title: Leave Request Workflow

User Story: As an employee I want to submit leave requests and track approval status So that my time off is managed transparently

Acceptance Criteria:
Scenario: Submitting a leave request
Given I am authenticated
When I submit a leave request with dates and reason
Then my manager receives the request and I can track its approval status

Additional Details:
- Priority: High
- Story Points: 8
- Dependencies: HR workflow, notification module
- Notes: Support for multiple leave types and escalation if not approved in time
Title: Bulk Shift Assignment

User Story: As an operations manager I want to assign shifts to multiple employees in bulk So that scheduling is efficient and scalable

Acceptance Criteria:
Scenario: Bulk assigning shifts
Given I have selected a group of employees
When I assign a shift template to them in bulk
Then all selected employees receive the assigned shift and scheduling is updated

Additional Details:
- Priority: High
- Story Points: 8
- Dependencies: Shift template management, scheduling module
- Notes: Support undo and audit trail for bulk actions
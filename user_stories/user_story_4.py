"""Title: Employee CRUD API

User Story: As an HR manager I want to create, read, update, and delete employee records So that I can manage warehouse staff efficiently

Acceptance Criteria:
Scenario: Create employee
  Given I have valid employee data
  When I POST to /employees
  Then a new employee record is created with a unique badgeId

Scenario: Update employee
  Given an employee exists
  When I PUT/PATCH to /employees/{id}
  Then the employee record is updated

Scenario: Delete employee (soft delete)
  Given an employee exists
  When I DELETE /employees/{id}
  Then the employee is marked as inactive but not removed from the database

Scenario: List employees with pagination
  Given multiple employees exist
  When I GET /employees?page=1&size=10
  Then I see a paginated list of employees

Additional Details:
- Priority: High
- Story Points: 5
- Dependencies: E01
- Notes: Enforce unique badgeId, support filtering.
"""
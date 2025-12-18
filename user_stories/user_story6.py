user_story = """
Title: Implement Employee CRUD APIs
User Story: As an HR user I want to create, read, update, and delete employee records via REST APIs so that employee information can be managed efficiently
Acceptance Criteria:
Scenario: Create Employee
  Given I have valid employee data
  When I POST to /employees
  Then a new employee is created with a unique badgeId
Scenario: Update Employee
  Given an employee exists
  When I PATCH /employees/{id} with updates
  Then the employee record is updated
Scenario: Delete Employee
  Given an employee exists
  When I DELETE /employees/{id}
  Then the employee is soft-deleted
Scenario: List Employees
  Given employees exist
  When I GET /employees with filters
  Then the API returns paginated and filtered results
Additional Details:
- Priority: High
- Story Points: 5
- Dependencies: user_story5.txt
- Notes: Enforce unique badgeId, support soft-delete
"""
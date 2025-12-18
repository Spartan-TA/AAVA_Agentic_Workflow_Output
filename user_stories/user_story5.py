user_story = """
Title: Create Employee Domain Model
User Story: As a backend developer I want to define the Employee domain model with fields for name, badgeId, role, department, shiftGroup, hireDate, and status so that employee data is structured and consistent
Acceptance Criteria:
Scenario: Domain Model Definition
  Given the project is set up
  When I inspect the Employee entity
  Then all required fields are present and mapped to the database
Additional Details:
- Priority: High
- Story Points: 2
- Dependencies: user_story1.txt
- Notes: Use JPA annotations and validation
"""
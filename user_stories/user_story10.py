user_story = """
Title: Enforce Row-Level Security on Employee Data
User Story: As a supervisor I want to access only the employee records for my team so that privacy is maintained
Acceptance Criteria:
Scenario: Supervisor Data Access
  Given I am authenticated as SUPERVISOR
  When I access employee data
  Then I only see records for my team
Additional Details:
- Priority: High
- Story Points: 3
- Dependencies: user_story8.txt
- Notes: Implement row-level constraints
"""
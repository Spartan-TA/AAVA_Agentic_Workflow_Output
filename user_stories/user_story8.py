user_story = """
Title: Implement Role-Based Access Control (RBAC)
User Story: As a system administrator I want to restrict API access by user role (ADMIN, HR, SUPERVISOR, WORKER) so that sensitive operations are only performed by authorized users
Acceptance Criteria:
Scenario: Unauthorized Access
  Given I am not authenticated
  When I access a protected endpoint
  Then I receive a 401 Unauthorized response
Scenario: Forbidden Action
  Given I am authenticated as a WORKER
  When I attempt an ADMIN-only operation
  Then I receive a 403 Forbidden response
Scenario: Admin Access
  Given I am authenticated as ADMIN
  When I access any employee record
  Then access is granted
Scenario: Supervisor Access
  Given I am authenticated as SUPERVISOR
  When I access team records
  Then access is granted
Additional Details:
- Priority: High
- Story Points: 5
- Dependencies: user_story6.txt
- Notes: Cover all security rules with tests
"""
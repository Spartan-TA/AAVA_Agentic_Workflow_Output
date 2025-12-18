user_story = """
Title: Enable Spring Boot Actuator Health Endpoint
User Story: As a DevOps engineer I want to enable the Actuator health endpoint so that I can monitor the application's health status
Acceptance Criteria:
Scenario: Health Endpoint Availability
  Given the application is running
  When I access /actuator/health
  Then the endpoint returns status UP
Scenario: Health Endpoint Security
  Given the application is running
  When an unauthorized user accesses /actuator/health
  Then sensitive details are not exposed
Additional Details:
- Priority: High
- Story Points: 1
- Dependencies: user_story1.txt
- Notes: Ensure endpoint is documented and secured
"""
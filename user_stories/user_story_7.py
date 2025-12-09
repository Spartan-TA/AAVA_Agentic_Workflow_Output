Title: API Key and OAuth2 Security Toggle

User Story: As a developer I want to toggle between API key and OAuth2 authentication So that I can secure endpoints according to integration requirements

Acceptance Criteria:
Scenario: Switching authentication methods
Given I am configuring API security
When I select API key or OAuth2 as the authentication method
Then endpoints enforce the selected method and documentation reflects the change

Additional Details:
- Priority: Medium
- Story Points: 5
- Dependencies: Security module, OpenAPI documentation
- Notes: Ensure endpoints are protected and documentation is updated automatically
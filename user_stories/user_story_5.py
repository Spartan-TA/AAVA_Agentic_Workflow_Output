Title: Employee Filtering and OpenAPI Documentation

User Story: As an HR manager I want to filter employees by department, role, and status via the API So that I can efficiently retrieve relevant employee data and leverage documented endpoints for integration

Acceptance Criteria:
Scenario: Filtering employees via API
Given I have access to the employee API documentation
When I send a request with department, role, and status filters
Then I receive a list of employees matching the criteria and the endpoint is documented in OpenAPI

Additional Details:
- Priority: High
- Story Points: 5
- Dependencies: Employee database, OpenAPI documentation module
- Notes: Ensure filtering supports multiple values and is reflected in the OpenAPI spec
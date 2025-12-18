user_story = """
Title: Provide OpenAPI Schemas for Employee APIs
User Story: As a developer I want OpenAPI documentation for all Employee APIs so that integration is straightforward and examples are available
Acceptance Criteria:
Scenario: OpenAPI Generation
  Given the application is running
  When I access the OpenAPI docs
  Then all Employee endpoints are documented with request/response examples
Additional Details:
- Priority: High
- Story Points: 1
- Dependencies: user_story6.txt
- Notes: Use Swagger annotations
"""
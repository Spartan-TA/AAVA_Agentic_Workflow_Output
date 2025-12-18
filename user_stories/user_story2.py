user_story = """
Title: Configure Database Migration Tool
User Story: As a developer I want to add Flyway or Liquibase for database migrations so that schema changes are versioned and repeatable across environments
Acceptance Criteria:
Scenario: Migration Tool Integration
  Given the project is initialized
  When I run the application
  Then Flyway or Liquibase applies the baseline migration to the database
Scenario: Migration Verification
  Given a new migration is added
  When I deploy to a new environment
  Then the migration runs automatically and updates the schema
Additional Details:
- Priority: High
- Story Points: 2
- Dependencies: user_story1.txt
- Notes: Document migration process in README
"""
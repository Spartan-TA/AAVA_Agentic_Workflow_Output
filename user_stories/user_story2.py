# Title: Add Flyway/Liquibase for DB Migrations
# User Story: As a developer, I want Flyway or Liquibase configured so that database schema changes are versioned and repeatable.
# Acceptance Criteria:
# Scenario: Baseline migration runs
# Given the project is built
# When I run the application
# Then baseline migration scripts are applied and DB is up-to-date
# Additional Details:
# Priority: High
# Story Points: 2
# Dependencies: Project structure
# Notes: Migration scripts are in /db/migration; failures are logged
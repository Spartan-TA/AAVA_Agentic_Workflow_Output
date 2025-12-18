user_story = """
Title: Initialize Spring Boot Project Structure

User Story: As a developer I want to initialize a Spring Boot (Maven) project with a standardized base package structure so that all modules follow a consistent architecture and onboarding is accelerated

Acceptance Criteria:
Scenario: Project Initialization
  Given I have access to the repository
  When I run the build command
  Then the project builds successfully and runs on port 8080

Scenario: Base Package Structure
  Given the project is initialized
  When I inspect the source code
  Then the base packages for employee, scheduling, attendance, and safety modules are present

Additional Details:
- Priority: High
- Story Points: 3
- Dependencies: None
- Notes: Follow company Java package naming conventions; ensure README documents build/run steps
"""
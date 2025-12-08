"""
Warehouse Employee Management System - User Stories

This file contains 33 user stories covering 20 epics for the warehouse employee management system.
Each user story includes:
- Title
- User Story narrative (As a [role] I want [action] so that [benefit])
- Acceptance Criteria (Gherkin format: Given/When/Then)
- Additional Details (Priority, Story Points, Dependencies, Notes)
"""

user_stories = [
    {
        "Title": "Initialize Spring Boot Project Scaffolding",
        "User Story": "As a developer I want to set up the Spring Boot (Maven) project with standardized base packages and core modules So that all teams can accelerate delivery and maintain consistency across the warehouse employee management system",
        "Acceptance Criteria": """
Scenario: Project scaffolding and domain setup
    Given I have cloned the repository
    When I run the build and start the application
    Then the project should build and run on port 8080, the README should contain build/run steps, the Actuator health endpoint should return UP, the base package structure should be created, and Flyway/Liquibase should run baseline migration
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 5,
            "Dependencies": "None",
            "Notes": "Ensure all core modules (employee, scheduling, attendance, safety) are included in the initial scaffolding"
        }
    },
    {
        "Title": "Configure Flyway/Liquibase for Database Migrations",
        "User Story": "As a developer I want to set up Flyway/Liquibase for database migrations So that schema changes are versioned and applied consistently across environments",
        "Acceptance Criteria": """
Scenario: Database migration setup
    Given the project scaffolding is complete
    When I run the application
    Then Flyway/Liquibase should execute baseline migration and subsequent migrations should be tracked and applied automatically
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 3,
            "Dependencies": "Project scaffolding",
            "Notes": "Ensure migration scripts are included in source control and documented in README"
        }
    },
    {
        "Title": "Document Build and Run Steps in README",
        "User Story": "As a developer I want clear build and run instructions in the README So that new team members can quickly set up and run the project without confusion",
        "Acceptance Criteria": """
Scenario: README documentation
    Given the project scaffolding is complete
    When I open the README file
    Then I should see step-by-step instructions for building and running the application, including prerequisites and troubleshooting tips
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 2,
            "Dependencies": "Project scaffolding",
            "Notes": "Update README with every major change to build/run process"
        }
    },
    {
        "Title": "Enable Actuator Health Endpoint",
        "User Story": "As a DevOps engineer I want the Actuator health endpoint enabled and returning UP So that I can monitor application health and automate deployment checks",
        "Acceptance Criteria": """
Scenario: Actuator health endpoint
    Given the application is running
    When I access the /actuator/health endpoint
    Then the response should indicate UP and reflect the status of core dependencies
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 2,
            "Dependencies": "Project scaffolding",
            "Notes": "Ensure health endpoint is secured and includes DB status"
        }
    },
    {
        "Title": "Create Employee Domain and CRUD APIs",
        "User Story": "As an HR manager I want to manage employee records with create, read, update, and delete APIs So that I have a single source of truth for warehouse employee data",
        "Acceptance Criteria": """
Scenario: Employee CRUD operations
    Given I have access to the employee management module
    When I perform POST, GET, PUT, PATCH, or DELETE on /employees
    Then the system should process the request, enforce unique badgeId, support soft-delete, and return paginated/filterable results
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 8,
            "Dependencies": "Project scaffolding",
            "Notes": "Ensure OpenAPI schemas with examples are available"
        }
    },
    {
        "Title": "Enforce Unique badgeId for Employees",
        "User Story": "As an HR manager I want the system to enforce unique badgeId for each employee So that duplicate records are prevented and badge-based identification is reliable",
        "Acceptance Criteria": """
Scenario: Unique badgeId enforcement
    Given I attempt to create or update an employee record
    When I enter a badgeId that already exists
    Then the system should reject the request and display an error message
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 2,
            "Dependencies": "Employee CRUD APIs",
            "Notes": "Validation should occur on both create and update operations"
        }
    },
    {
        "Title": "Support Soft-Delete for Employee Records",
        "User Story": "As an HR manager I want to soft-delete employee records So that historical data is preserved and can be restored if needed",
        "Acceptance Criteria": """
Scenario: Soft-delete employee
    Given I have access to the employee management module
    When I delete an employee record
    Then the record should be marked as inactive and excluded from active queries, but remain in the database for audit purposes
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 3,
            "Dependencies": "Employee CRUD APIs",
            "Notes": "Provide option to restore soft-deleted records"
        }
    },
    {
        "Title": "Employee List Pagination and Filtering",
        "User Story": "As an HR user I want to view employee records with pagination and filtering So that I can efficiently search and manage large employee datasets",
        "Acceptance Criteria": """
Scenario: Paginated employee list
    Given there are more than 50 employees in the system
    When I access the employee list endpoint with pagination parameters
    Then I should receive a paginated response with correct total count and navigation links

Scenario: Filter employees by department
    Given employees are assigned to different departments
    When I apply a department filter on the employee list
    Then only employees from the selected department should be returned
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 3,
            "Dependencies": "Employee CRUD API",
            "Notes": "Support for multiple filter fields (role, status, department, shiftGroup); pagination parameters (page, size) must be documented in OpenAPI."
        }
    },
    {
        "Title": "Employee API OpenAPI Documentation",
        "User Story": "As a developer I want the Employee CRUD API to be documented in OpenAPI with examples So that integration and testing are straightforward",
        "Acceptance Criteria": """
Scenario: OpenAPI schema generation
    Given the Employee CRUD endpoints are implemented
    When I view the OpenAPI documentation
    Then all endpoints, request/response schemas, and example payloads are present and accurate
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 2,
            "Dependencies": "Employee CRUD API",
            "Notes": "Ensure badgeId uniqueness and soft-delete are reflected in documentation."
        }
    },
    {
        "Title": "Role-Based Access Control for Employee Data",
        "User Story": "As an ADMIN I want to manage all employee records So that sensitive operations are restricted to authorized roles",
        "Acceptance Criteria": """
Scenario: ADMIN access
    Given I am authenticated as ADMIN
    When I perform any CRUD operation on employee records
    Then the action should succeed

Scenario: SUPERVISOR access
    Given I am authenticated as SUPERVISOR
    When I try to access employee records outside my team
    Then I should receive a 403 Forbidden error
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 5,
            "Dependencies": "Employee CRUD, Security module",
            "Notes": "Method/endpoint security and row-level constraints enforced."
        }
    },
    {
        "Title": "API Key/OAuth2 Toggle for Security",
        "User Story": "As a system administrator I want to toggle between API key and OAuth2 authentication So that I can adapt security to integration needs",
        "Acceptance Criteria": """
Scenario: Toggle authentication method
    Given the system configuration allows authentication method selection
    When I set the method to API key or OAuth2
    Then all endpoints enforce the selected method
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 3,
            "Dependencies": "Security module",
            "Notes": "Configuration-driven; covered by automated tests."
        }
    },
    {
        "Title": "Unauthorized and Forbidden Request Handling",
        "User Story": "As a user I want clear error responses for unauthorized and forbidden actions So that I understand access limitations",
        "Acceptance Criteria": """
Scenario: Unauthorized request
    Given I am not authenticated
    When I access a protected endpoint
    Then I receive a 401 Unauthorized response

Scenario: Forbidden action
    Given I am authenticated but lack required role
    When I perform a restricted action
    Then I receive a 403 Forbidden response
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 2,
            "Dependencies": "Security module",
            "Notes": "Standardized error messages; test coverage required."
        }
    },
    {
        "Title": "Clock In/Out with Geofence Validation",
        "User Story": "As a warehouse worker I want to clock in and out with geofence validation So that my attendance is accurately recorded for payroll",
        "Acceptance Criteria": """
Scenario: Valid clock-in
    Given I am within the geofence area
    When I clock in using my device
    Then my attendance event is recorded with location and device info

Scenario: Invalid clock-in
    Given I am outside the geofence area
    When I attempt to clock in
    Then the system rejects the event with an error
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 5,
            "Dependencies": "Employee, Security, Attendance modules",
            "Notes": "Device capture required; geofence optional per site."
        }
    },
    {
        "Title": "Missed Punch Correction Workflow",
        "User Story": "As a worker I want to request corrections for missed punches So that my attendance records are accurate",
        "Acceptance Criteria": """
Scenario: Correction request
    Given I missed a clock-in or clock-out
    When I submit a correction request
    Then an approval task is created for my supervisor

Scenario: Supervisor approval
    Given a correction request is pending
    When my supervisor reviews and approves it
    Then my attendance record is updated
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 3,
            "Dependencies": "Attendance, Workflow modules",
            "Notes": "Correction history must be auditable."
        }
    },
    {
        "Title": "Attendance Reporting and Export",
        "User Story": "As an HR user I want to export attendance reports in CSV format So that I can process payroll and compliance reports",
        "Acceptance Criteria": """
Scenario: Attendance export
    Given attendance data exists for a period
    When I request a CSV export
    Then I receive a file with clock-in/out events, totals, and corrections
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 2,
            "Dependencies": "Attendance module",
            "Notes": "Export must include daily totals and correction status."
        }
    },
    {
        "Title": "Shift Template Creation and Assignment",
        "User Story": "As a supervisor I want to create recurring shift templates and assign them to employees So that I can efficiently manage warehouse staffing",
        "Acceptance Criteria": """
Scenario: Create shift template
    Given I am a supervisor
    When I define a new shift template
    Then it is saved and available for assignment

Scenario: Assign shift template
    Given a shift template exists
    When I assign it to employees
    Then their schedules are updated accordingly
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 3,
            "Dependencies": "Employee, Scheduling modules",
            "Notes": "Support for bulk assignment and audit entries."
        }
    },
    {
        "Title": "Overtime and Conflict Detection",
        "User Story": "As a supervisor I want the system to detect scheduling conflicts and overtime So that I can prevent staffing issues",
        "Acceptance Criteria": """
Scenario: Conflict detection
    Given overlapping shifts are assigned
    When I save the schedule
    Then the system alerts me to conflicts and prevents saving

Scenario: Overtime calculation
    Given an employee is scheduled beyond standard hours
    When I view the schedule
    Then overtime hours are calculated and flagged
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 4,
            "Dependencies": "Scheduling module",
            "Notes": "Blackout dates and warehouse calendars supported."
        }
    },
    {
        "Title": "Leave Request and Approval Workflow",
        "User Story": "As an employee I want to request PTO, sick, or unpaid leave So that my time off is tracked and approved",
        "Acceptance Criteria": """
Scenario: Submit leave request
    Given I have available leave balance
    When I submit a leave request
    Then my supervisor receives an approval task

Scenario: Supervisor approval
    Given a leave request is pending
    When my supervisor approves or denies it
    Then my leave balance and schedule are updated
""",
        "Additional Details": {
            "Priority": "Medium",
            "Story Points": 3,
            "Dependencies": "Employee, Scheduling, Leave modules",
            "Notes": "Integration hooks for scheduling and payroll exclusion."
        }
    },
    {
        "Title": "Leave Accrual and Balance Update",
        "User Story": "As an HR user I want leave balances to update automatically based on accrual policies So that employee records remain accurate",
        "Acceptance Criteria": """
Scenario: Accrual update
    Given accrual policies are defined
    When a pay period ends
    Then leave balances are updated for all eligible employees
""",
        "Additional Details": {
            "Priority": "Medium",
            "Story Points": 2,
            "Dependencies": "Leave module",
            "Notes": "Scheduled job; export includes approved leaves."
        }
    },
    {
        "Title": "Certification Tracking and Expiry Alerts",
        "User Story": "As a supervisor I want to track employee certifications and receive expiry alerts So that only qualified staff are assigned to sensitive roles",
        "Acceptance Criteria": """
Scenario: Certification expiry alert
    Given a certification is expiring in 30 or 7 days
    When I view the dashboard
    Then I see an alert for affected employees

Scenario: Block assignment for expired certs
    Given an employee's certification is expired
    When I try to assign them to a restricted task
    Then the system blocks the assignment
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 3,
            "Dependencies": "Employee, Certification, Scheduling modules",
            "Notes": "Certification status visible on profile; proof documents uploadable."
        }
    },
    {
        "Title": "Safety Incident Recording and Workflow",
        "User Story": "As a safety manager I want to record incidents and manage investigation workflows So that safety and compliance are improved",
        "Acceptance Criteria": """
Scenario: Incident recording
    Given an incident occurs
    When I submit a new incident report
    Then the system validates required fields and saves the record

Scenario: Investigation workflow
    Given an incident is recorded
    When I change its status to Investigating or Resolved
    Then the workflow updates and audit logs are generated
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 4,
            "Dependencies": "Employee, Safety modules",
            "Notes": "OSHA 300/300A export supported; metrics dashboard available."
        }
    },
    {
        "Title": "Equipment Checkout and Certification Validation",
        "User Story": "As a worker I want to check out equipment only if I have valid certifications So that asset use is safe and compliant",
        "Acceptance Criteria": """
Scenario: Valid certification
    Given I have a valid certification for the equipment
    When I check out the asset
    Then the system records the checkout and allows use

Scenario: Invalid certification
    Given my certification is expired or missing
    When I attempt to check out the asset
    Then the system blocks the action and notifies me
""",
        "Additional Details": {
            "Priority": "Medium",
            "Story Points": 3,
            "Dependencies": "Employee, Certification, Asset modules",
            "Notes": "History log per asset and employee; overdue returns reportable."
        }
    },
    {
        "Title": "Performance Review Cycle Creation",
        "User Story": "As an HR user I want to create quarterly and annual review cycles So that structured feedback and goal tracking are enabled",
        "Acceptance Criteria": """
Scenario: Create review cycle
    Given I am an HR user
    When I define a new review cycle
    Then it is available for assignment to employees

Scenario: Submit and acknowledge review
    Given a review is assigned
    When supervisor and employee submit and acknowledge
    Then the review is locked and history is immutable
""",
        "Additional Details": {
            "Priority": "Medium",
            "Story Points": 3,
            "Dependencies": "Employee, Review modules",
            "Notes": "PDF export and role-based visibility required."
        }
    },
    {
        "Title": "Payroll Export and Delivery",
        "User Story": "As a payroll administrator I want to export payroll-ready files and deliver them securely So that payroll runs are accurate and efficient",
        "Acceptance Criteria": """
Scenario: Export payroll file
    Given approved attendance and leave data
    When I generate a payroll export
    Then the file matches provider schema and is delivered via SFTP/API

Scenario: Failed delivery retry
    Given a delivery fails
    When the system retries with backoff
    Then audit logs are updated for each attempt
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 4,
            "Dependencies": "Attendance, Leave, Payroll modules",
            "Notes": "Totals must reconcile with reports."
        }
    },
    {
        "Title": "Notifications for Shift Changes and Expiring Certifications",
        "User Story": "As a worker I want to receive notifications for shift changes and expiring certifications So that I stay informed and compliant",
        "Acceptance Criteria": """
Scenario: Notification delivery
    Given a shift change or certification expiry
    When the event occurs
    Then I receive an in-app and email/SMS notification if opted-in

Scenario: Opt-in/out and quiet hours
    Given notification settings
    When I configure my preferences
    Then notifications respect my opt-in/out and quiet hours
""",
        "Additional Details": {
            "Priority": "Medium",
            "Story Points": 3,
            "Dependencies": "Scheduling, Certification, Notification modules",
            "Notes": "Templates localized; delivery status tracked."
        }
    },
    {
        "Title": "HRIS/WMS API Integration",
        "User Story": "As a system integrator I want REST APIs and connectors for HRIS and WMS So that master data is synchronized and duplicate entry is reduced",
        "Acceptance Criteria": """
Scenario: HRIS sync job
    Given new hires or terminations in HRIS
    When the sync job runs
    Then employee records are created or updated in the system

Scenario: JWT/OAuth2-secured APIs
    Given integration endpoints
    When accessed with valid tokens
    Then data is securely exchanged
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 5,
            "Dependencies": "Employee, Integration modules",
            "Notes": "Webhooks for events; OpenAPI documentation required."
        }
    },
    {
        "Title": "Audit Trail for Sensitive Changes",
        "User Story": "As a compliance officer I want immutable audit logs for sensitive changes So that forensic analysis and compliance are supported",
        "Acceptance Criteria": """
Scenario: Audit log entry
    Given a create/update/delete on sensitive data
    When the action occurs
    Then an audit log is generated with actor, timestamp, before/after values

Scenario: Export audit logs
    Given audit logs exist
    When I request an export by date/user/entity
    Then I receive a tamper-evident file
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 3,
            "Dependencies": "Security, Audit modules",
            "Notes": "Automated test coverage required."
        }
    },
    {
        "Title": "Attendance and Safety Reporting",
        "User Story": "As a supervisor I want operational reports on attendance, overtime, leave, certifications, and safety So that I can make data-driven staffing decisions",
        "Acceptance Criteria": """
Scenario: Generate report
    Given data exists for the selected period
    When I request a report
    Then I receive a CSV/PDF export within 10 seconds for up to 50,000 rows

Scenario: Role-based dashboard access
    Given my role
    When I access the dashboard
    Then I see only permitted reports and metrics
""",
        "Additional Details": {
            "Priority": "Medium",
            "Story Points": 4,
            "Dependencies": "Attendance, Leave, Certification, Safety, Audit modules",
            "Notes": "Metrics endpoints available for BI."
        }
    },
    {
        "Title": "Mobile Access via PWA",
        "User Story": "As a warehouse worker I want to access core workflows on mobile devices with offline support So that I can clock in/out and view schedules anywhere",
        "Acceptance Criteria": """
Scenario: Mobile usability
    Given I access the app on a mobile device
    When I use core flows (clock-in/out, view schedule, request leave)
    Then the experience is responsive and installable as a PWA

Scenario: Offline clock event queue
    Given I am offline
    When I clock in/out
    Then the event is queued and synced when online, with conflict resolution
""",
        "Additional Details": {
            "Priority": "Medium",
            "Story Points": 5,
            "Dependencies": "Attendance, Scheduling, Leave, Notification modules",
            "Notes": "Lighthouse PWA score â¥ 80."
        }
    },
    {
        "Title": "Onboarding and Offboarding Automation",
        "User Story": "As an HR user I want onboarding and offboarding workflows to automate provisioning and deprovisioning So that employee lifecycle changes are efficient and low-risk",
        "Acceptance Criteria": """
Scenario: Onboarding automation
    Given a new hire appears from HRIS
    When onboarding tasks are generated
    Then accounts, initial schedule, and required training are provisioned

Scenario: Offboarding automation
    Given an employee is terminated
    When offboarding tasks run
    Then access is revoked, assets collected, and schedules updated
""",
        "Additional Details": {
            "Priority": "Medium",
            "Story Points": 4,
            "Dependencies": "Employee, Scheduling, Certification, Asset, Integration modules",
            "Notes": "Task tracking and notifications included."
        }
    },
    {
        "Title": "Localization and Multi-Tenant Support",
        "User Story": "As a global admin I want locale-specific formatting and tenant isolation So that the system supports multiple warehouses and languages",
        "Acceptance Criteria": """
Scenario: Locale formatting
    Given a tenant is configured for a locale
    When I view dates, times, and currency
    Then they are formatted per locale settings

Scenario: Tenant isolation
    Given multiple tenants exist
    When I access data
    Then I only see data for my tenant
""",
        "Additional Details": {
            "Priority": "Medium",
            "Story Points": 4,
            "Dependencies": "Employee, Scheduling, Localization modules",
            "Notes": "Language packs and tenant isolation enforced."
        }
    },
    {
        "Title": "Observability and Monitoring Integration",
        "User Story": "As a DevOps engineer I want structured logging and metrics via OpenTelemetry, Prometheus, and Grafana So that system health and performance are monitored",
        "Acceptance Criteria": """
Scenario: Structured logging
    Given application events occur
    When logs are generated
    Then they follow structured format and are ingested by monitoring tools

Scenario: Metrics dashboard
    Given Prometheus and Grafana are configured
    When I view dashboards
    Then I see real-time metrics for key system components
""",
        "Additional Details": {
            "Priority": "Medium",
            "Story Points": 4,
            "Dependencies": "All modules",
            "Notes": "Alerts and dashboards for error rates, latency, and resource usage."
        }
    },
    {
        "Title": "Deployment Automation with CI/CD",
        "User Story": "As a release manager I want automated deployment using Docker, Kubernetes, and blue-green strategies So that releases are reliable and rollback is possible",
        "Acceptance Criteria": """
Scenario: CI/CD pipeline
    Given code changes are committed
    When the pipeline runs
    Then Docker images are built, tested, and deployed to Kubernetes

Scenario: Blue-green deployment
    Given a new release is ready
    When I trigger deployment
    Then traffic is switched to the new version with rollback available
""",
        "Additional Details": {
            "Priority": "High",
            "Story Points": 5,
            "Dependencies": "All modules",
            "Notes": "Rollback and health checks required."
        }
    }
]

def display_all_user_stories():
    for idx, story in enumerate(user_stories, 1):
        print(f"
{'='*80}")
        print(f"USER STORY #{idx}")
        print(f"{'='*80}")
        print(f"Title: {story['Title']}")
        print(f"
User Story: {story['User Story']}")
        print(f"
Acceptance Criteria:{story['Acceptance Criteria']}")
        print(f"
Additional Details:")
        for key, value in story['Additional Details'].items():
            print(f"  - {key}: {value}")

def get_user_story(story_number):
    if 1 <= story_number <= len(user_stories):
        return user_stories[story_number - 1]
    else:
        return None

def search_user_stories(keyword):
    results = []
    for story in user_stories:
        if (keyword.lower() in story['Title'].lower() or 
            keyword.lower() in story['User Story'].lower()):
            results.append(story)
    return results

if __name__ == "__main__":
    print("Warehouse Employee Management System - User Stories")
    print(f"Total User Stories: {len(user_stories)}")
    display_all_user_stories()
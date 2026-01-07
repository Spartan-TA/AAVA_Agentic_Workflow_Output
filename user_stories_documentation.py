"""
Comprehensive User Stories Documentation
Generated from Epic Decomposition Process

===========================================
USER STORY 1: User Registration
===========================================

Title: User Registration

User Story: As a new user I want to register for an account so that I can access the application's features.

Acceptance Criteria:

Scenario: Successful registration with valid details
Given I am on the registration page
When I enter valid information and submit the form
Then my account is created and I receive a confirmation email

Scenario: Registration with an already used email
Given I am on the registration page
When I enter an email that is already registered
Then I see an error message indicating the email is already in use

Scenario: Registration with invalid data
Given I am on the registration page
When I submit the form with missing or invalid fields
Then I see validation errors for the incorrect fields

Additional Details:
- Priority: High
- Story Points: 8
- Dependencies: Email service, User database
- Notes: Ensure password strength validation and email format checks

===========================================
USER STORY 2: User Login
===========================================

Title: User Login

User Story: As a registered user I want to log in to my account so that I can securely access my personal dashboard.

Acceptance Criteria:

Scenario: Successful login
Given I am on the login page
When I enter valid credentials
Then I am redirected to my dashboard

Scenario: Login with incorrect password
Given I am on the login page
When I enter an incorrect password
Then I see an error message indicating invalid credentials

Scenario: Account lockout after multiple failed attempts
Given I have failed to log in 5 times
When I attempt to log in again
Then my account is locked and I am notified

Additional Details:
- Priority: High
- Story Points: 5
- Dependencies: User database, Authentication module
- Notes: Implement CAPTCHA after 3 failed attempts

===========================================
USER STORY 3: Password Reset
===========================================

Title: Password Reset

User Story: As a user I want to reset my password so that I can regain access if I forget my credentials.

Acceptance Criteria:

Scenario: Request password reset
Given I am on the login page
When I click 'Forgot Password' and enter my email
Then I receive a password reset link

Scenario: Reset with valid link
Given I have received a reset link
When I click the link and enter a new password
Then my password is updated and I can log in with the new password

Scenario: Reset with expired link
Given my reset link has expired
When I try to use it
Then I see an error and am prompted to request a new link

Additional Details:
- Priority: High
- Story Points: 5
- Dependencies: Email service, Authentication module
- Notes: Reset links expire after 1 hour

===========================================
USER STORY 4: Profile Management
===========================================

Title: Profile Management

User Story: As a user I want to update my profile information so that my account details remain current.

Acceptance Criteria:

Scenario: Update profile with valid data
Given I am logged in
When I update my profile and save changes
Then my updated information is displayed

Scenario: Update profile with invalid data
Given I am editing my profile
When I enter invalid data (e.g., invalid phone number)
Then I see validation errors for the incorrect fields

Scenario: Attempt to change email to one already in use
Given I am editing my profile
When I change my email to one that is already registered
Then I see an error message

Additional Details:
- Priority: Medium
- Story Points: 3
- Dependencies: User database
- Notes: Audit changes for security

===========================================
USER STORY 5: Role-Based Access Control
===========================================

Title: Role-Based Access Control

User Story: As an administrator I want to assign roles to users so that access to features is appropriately restricted.

Acceptance Criteria:

Scenario: Assign role to user
Given I am an admin on the user management page
When I assign a role to a user
Then the user's permissions are updated accordingly

Scenario: User attempts to access restricted feature
Given a user does not have admin rights
When they try to access the admin panel
Then they are denied access

Scenario: View user roles
Given I am an admin
When I view the user list
Then I see each user's assigned role

Additional Details:
- Priority: High
- Story Points: 8
- Dependencies: User management module
- Notes: Roles include Admin, Manager, User

===========================================
USER STORY 6: Data Visualization Dashboard
===========================================

Title: Data Visualization Dashboard

User Story: As a manager I want to view key metrics in a dashboard so that I can monitor performance at a glance.

Acceptance Criteria:

Scenario: View dashboard with data
Given I am logged in as a manager
When I navigate to the dashboard
Then I see charts and graphs with up-to-date metrics

Scenario: Filter dashboard data by date range
Given I am on the dashboard
When I select a specific date range
Then the displayed data updates accordingly

Scenario: No data available
Given there is no data for the selected period
When I view the dashboard
Then I see a message indicating no data is available

Additional Details:
- Priority: High
- Story Points: 13
- Dependencies: Reporting module, Data warehouse
- Notes: Charts include line, bar, and pie graphs

===========================================
USER STORY 7: Export Reports
===========================================

Title: Export Reports

User Story: As a user I want to export reports in PDF and CSV formats so that I can share or analyze data offline.

Acceptance Criteria:

Scenario: Export report as PDF
Given I am viewing a report
When I click 'Export as PDF'
Then a PDF file downloads with the report data

Scenario: Export report as CSV
Given I am viewing a report
When I click 'Export as CSV'
Then a CSV file downloads with the report data

Scenario: Export with filters applied
Given I have applied filters to the report
When I export the report
Then the exported file reflects the filtered data

Additional Details:
- Priority: Medium
- Story Points: 8
- Dependencies: Reporting module, File generation service
- Notes: Ensure proper formatting and encoding

===========================================
USER STORY 8: In-App Notifications
===========================================

Title: In-App Notifications

User Story: As a user I want to receive in-app notifications for important events so that I stay informed in real time.

Acceptance Criteria:

Scenario: Receive notification for new message
Given I am logged in
When I receive a new message
Then a notification appears in the app

Scenario: Mark notification as read
Given I have unread notifications
When I mark one as read
Then it is no longer highlighted as unread

Scenario: View notification history
Given I am on the notifications page
When I view my notifications
Then I see a list of all past notifications

Additional Details:
- Priority: Medium
- Story Points: 5
- Dependencies: Notification service
- Notes: Support for push notifications in future

===========================================
USER STORY 9: Email Notifications
===========================================

Title: Email Notifications

User Story: As a user I want to receive email notifications for critical actions so that I am alerted even when not logged in.

Acceptance Criteria:

Scenario: Receive email for password change
Given I change my account password
When the change is saved
Then I receive an email confirmation

Scenario: Receive email for failed login attempts
Given there are multiple failed login attempts on my account
When the threshold is reached
Then I receive an alert email

Scenario: Unsubscribe from email notifications
Given I am receiving email notifications
When I update my preferences to unsubscribe
Then I no longer receive those emails

Additional Details:
- Priority: Medium
- Story Points: 5
- Dependencies: Email service, User preferences module
- Notes: Emails must comply with CAN-SPAM

===========================================
USER STORY 10: Audit Logging
===========================================

Title: Audit Logging

User Story: As an administrator I want to view an audit log of user actions so that I can monitor system usage and security.

Acceptance Criteria:

Scenario: View audit log
Given I am an admin
When I access the audit log
Then I see a chronological list of user actions

Scenario: Filter audit log by user
Given I am viewing the audit log
When I filter by a specific user
Then only that user's actions are displayed

Scenario: Export audit log
Given I am viewing the audit log
When I click 'Export'
Then the log downloads as a CSV file

Additional Details:
- Priority: High
- Story Points: 8
- Dependencies: Logging module, User management
- Notes: Log sensitive actions (login, data export, role changes)

===========================================
USER STORY 11: Bulk User Import
===========================================

Title: Bulk User Import

User Story: As an administrator I want to import users in bulk from a CSV file so that I can quickly onboard multiple users.

Acceptance Criteria:

Scenario: Successful import with valid CSV
Given I am on the user import page
When I upload a valid CSV file
Then users are created and notified by email

Scenario: Import with invalid data
Given I upload a CSV with errors
When the import runs
Then I see a detailed error report

Scenario: Duplicate users in import
Given the CSV contains duplicate emails
When the import runs
Then duplicates are skipped and reported

Additional Details:
- Priority: Medium
- Story Points: 8
- Dependencies: User management, Email service
- Notes: Provide CSV template for admins

===========================================
USER STORY 12: Two-Factor Authentication (2FA)
===========================================

Title: Two-Factor Authentication (2FA)

User Story: As a security-conscious user I want to enable two-factor authentication so that my account is more secure.

Acceptance Criteria:

Scenario: Enable 2FA
Given I am on my security settings page
When I enable 2FA and verify my device
Then 2FA is activated for my account

Scenario: Login with 2FA enabled
Given I have 2FA enabled
When I log in with correct credentials
Then I am prompted for a verification code

Scenario: Disable 2FA
Given I have 2FA enabled
When I disable it
Then I am no longer prompted for a code at login

Additional Details:
- Priority: High
- Story Points: 8
- Dependencies: Authentication module, SMS/Email service
- Notes: Support for authenticator apps and SMS

===========================================
USER STORY 13: User Search and Filtering
===========================================

Title: User Search and Filtering

User Story: As an administrator I want to search and filter users by various criteria so that I can efficiently manage the user base.

Acceptance Criteria:

Scenario: Search by name or email
Given I am on the user management page
When I enter a name or email in the search box
Then matching users are displayed

Scenario: Filter by role
Given I am on the user management page
When I select a role filter
Then only users with that role are shown

Scenario: No results found
Given I search for a non-existent user
When I perform the search
Then I see a 'No users found' message

Additional Details:
- Priority: Medium
- Story Points: 5
- Dependencies: User management module
- Notes: Support for pagination in results

===========================================
USER STORY 14: Scheduled Reports
===========================================

Title: Scheduled Reports

User Story: As a manager I want to schedule reports to be generated and emailed automatically so that I receive regular updates without manual effort.

Acceptance Criteria:

Scenario: Schedule a report
Given I am on the reports page
When I set up a schedule and save
Then the report is generated and emailed at the specified time

Scenario: Edit scheduled report
Given I have a scheduled report
When I update the schedule
Then the changes are saved and applied

Scenario: Cancel scheduled report
Given I have a scheduled report
When I cancel it
Then the report is no longer generated or emailed

Additional Details:
- Priority: Medium
- Story Points: 8
- Dependencies: Reporting module, Email service, Scheduler
- Notes: Support daily, weekly, and monthly frequencies

===========================================
EXECUTIVE SUMMARY
===========================================

This document contains 14 comprehensive user stories derived from common software development epics.
Each story includes:
- Clear title and user narrative
- Multiple Gherkin-formatted acceptance criteria (Given-When-Then)
- Priority level (High/Medium/Low)
- Story points estimate (1-13 scale)
- Dependencies on other modules
- Additional implementation notes

Total Story Points: 97
High Priority Stories: 7
Medium Priority Stories: 7

These stories cover key functional areas including:
- Authentication and Security (Stories 1-3, 12)
- User Management (Stories 4-5, 11, 13)
- Data Visualization and Reporting (Stories 6-7, 14)
- Notifications (Stories 8-9)
- System Administration (Story 10)

All stories are ready for backlog refinement and sprint planning.
"""

# User Stories Data Structure
user_stories = [
    {
        'id': 1,
        'title': 'User Registration',
        'user_story': 'As a new user I want to register for an account so that I can access the application's features.',
        'priority': 'High',
        'story_points': 8,
        'dependencies': ['Email service', 'User database']
    },
    {
        'id': 2,
        'title': 'User Login',
        'user_story': 'As a registered user I want to log in to my account so that I can securely access my personal dashboard.',
        'priority': 'High',
        'story_points': 5,
        'dependencies': ['User database', 'Authentication module']
    },
    {
        'id': 3,
        'title': 'Password Reset',
        'user_story': 'As a user I want to reset my password so that I can regain access if I forget my credentials.',
        'priority': 'High',
        'story_points': 5,
        'dependencies': ['Email service', 'Authentication module']
    },
    {
        'id': 4,
        'title': 'Profile Management',
        'user_story': 'As a user I want to update my profile information so that my account details remain current.',
        'priority': 'Medium',
        'story_points': 3,
        'dependencies': ['User database']
    },
    {
        'id': 5,
        'title': 'Role-Based Access Control',
        'user_story': 'As an administrator I want to assign roles to users so that access to features is appropriately restricted.',
        'priority': 'High',
        'story_points': 8,
        'dependencies': ['User management module']
    },
    {
        'id': 6,
        'title': 'Data Visualization Dashboard',
        'user_story': 'As a manager I want to view key metrics in a dashboard so that I can monitor performance at a glance.',
        'priority': 'High',
        'story_points': 13,
        'dependencies': ['Reporting module', 'Data warehouse']
    },
    {
        'id': 7,
        'title': 'Export Reports',
        'user_story': 'As a user I want to export reports in PDF and CSV formats so that I can share or analyze data offline.',
        'priority': 'Medium',
        'story_points': 8,
        'dependencies': ['Reporting module', 'File generation service']
    },
    {
        'id': 8,
        'title': 'In-App Notifications',
        'user_story': 'As a user I want to receive in-app notifications for important events so that I stay informed in real time.',
        'priority': 'Medium',
        'story_points': 5,
        'dependencies': ['Notification service']
    },
    {
        'id': 9,
        'title': 'Email Notifications',
        'user_story': 'As a user I want to receive email notifications for critical actions so that I am alerted even when not logged in.',
        'priority': 'Medium',
        'story_points': 5,
        'dependencies': ['Email service', 'User preferences module']
    },
    {
        'id': 10,
        'title': 'Audit Logging',
        'user_story': 'As an administrator I want to view an audit log of user actions so that I can monitor system usage and security.',
        'priority': 'High',
        'story_points': 8,
        'dependencies': ['Logging module', 'User management']
    },
    {
        'id': 11,
        'title': 'Bulk User Import',
        'user_story': 'As an administrator I want to import users in bulk from a CSV file so that I can quickly onboard multiple users.',
        'priority': 'Medium',
        'story_points': 8,
        'dependencies': ['User management', 'Email service']
    },
    {
        'id': 12,
        'title': 'Two-Factor Authentication (2FA)',
        'user_story': 'As a security-conscious user I want to enable two-factor authentication so that my account is more secure.',
        'priority': 'High',
        'story_points': 8,
        'dependencies': ['Authentication module', 'SMS/Email service']
    },
    {
        'id': 13,
        'title': 'User Search and Filtering',
        'user_story': 'As an administrator I want to search and filter users by various criteria so that I can efficiently manage the user base.',
        'priority': 'Medium',
        'story_points': 5,
        'dependencies': ['User management module']
    },
    {
        'id': 14,
        'title': 'Scheduled Reports',
        'user_story': 'As a manager I want to schedule reports to be generated and emailed automatically so that I receive regular updates without manual effort.',
        'priority': 'Medium',
        'story_points': 8,
        'dependencies': ['Reporting module', 'Email service', 'Scheduler']
    }
]

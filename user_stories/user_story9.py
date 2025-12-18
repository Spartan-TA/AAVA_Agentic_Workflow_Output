user_story = """
Title: Add API Key/OAuth2 Toggle for Security
User Story: As a DevOps engineer I want to toggle between API key and OAuth2 authentication via configuration so that security can be adapted to different environments
Acceptance Criteria:
Scenario: API Key Mode
  Given the config is set to API key
  When I authenticate with a valid key
  Then access is granted
Scenario: OAuth2 Mode
  Given the config is set to OAuth2
  When I authenticate with a valid token
  Then access is granted
Additional Details:
- Priority: High
- Story Points: 3
- Dependencies: user_story8.txt
- Notes: Document configuration options
"""
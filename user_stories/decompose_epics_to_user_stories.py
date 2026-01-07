# Python script to decompose epics into user stories
import csv
import os

def decompose_epic_to_user_stories(epic, index_start=1):
    # This is a placeholder for actual decomposition logic
    # In a real scenario, this would use NLP or templates
    stories = []
    # Example decomposition for demonstration
    # You would replace this with logic based on epic content
    stories.append({
        'Title': f"{epic['Title']} - Core Functionality",
        'User Story': f"As a {epic['User Roles']} I want {epic['Key Functionalities']} so that {epic['Measurable Outcomes']}",
        'Acceptance Criteria': f"""Scenario: {epic['Title']} basic flow
Given {epic['Description']}
When I use the {epic['Key Functionalities']}
Then I achieve {epic['Measurable Outcomes']}
""",
        'Priority': 'High',
        'Story Points': 5,
        'Dependencies': '',
        'Notes': 'Decomposed from epic.'
    })
    # Add more stories as needed per epic
    return stories

def main():
    csv_path = os.path.join('user_stories', 'epics.csv')
    with open(csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        epics = [row for row in reader]
    user_story_idx = 1
    for epic in epics:
        stories = decompose_epic_to_user_stories(epic, user_story_idx)
        for story in stories:
            file_path = os.path.join('user_stories', f'user_story{user_story_idx}.txt')
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(f"Title: {story['Title']}
")
                f.write(f"User Story: {story['User Story']}
")
                f.write(f"Acceptance Criteria:
{story['Acceptance Criteria']}
")
                f.write(f"Additional Details:
Priority: {story['Priority']}
Story Points: {story['Story Points']}
Dependencies: {story['Dependencies']}
Notes: {story['Notes']}
")
            user_story_idx += 1

if __name__ == '__main__':
    main()

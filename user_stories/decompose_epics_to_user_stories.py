import os
import json
from validate_and_extract_epics import validate_and_extract_epics

def decompose_epic_to_user_stories(epic, start_index=1):
    # Example decomposition logic (to be replaced with real logic per epic)
    user_stories = []
    # Placeholder: Decompose based on epic description keywords, etc.
    # For demonstration, create two user stories per epic
    for i in range(2):
        user_story = {
            'Title': f"{epic['Title']} - User Story {i+1}",
            'User Story': f"As a [role] I want [functionality] So that [benefit] (from epic: {epic['Description']})",
            'Acceptance Criteria': """Scenario: [Scenario Title]
  Given [context]
  When [action]
  Then [outcome]""",
            'Additional Details': {
                'Priority': 'High' if i == 0 else 'Medium',
                'Story Points': 3 + i,
                'Dependencies': '',
                'Notes': 'Auto-generated. Refine as needed.'
            }
        }
        user_stories.append(user_story)
    return user_stories

def write_user_stories_to_files(user_stories, output_dir):
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    for idx, us in enumerate(user_stories, 1):
        file_path = os.path.join(output_dir, f'user_story{idx}.txt')
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(f"Title: {us['Title']}
")
            f.write(f"User Story: {us['User Story']}
")
            f.write(f"Acceptance Criteria: {us['Acceptance Criteria']}
")
            f.write("Additional Details:
")
            for k, v in us['Additional Details'].items():
                f.write(f"  {k}: {v}
")

if __name__ == '__main__':
    csv_path = 'epics.csv'
    epics = validate_and_extract_epics(csv_path)
    all_user_stories = []
    for epic in epics:
        all_user_stories.extend(decompose_epic_to_user_stories(epic))
    write_user_stories_to_files(all_user_stories, 'user_stories')
    print(f"Generated {len(all_user_stories)} user stories.")
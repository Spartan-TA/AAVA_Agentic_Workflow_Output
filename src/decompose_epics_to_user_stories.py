import csv

USER_STORY_TEMPLATE = '''Title: {title}
User Story: As a {role} I want {functionality} So that {benefit}
Acceptance Criteria:
{acceptance_criteria}
Additional Details:
  Priority: {priority}
  Story Points: {story_points}
  Dependencies: {dependencies}
  Notes: {notes}
'''

GHERKIN_TEMPLATE = '''Given {given}
When {when}
Then {then}'''

def decompose_epic(epic):
    # Example decomposition logic (customize as needed)
    # Extract fields from epic
    title = epic.get('Epic Title', 'Untitled Epic')
    description = epic.get('Epic Description', '')
    role = epic.get('User Role', 'user')
    functionalities = epic.get('Key Functionalities', '').split(';')
    outcomes = epic.get('Measurable Outcomes', '').split(';')
    priority = epic.get('Priority', 'Medium')
    story_points = epic.get('Estimated Story Points', '5')
    dependencies = epic.get('Dependencies', 'None')
    notes = epic.get('Notes', '')

    user_stories = []
    for i, func in enumerate(functionalities):
        func = func.strip()
        benefit = outcomes[i].strip() if i < len(outcomes) else ''
        title_story = f"{title} - {func}" if func else title
        acceptance_criteria = GHERKIN_TEMPLATE.format(
            given=f"the {role} needs to {func}",
            when=f"the {role} performs {func}",
            then=f"the system enables {benefit}"
        )
        user_stories.append(USER_STORY_TEMPLATE.format(
            title=title_story,
            role=role,
            functionality=func,
            benefit=benefit,
            acceptance_criteria=acceptance_criteria,
            priority=priority,
            story_points=story_points,
            dependencies=dependencies,
            notes=notes
        ))
    return user_stories

def process_epics(csv_path):
    with open(csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        all_user_stories = []
        for epic in reader:
            user_stories = decompose_epic(epic)
            all_user_stories.extend(user_stories)
    return all_user_stories

if __name__ == '__main__':
    csv_path = 'epics.csv'  # Update with actual path
    user_stories = process_epics(csv_path)
    print('
---
'.join(user_stories))

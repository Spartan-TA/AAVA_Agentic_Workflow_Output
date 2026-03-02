import csv
import os

EPICS_CSV_PATH = 'src/epics.csv'
OUTPUT_PATH = 'src/user_stories.txt'

# Helper to create Gherkin acceptance criteria
AC_TEMPLATE = """Scenario: {scenario}
  Given {given}
  When {when}
  Then {then}
"""

def decompose_epic(epic):
    # Decompose epic into user stories (example logic, can be expanded)
    stories = []
    # Example: split by key functionalities if comma-separated
    functionalities = [f.strip() for f in epic.get('Key Functionality', '').split(',') if f.strip()]
    if not functionalities:
        functionalities = [epic.get('Key Functionality', '')]
    for func in functionalities:
        story = {}
        story['Title'] = f"{func} for {epic.get('User Role', 'User')}"
        story['User Story'] = f"As a {epic.get('User Role', 'user')} I want {func} so that {epic.get('Measurable Outcome', '').rstrip('.')}"
        # Gherkin acceptance criteria (basic template)
        story['Acceptance Criteria'] = AC_TEMPLATE.format(
            scenario=f"{func} works as expected",
            given=f"I am a {epic.get('User Role', 'user')} and {epic.get('Epic Description', '').lower()}",
            when=f"I {func.lower()}",
            then=f"I achieve {epic.get('Measurable Outcome', '').rstrip('.')}"
        )
        # Additional details (defaults for demonstration)
        story['Priority'] = 'High' if 'critical' in epic.get('Epic Description', '').lower() else 'Medium'
        story['Story Points'] = 5
        story['Dependencies'] = ''
        story['Notes'] = ''
        # Error handling for missing fields
        missing = [k for k in ['Epic Title', 'Epic Description', 'User Role', 'Key Functionality', 'Measurable Outcome'] if not epic.get(k)]
        if missing:
            story['Notes'] += f"Missing fields: {', '.join(missing)}. "
        if 'error' in epic:
            story['Notes'] += epic['error']
        stories.append(story)
    return stories

def main():
    if not os.path.exists(EPICS_CSV_PATH):
        print(f"CSV file not found: {EPICS_CSV_PATH}")
        return
    with open(EPICS_CSV_PATH, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        all_stories = []
        for row in reader:
            stories = decompose_epic(row)
            all_stories.extend(stories)
    # Write output in required format
    with open(OUTPUT_PATH, 'w', encoding='utf-8') as f:
        for story in all_stories:
            f.write(f"Title: {story['Title']}
")
            f.write(f"User Story: {story['User Story']}
")
            f.write(f"Acceptance Criteria:
{story['Acceptance Criteria']}
")
            f.write(f"Additional Details: Priority: {story['Priority']}, Story Points: {story['Story Points']}, Dependencies: {story['Dependencies']}, Notes: {story['Notes']}
")
            f.write("---
")
    print(f"User stories written to {OUTPUT_PATH}")

if __name__ == '__main__':
    main()

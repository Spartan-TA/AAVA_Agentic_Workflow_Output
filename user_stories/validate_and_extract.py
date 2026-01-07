# Python script to validate CSV structure and extract epic data
import csv
import os

def validate_and_extract_epics(csv_path):
    required_fields = ['Title', 'Description', 'User Roles', 'Key Functionalities', 'Measurable Outcomes']
    epics = []
    with open(csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        for field in required_fields:
            if field not in reader.fieldnames:
                raise ValueError(f'Missing required field: {field}')
        for row in reader:
            epic = {field: row[field] for field in required_fields}
            epics.append(epic)
    return epics

if __name__ == '__main__':
    csv_path = os.path.join('user_stories', 'epics.csv')
    epics = validate_and_extract_epics(csv_path)
    for epic in epics:
        print(epic)

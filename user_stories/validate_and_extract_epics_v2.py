import csv

def validate_and_extract_epics(csv_path):
    epics = []
    with open(csv_path, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            epics.append(row)
    # Validate required fields
    required_fields = ['Title', 'Description', 'Target User Role', 'Measurable Outcomes']
    for epic in epics:
        for field in required_fields:
            if field not in epic or not epic[field]:
                raise ValueError(f"Missing required field: {field} in epic: {epic.get('Title', 'Unknown')}")
    return epics

if __name__ == '__main__':
    epics = validate_and_extract_epics('user_stories/epics.csv')
    print(epics)

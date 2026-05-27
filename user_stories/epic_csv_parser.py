# epic_csv_parser.py
import csv
import sys

def validate_and_extract_epics(csv_file_path):
    required_fields = ['Epic Title', 'Epic Description', 'Target User Roles', 'Measurable Outcomes']
    epics = []
    errors = []
    try:
        with open(csv_file_path, newline='', encoding='utf-8') as csvfile:
            reader = csv.DictReader(csvfile)
            for idx, row in enumerate(reader, start=1):
                missing = [field for field in required_fields if not row.get(field)]
                if missing:
                    errors.append(f"Row {idx} missing fields: {', '.join(missing)}")
                    continue
                epics.append({
                    'title': row['Epic Title'].strip(),
                    'description': row['Epic Description'].strip(),
                    'roles': [role.strip() for role in row['Target User Roles'].split(';') if role.strip()],
                    'outcomes': row['Measurable Outcomes'].strip(),
                    'raw': row
                })
        if errors:
            print('Errors found in CSV:')
            for error in errors:
                print(error)
        return epics
    except Exception as e:
        print(f"Failed to read CSV: {e}")
        sys.exit(1)

# Example usage:
# epics = validate_and_extract_epics('epics.csv')
# print(epics)

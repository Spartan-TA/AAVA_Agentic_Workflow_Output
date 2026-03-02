import csv
import os

EPICS_CSV_PATH = 'src/epics.csv'

def extract_epics(csv_path):
    epics = []
    if not os.path.exists(csv_path):
        raise FileNotFoundError(f'CSV file not found: {csv_path}')
    with open(csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        required_fields = ['Epic Title', 'Epic Description', 'User Role', 'Key Functionality', 'Measurable Outcome']
        for row_num, row in enumerate(reader, start=1):
            epic = {}
            for field in required_fields:
                value = row.get(field, '').strip()
                if not value:
                    epic['error'] = f'Missing or empty field "{field}" in row {row_num}'
                epic[field] = value
            epics.append(epic)
    return epics

def main():
    try:
        epics = extract_epics(EPICS_CSV_PATH)
        for idx, epic in enumerate(epics, 1):
            print(f'Epic {idx}:')
            for k, v in epic.items():
                print(f'  {k}: {v}')
            print('---')
    except Exception as e:
        print(f'Error: {e}')

if __name__ == '__main__':
    main()

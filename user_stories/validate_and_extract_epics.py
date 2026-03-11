import csv

def validate_and_extract_epics(csv_path):
    with open(csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        required_fields = {'Title', 'Description'}
        for field in required_fields:
            if field not in reader.fieldnames:
                raise ValueError(f'Missing required field: {field}')
        epics = [row for row in reader]
    return epics

if __name__ == '__main__':
    csv_path = 'epics.csv'
    epics = validate_and_extract_epics(csv_path)
    print(epics)
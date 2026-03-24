import csv

def import_epics(csv_path):
    epics = []
    with open(csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            epics.append(row)
    return epics

if __name__ == '__main__':
    csv_path = 'epics.csv'  # Update with actual path
    epics = import_epics(csv_path)
    for epic in epics:
        print(epic)

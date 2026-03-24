import csv

def analyze_epics_csv(csv_path):
    with open(csv_path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        columns = reader.fieldnames
        sample_rows = []
        for i, row in enumerate(reader):
            if i < 3:
                sample_rows.append(row)
            else:
                break
        return columns, sample_rows

if __name__ == '__main__':
    csv_path = 'epics.csv'  # Update with actual path
    columns, samples = analyze_epics_csv(csv_path)
    print('Columns:', columns)
    print('Sample Rows:', samples)

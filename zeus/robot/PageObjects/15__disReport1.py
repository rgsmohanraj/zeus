import csv
from datetime import datetime
from datetime import datetime, timedelta

file_path = 'D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads//130723//Disbursement_Report_130723.csv'
external_id = '${exId}'


def search_total_outstanding_amount(file_path, external_id):
    with open(file_path, 'r') as csv_file:
        read = csv.reader(csv_file)
        next(read)
        loop_failed = False  # Variable to track loop status

        for line in read:
            if external_id in line:
                print(line)
                totOut = line[12]
                return totOut
                print(totOut)
                loanId = line[3]
                return loanId
                print(loanId)


def search_loanId(file_path, external_id):
    with open(file_path, 'r') as csv_file:
        read = csv.reader(csv_file)
        next(read)
        loop_failed = False  # Variable to track loop status

        for line in read:
            if external_id in line:
                print(line)
                loanId = line[5]
                return loanId
                print(loanId)


def search_XIRR(file_path, external_id):
    with open(file_path, 'r') as csv_file:
        read = csv.reader(csv_file)
        next(read)
        loop_failed = False  # Variable to track loop status

        for line in read:
            if external_id in line:
                print(line)
                XIRR = line[13]
                return XIRR
                print(XIRR)


search_total_outstanding_amount(file_path, external_id)
search_loanId(file_path, external_id)
search_XIRR(file_path, external_id)

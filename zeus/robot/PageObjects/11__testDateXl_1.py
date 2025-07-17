import openpyxl
from datetime import datetime
from datetime import date
from faker import Faker
from datetime import datetime, timedelta
import calendar
import os
import glob

download_directory = "D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads"
search_pattern = os.path.join(download_directory, "*.xlsx")
files = glob.glob(search_pattern)
# get the most recent file using file creation time
most_recent_file = max(files, key=os.path.getctime)
excelPath = most_recent_file
print(excelPath)
workbook = openpyxl.load_workbook(excelPath)

K = "K"
W = "W"
X = "X"
AF = "AF"
AG = "AG"
AJ = "AJ"
AK = "AK"


def dateCreationXl():
    for i in range(2, 1600):
        dob = '17-11-1995'
        submittedOn = '02-06-2019'

        loanSubmittedOn = datetime.strptime('01-12-2022', '%d-%m-%Y').date()
        dueDay = 1

        for _ in range(i-2):
            month_days = calendar.monthrange(
                loanSubmittedOn.year, loanSubmittedOn.month)[1]
            if dueDay + 1 > month_days:
                loanSubmittedOn += timedelta(days=1)
                dueDay = 1
            else:
                dueDay += 1

        # Take loan submitted on date and increase 31 days for 1st repayment on
        date_format = '%d-%m-%Y'
        next_month = loanSubmittedOn + timedelta(days=31)
        firstRepayOn = next_month.strftime(date_format)

        # Convert the strings to datetime objects
        dob = datetime.strptime(dob, date_format).date()
        submittedOn = datetime.strptime(submittedOn, date_format).date()
        firstRepayOn = datetime.strptime(firstRepayOn, date_format).date()

        worksheet = workbook['ClientLoan']

        # concatenate  excel column into loop iteration num(i)
        cell_reference1 = K+str(i)
        cell_reference2 = W+str(i)
        cell_reference3 = X+str(i)
        cell_reference4 = AF+str(i)
        cell_reference5 = AG+str(i)
        cell_reference6 = AJ+str(i)
        cell_reference7 = AK+str(i)

        # Set the value in the cell
        worksheet[cell_reference1] = dob
        worksheet[cell_reference2] = submittedOn
        worksheet[cell_reference3] = submittedOn
        worksheet[cell_reference4] = loanSubmittedOn.strftime(date_format)
        worksheet[cell_reference5] = loanSubmittedOn.strftime(date_format)
        worksheet[cell_reference6] = firstRepayOn
        worksheet[cell_reference7] = dueDay


# Function call
dateCreationXl()

workbook.save(excelPath)

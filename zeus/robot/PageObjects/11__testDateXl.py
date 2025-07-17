import openpyxl
from datetime import datetime
from datetime import date
from faker import Faker
from datetime import datetime, timedelta
import random
import os
import glob

loanCount1 = '${loanCount1}'
K = "K"
W = "W"
X = "X"
AF = "AF"
AG = "AG"
AJ = "AJ"
AK = "AK"


def dateCreationXl(loanCount1):
    current_directory = os.getcwd()
    download_directory = os.path.join(
        current_directory, 'zeus', 'robot', 'PageObjects', 'testData')
    # download_directory = "D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads"
    search_pattern = os.path.join(download_directory, "*.xlsx")
    try:
        files = glob.glob(search_pattern)
        if not files:
            print("No Excel files found in the specified directory.")
            return
        # get the most recent file using file creation time
        recent_file = max(files, key=os.path.getctime)
        excelPath = recent_file
        print(excelPath)
        workbook = openpyxl.load_workbook(excelPath)
        for i in range(2, loanCount1):
            dob = '17-11-1999'
            submittedOn = '02-06-2019'

            fake = Faker()
            start_date = datetime(2023, 5, 1)
            end_date = datetime(2024, 1, 1)

            loanSubmittedOn = fake.date_between(
                start_date=start_date, end_date=end_date)
            # loanSubmittedOn = fake.date_this_year()
            # Take loan submitted on date and increase 31days for 1st repayment on
            date_format = '%d-%m-%Y'
            next_month = loanSubmittedOn + timedelta(days=31)
            firstRepayOn = next_month.strftime(date_format)

            # Convert the string to a datetime object
            dob = datetime.strptime(dob, date_format)
            dob = dob.date()
            submittedOn = datetime.strptime(submittedOn, date_format)
            submittedOn = submittedOn.date()
            firstRepayOn = datetime.strptime(firstRepayOn, date_format)
            firstRepayOn = firstRepayOn.date()
            # dueDay = firstRepayOn.day - it will take 1st repayment date as all due day
            dueDay = random.randint(1, 31)
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
            worksheet[cell_reference4] = loanSubmittedOn
            worksheet[cell_reference5] = loanSubmittedOn
            worksheet[cell_reference6] = firstRepayOn
            worksheet[cell_reference7] = dueDay
            workbook.save(excelPath)

    except Exception as e:
        print(f"An error occurred: {e}")

    #     for i in range(loanCount2, loanCount1):
    #         dob = '17-11-2015'
    #         submittedOn = '11-09-2019'

    #         fake = Faker()
    #         loanSubmittedOn = fake.date_this_year()
    #         # Take loan submitted on date and increase 31days for 1st repayment on
    #         date_format = '%d-%m-%Y'
    #         next_month = loanSubmittedOn + timedelta(days=35)
    #         firstRepayOn = next_month.strftime(date_format)

    #         # Convert the string to a datetime object
    #         dob = datetime.strptime(dob, date_format)
    #         dob = dob.date()
    #         submittedOn = datetime.strptime(submittedOn, date_format)
    #         submittedOn = submittedOn.date()
    #         firstRepayOn = datetime.strptime(firstRepayOn, date_format)
    #         firstRepayOn = firstRepayOn.date()
    #         dueDay = random.randint(1, 31)
    #         worksheet = workbook['ClientLoan']

    #         # concatenate  excel column into loop iteration num(i)
    #         cell_reference1 = K+str(i)
    #         cell_reference2 = W+str(i)
    #         cell_reference3 = X+str(i)
    #         cell_reference4 = AF+str(i)
    #         cell_reference5 = AG+str(i)
    #         cell_reference6 = AJ+str(i)
    #         cell_reference7 = AK+str(i)

    #         # Set the value in the cell
    #         worksheet[cell_reference1] = dob
    #         worksheet[cell_reference2] = submittedOn
    #         worksheet[cell_reference3] = submittedOn
    #         worksheet[cell_reference4] = loanSubmittedOn
    #         worksheet[cell_reference5] = loanSubmittedOn
    #         worksheet[cell_reference6] = firstRepayOn
    #         worksheet[cell_reference7] = dueDay
    #         workbook.save(excelPath)

    # for i in range(16, 22):
    #     dob = '01-01-2000'
    #     submittedOn = '01-10-2019'

    #     fake = Faker()
    #     loanSubmittedOn = fake.date_this_year()
    #     # Take loan submitted on date and increase 31days for 1st repayment on
    #     date_format = '%d-%m-%Y'
    #     next_month = loanSubmittedOn + timedelta(days=28)
    #     firstRepayOn = next_month.strftime(date_format)

    #     # Convert the string to a datetime object
    #     dob = datetime.strptime(dob, date_format)
    #     dob = dob.date()
    #     submittedOn = datetime.strptime(submittedOn, date_format)
    #     submittedOn = submittedOn.date()
    #     firstRepayOn = datetime.strptime(firstRepayOn, date_format)
    #     firstRepayOn = firstRepayOn.date()
    #     dueDay = random.randint(1, 31)
    #     worksheet = workbook['ClientLoan']

    #     # concatenate  excel column into loop iteration num(i)
    #     cell_reference1 = K+str(i)
    #     cell_reference2 = W+str(i)
    #     cell_reference3 = X+str(i)
    #     cell_reference4 = AF+str(i)
    #     cell_reference5 = AG+str(i)
    #     cell_reference6 = AJ+str(i)
    #     cell_reference7 = AK+str(i)

    #     # Set the value in the cell
    #     worksheet[cell_reference1] = dob
    #     worksheet[cell_reference2] = submittedOn
    #     worksheet[cell_reference3] = submittedOn
    #     worksheet[cell_reference4] = loanSubmittedOn
    #     worksheet[cell_reference5] = loanSubmittedOn
    #     worksheet[cell_reference6] = firstRepayOn
    #     worksheet[cell_reference7] = dueDay
    #     workbook.save(excelPath)


# Function call
dateCreationXl(loanCount1)

# workbook.save(excelPath)

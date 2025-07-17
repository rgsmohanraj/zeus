import csv
import os

current_directory = os.getcwd()

file_path = os.path.join(
    current_directory, 'zeus', 'robot', 'PageObjects', 'testData', 'Disbursement_Report.csv')
clientName = '${clientName}'
dob = '${dob}'
gender = '${gender}'
pan = '${pan}'
passport = '${passport}'
voterId = '${voterId}'
DL = '${DL}'
mobNum = '${mobNum}'
email = '${email}'
address = '${address}'
stateCode1 = '${stateCode1}'
pinCode = '${pinCode}'
disburseDate = '${disburseDate}'
lastPaidDateRs = '${lastPaidDate}'
dateClosed = '${dateClosed}'
emiRs = '${emiRs}'
dateReported = '${dateReported}'
principal = '${principal}'
interest = '${interest}'
tenure = '${tenure}'
accType = '${accType}'
assetClassify = '${assetClassify}'
writeOffAmount = '${writeOffAmount}'
writeOffPriAmount = '${writeOffPriAmount}'
loanAccNo = '${loanAccNo}'
posRs = '${currentBalance}'
dpdDiff = '${dpdDiff}'
odAmountRs = '${odAmountRs}'
loanStatus = '${status}'


def search_bureau_csv_by_loanAccountNumber(file_path, clientName, dob, gender, pan, passport, voterId, DL, mobNum, email, address, stateCode1, pinCode, disburseDate, lastPaidDateRs, dateClosed, emiRs, dateReported, principal, interest, tenure, accType, assetClassify, writeOffAmount, writeOffPriAmount, loanAccNo, posRs, dpdDiff, odAmountRs, loanStatus):
    with open(file_path, 'r') as csv_file:
        read = csv.reader(csv_file)
        next(read)
        loop_failed = False     # Variable to track loop status
        for line in read:
            if loanAccNo in line:
                print(line)
                print(file_path)
                if (line[35]) == loanAccNo:
                    print(
                        f"The loan Account Number ({line[35]})=({loanAccNo}) matches the value in the CSV.")
                else:
                    print(
                        f"The loan Account Number ({line[35]})!=({loanAccNo}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[0]) == clientName:
                    print(
                        f"The client Name ({line[0]})=({clientName}) matches the value in the CSV.")
                else:
                    print(
                        f"The clientName ({line[0]})!=({clientName}) does not match the value in the CSV.")
                    loop_failed = True
                line_1 = line[1].replace("'", "")
                if (line_1) == dob:
                    print(
                        f"The DOB ({line_1})=({dob}) matches the value in the CSV.")
                else:
                    print(
                        f"The DOB ({line_1})!=({dob}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[2]) == gender:
                    print(
                        f"The Gender ({line[2]})=({gender}) matches the value in the CSV.")
                else:
                    print(
                        f"The Gender ({line[2]})!=({gender}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[3]) == pan:
                    print(
                        f"The PAN Number ({line[3]})=({pan}) matches the value in the CSV.")
                else:
                    print(
                        f"The PAN Number ({line[3]})!=({pan}) does not match the value in the CSV.")
                    loop_failed = True
                if passport is None:
                    passport = ""
                if (line[4]) == passport:
                    print(
                        f"The Passport Number ({line[4]})=({passport}) matches the value in the CSV.")
                else:
                    print(
                        f"The Passport Number ({line[4]})!=({passport}) does not match the value in the CSV.")
                    loop_failed = True
                if voterId is None:
                    voterId = ""
                if (line[7]) == voterId:
                    print(
                        f"The voterId ({line[7]})=({voterId}) matches the value in the CSV.")
                else:
                    print(
                        f"The voterId ({line[7]})!=({voterId}) does not match the value in the CSV.")
                    loop_failed = True
                if DL is None:
                    DL = ""
                if (line[8]) == DL:
                    print(
                        f"The Driving license ({line[8]})=({DL}) matches the value in the CSV.")
                else:
                    print(
                        f"The Driving license ({line[8]})!=({DL}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[15]) == str(mobNum):
                    print(
                        f"The Telephone No.Mobile ({line[15]})=({mobNum}) matches the value in the CSV.")
                else:
                    print(
                        f"The Telephone No.Mobile ({line[15]})!=({mobNum}) does not match the value in the CSV.")
                    loop_failed = True
                if email is None:
                    email = ""
                if (line[21]) == email:
                    print(
                        f"The Email ID-1 ({line[21]})=({email}) matches the value in the CSV.")
                else:
                    print(
                        f"The Email ID-1 ({line[21]})!=({email}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[23]) == address:
                    print(
                        f"The Address 1 ({line[23]})=({address}) matches the value in the CSV.")
                else:
                    print(
                        f"The Address 1 ({line[23]})!=({address}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[24]) == stateCode1:
                    print(
                        f"The stateCode1 ({line[24]})=({stateCode1}) matches the value in the CSV.")
                else:
                    print(
                        f"The stateCode1 ({line[24]})!=({stateCode1}) does not match the value in the CSV.")
                    loop_failed = True
                if int(line[25]) == pinCode:
                    print(
                        f"The PinCode ({line[25]})=({pinCode}) matches the value in the CSV.")
                else:
                    print(
                        f"The PinCode ({line[25]})!=({pinCode}) does not match the value in the CSV.")
                    loop_failed = True
                line_36 = line[36].replace("0", "")
                if line_36 == accType:
                    print(
                        f"The Account Type ({line_36})=({accType}) matches the value in the CSV.")
                else:
                    print(
                        f"The Account Type ({line_36})!=({accType}) does not match the value in the CSV.")
                    loop_failed = True
                line_38 = line[38].replace("'", "")
                if line_38 == disburseDate:
                    print(
                        f"The Date Opened/Disbursed ({line_38})=({disburseDate}) matches the value in the CSV.")
                else:
                    print(
                        f"The Date Opened/Disbursed ({line_38})!=({disburseDate}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[39]) == lastPaidDateRs:
                    print(
                        f"The Date of last payment ({line[39]})=({lastPaidDateRs}) matches the value in the CSV.")
                else:
                    print(
                        f"The Date of last payment ({line[39]})!=({lastPaidDateRs}) does not match the value in the CSV.")
                    loop_failed = True
                if dateClosed == None:
                    dateClosed = ""
                if (line[40]) == dateClosed:
                    print(
                        f"The Date of loan closed ({line[40]})=({dateClosed}) matches the value in the CSV.")
                else:
                    print(
                        f"The Date of loan closed ({line[40]})!=({dateClosed}) does not match the value in the CSV.")
                    loop_failed = True
                if str(line[41]) == dateReported:
                    print(
                        f"The Date Reported ({line[41]})=({dateReported}) matches the value in the CSV.")
                else:
                    print(
                        f"The Date Reported ({line[41]})!=({dateReported}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[42]) == principal:
                    print(
                        f"The High Credit/Sanctioned Amt ({line[42]})=({principal}) matches the value in the CSV.")
                else:
                    print(
                        f"The High Credit/Sanctioned Amt ({line[42]})!=({principal}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[43]) == posRs:
                    print(
                        f"The Current Balance/POS ({line[43]})=({posRs}) matches the value in the CSV.")
                else:
                    print(
                        f"The Current Balance/POS ({line[43]})!=({posRs}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[44]) == odAmountRs:
                    print(
                        f"The Amt Overdue ({line[44]})=({odAmountRs}) matches the value in the CSV.")
                else:
                    print(
                        f"The Amt Overdue ({line[44]})!=({odAmountRs}) does not match the value in the CSV.")
                    # loop_failed = True
                if loanStatus not in 'Closed':
                    if (line[45]) == str(dpdDiff):
                        print(
                            f"The No of Days Past Due ({line[45]})=({dpdDiff}) matches the value in the CSV.")
                    else:
                        print(
                            f"The No of Days Past Due ({line[45]})!=({dpdDiff}) does not match the value in the CSV.")
                        loop_failed = True
                if (line[53]) == assetClassify:
                    print(
                        f"The Asset Classification ({line[53]})=({assetClassify}) matches the value in the CSV.")
                else:
                    print(
                        f"The Asset Classification ({line[53]})!=({assetClassify}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[58]) == interest:
                    print(
                        f"The Rate of Interest ({line[58]})=({interest}) matches the value in the CSV.")
                else:
                    print(
                        f"The Rate of Interest ({line[58]})!=({interest}) does not match the value in the CSV.")
                    loop_failed = True
                if int(line[59]) == tenure:
                    print(
                        f"The Repayment Tenure ({line[59]})=({tenure}) matches the value in the CSV.")
                else:
                    print(
                        f"The Repayment Tenure ({line[59]})!=({tenure}) does not match the value in the CSV.")
                    loop_failed = True
                if emiRs != '':
                    if float(line[60]) == emiRs:
                        print(
                            f"The EMI Amount ({line[60]})=({emiRs}) matches the value in the CSV.")
                    else:
                        print(
                            f"The EMI Amount ({line[60]})!=({emiRs}) does not match the value in the CSV.")
                        loop_failed = True
                if float(line[61]) == float(writeOffAmount):
                    print(
                        f"The Written-off Amount(Total) ({line[61]})=({writeOffAmount}) matches the value in the CSV.")
                else:
                    print(
                        f"The Written-off Amount(Total) ({line[61]})!=({writeOffAmount}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[62]) == float(writeOffPriAmount):
                    print(
                        f"The Written-off Principal Amount ({line[62]})=({writeOffPriAmount}) matches the value in the CSV.")
                else:
                    print(
                        f"The Written-off Principal Amount ({line[62]})!=({writeOffPriAmount}) does not match the value in the CSV.")
                    loop_failed = True
                if loanStatus not in 'Active':
                    if (line[60]) == '':
                        print(
                            f"The EMI Amount ({line[60]})==("") matches the value in the CSV.")
                    else:
                        print(
                            f"The EMI Amount ({line[60]})!=("") does not matches the value in the CSV.")
                        loop_failed = True
    if loop_failed:
        return "FAIL"
        print(f"Loop number {index} failed!")
    return "PASS"


search_bureau_csv_by_loanAccountNumber(file_path, clientName, dob, gender, pan, passport, voterId, DL, mobNum, email, address, stateCode1,
                                       pinCode, disburseDate, lastPaidDateRs, dateClosed, emiRs, dateReported, principal, interest, tenure, accType, assetClassify, writeOffAmount, writeOffPriAmount, loanAccNo, posRs, dpdDiff, odAmountRs, loanStatus)

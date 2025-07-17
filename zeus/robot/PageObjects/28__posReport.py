import csv
import os

current_directory = os.getcwd()

file_path = os.path.join(
    current_directory, 'zeus', 'robot', 'PageObjects', 'testData', 'Disbursement_Report.csv')
clientName = '${clientName}'
cifId = '${clientIdRs}'
productCls = '${productClsRs}'
loanAccNo = '${loanAccNo}'
disburseDate = '${disburDate}'
maturityDate = '${maturityDate}'
tenure = '${tenure}'
remTenure = '${remTenure}'
principal = '${principal}'
interest = '${interest}'
emi = '${emi}'
aging = '${aging}'
closingDate = '${closingDate}'
statusLoan = '${statusLoan}'
lastPaidDateRs = '${lastPaidDate}'
lastPaidAmountRs = '${lastPaidAmount}'
# dateClosed = '${dateClosed}'
# emiRs = '${emiRs}'
# dateReported = '${dateReported}'
# posRs = '${currentBalance}'
# dpdDiff = '${dpdDiff}'
# odAmountRs = '${odAmountRs}'


# def search_pos_csv_by_loanAccountNumber(file_path, cifId, clientName, productCls, loanAccNo,disburseDate, maturityDate, lastPaidDateRs, dateClosed, emiRs, dateReported, principal, interest, tenure, loanAccNo, posRs, dpdDiff, odAmountRs, loanStatus):
def search_pos_csv_by_loanAccountNumber(file_path, cifId, clientName, productCls, loanAccNo, disburseDate, maturityDate, tenure, remTenure, interest, principal, emi, aging, closingDate, statusLoan, lastPaidDateRs, lastPaidAmountRs):
    with open(file_path, 'r') as csv_file:
        read = csv.reader(csv_file)
        print(file_path)
        next(read)
        loop_failed = False     # Variable to track loop status
        for line in read:
            if loanAccNo in line:
                print(line)
                print(file_path)
                line_0 = line[0].replace("000000", "")
                if int(line[0]) == cifId:
                    print(
                        f"The client Id ({line_0})=({cifId}) matches the value in the CSV.")
                else:
                    print(
                        f"The client Id ({line_0})!=({cifId}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[1]) == clientName:
                    print(
                        f"The client Name ({line[1]})=({clientName}) matches the value in the CSV.")
                else:
                    print(
                        f"The clientName ({line[1]})!=({clientName}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[2]) == productCls:
                    print(
                        f"The Product class ({line[2]})=({productCls}) matches the value in the CSV.")
                else:
                    print(
                        f"The Product class ({line[2]})!=({productCls}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[3]) == loanAccNo:
                    print(
                        f"The loan Account Number ({line[3]})=({loanAccNo}) matches the value in the CSV.")
                else:
                    print(
                        f"The loan Account Number ({line[3]})!=({loanAccNo}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[4]) == disburseDate:
                    print(
                        f"The Date Opened/Disbursed ({line[4]})=({disburseDate}) matches the value in the CSV.")
                else:
                    print(
                        f"The Date Opened/Disbursed ({line[4]})!=({disburseDate}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[5]) == maturityDate:
                    print(
                        f"The maturity Date ({line[5]})=({maturityDate}) matches the value in the CSV.")
                else:
                    print(
                        f"The maturityDate ({line[5]})!=({maturityDate}) does not match the value in the CSV.")
                    loop_failed = True
                if int(line[6]) == tenure:
                    print(
                        f"The Repayment Tenure ({line[6]})=({tenure}) matches the value in the CSV.")
                else:
                    print(
                        f"The Repayment Tenure ({line[6]})!=({tenure}) does not match the value in the CSV.")
                    loop_failed = True
                if int(line[7]) == remTenure:
                    print(
                        f"The Remaining Tenure ({line[7]})=({remTenure}) matches the value in the CSV.")
                else:
                    print(
                        f"The Remaining Tenure ({line[7]})!=({remTenure}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[8]) == interest:
                    print(
                        f"The Rate of Interest ({line[8]})=({interest}) matches the value in the CSV.")
                else:
                    print(
                        f"The Rate of Interest ({line[8]})!=({interest}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[9]) == principal:
                    print(
                        f"The Loan amount ({line[9]})=({principal}) matches the value in the CSV.")
                else:
                    print(
                        f"The Loan amount ({line[9]})!=({principal}) does not match the value in the CSV.")
                    loop_failed = True
                if emi != '':
                    if float(line[14]) == emi:
                        print(
                            f"The EMI Amount ({line[14]})=({emi}) matches the value in the CSV.")
                    else:
                        print(
                            f"The EMI Amount ({line[14]})!=({emi}) does not match the value in the CSV.")
                        loop_failed = True
                if (line[15]) == str(aging):
                    print(
                        f"The Aging ({line[15]})=({aging}) matches the value in the CSV.")
                else:
                    print(
                        f"The Aging ({line[15]})!=({aging}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[16]) == closingDate:
                    print(
                        f"The Date of loan closed ({line[16]})=({closingDate}) matches the value in the CSV.")
                else:
                    print(
                        f"The Date of loan closed ({line[16]})!=({closingDate}) does not match the value in the CSV.")
                    loop_failed = True
                if str(line[17]) == statusLoan:
                    print(
                        f"The Loan Status ({line[17]})=({statusLoan}) matches the value in the CSV.")
                else:
                    print(
                        f"The Loan Status ({line[17]})!=({statusLoan}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[18]) == lastPaidDateRs:
                    print(
                        f"The Date of last payment ({line[18]})=({lastPaidDateRs}) matches the value in the CSV.")
                else:
                    print(
                        f"The Date of last payment ({line[18]})!=({lastPaidDateRs}) does not match the value in the CSV.")
                    loop_failed = True
                if lastPaidAmountRs != '':
                    if (line[19]) == lastPaidAmountRs:
                        print(
                            f"The last paid amount ({line[19]})=({lastPaidAmountRs}) matches the value in the CSV.")
                    else:
                        print(
                            f"The last paid amount ({line[19]})!=({lastPaidAmountRs}) does not match the value in the CSV.")
                        loop_failed = True
                # if dateClosed == None:
                #     dateClosed = ""
                # if str(line[41]) == dateReported:
                #     print(
                #         f"The Date Reported ({line[41]})=({dateReported}) matches the value in the CSV.")
                # else:
                #     print(
                #         f"The Date Reported ({line[41]})!=({dateReported}) does not match the value in the CSV.")
                #     loop_failed = True

                # if float(line[43]) == posRs:
                #     print(
                #         f"The Current Balance/POS ({line[43]})=({posRs}) matches the value in the CSV.")
                # else:
                #     print(
                #         f"The Current Balance/POS ({line[43]})!=({posRs}) does not match the value in the CSV.")
                #     loop_failed = True
                # if float(line[44]) == odAmountRs:
                #     print(
                #         f"The Amt Overdue ({line[44]})=({odAmountRs}) matches the value in the CSV.")
                # else:
                #     print(
                #         f"The Amt Overdue ({line[44]})!=({odAmountRs}) does not match the value in the CSV.")
                #     # loop_failed = True
                # if loanStatus not in 'Closed':
                #     if (line[45]) == str(dpdDiff):
                #         print(
                #             f"The No of Days Past Due ({line[45]})=({dpdDiff}) matches the value in the CSV.")
                #     else:
                #         print(
                #             f"The No of Days Past Due ({line[45]})!=({dpdDiff}) does not match the value in the CSV.")
                #         loop_failed = True

    if loop_failed:
        return "FAIL"
        print(f"Loop number {index} failed!")
    return "PASS"


# search_pos_csv_by_loanAccountNumber(file_path, cifId, clientName, productCls, disburseDate, maturityDate, lastPaidDateRs, dateClosed,
    # emiRs, dateReported, principal, interest, tenure, loanAccNo, posRs, dpdDiff, odAmountRs, loanStatus)
search_pos_csv_by_loanAccountNumber(
    file_path, cifId, clientName, productCls, loanAccNo, disburseDate, maturityDate, tenure, remTenure, interest, principal, emi, aging, closingDate, statusLoan, lastPaidDateRs, lastPaidAmountRs)

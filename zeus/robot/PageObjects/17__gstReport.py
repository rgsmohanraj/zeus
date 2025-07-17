import csv
import os
from datetime import datetime
from datetime import datetime, timedelta

current_directory = os.getcwd()

file_path = os.path.join(
    current_directory, 'zeus', 'robot', 'PageObjects', 'testData', 'Disbursement_Report.csv')
external_id = '${exId}'
loanAccNo = '${loanAccNo}'
client = '${clientName}'
Type = '${Type}'
partner = '${partner}'
assetCls = '${assetCls}'
index = '${index}'
stateCode = '${stateCode1}'
processFees = '${processFees}'
processFeeAmount = '${processFeeGSTAmount}'
insCharges = '${insCharges}'
insChargesAmount = '${insChargesAmountEx}'
totalChargesDeducted = '${totalChargesDeducted}'
totalGSTDeducted = '${totalGSTDeducted}'
disburseDate = '${disburseDate}'
ProcessingFees = 'Processing Fees'
InsuranceCharges = 'Insurance Charges'
billState = '${billState}'
billCountry = '${billCountry}'
CGSTpf = '${CGSTpf}'
CGSTic = '${CGSTic}'
SGSTpf = '${SGSTpf}'
SGSTic = '${SGSTic}'
IGSTpf = '${IGSTpf}'
IGSTic = '${IGSTic}'
HSNcode = '${HSNcode}'
subject = '${subject}'


def search_csv_by_loanAccountNumber(file_path, external_id, loanAccNo, client, Type, assetCls, partner, index, stateCode, processFees, processFeeAmount, insCharges, insChargesAmount, totalChargesDeducted, totalGSTDeducted, disburseDate, billState, billCountry, CGSTpf, CGSTic, SGSTpf, SGSTic, IGSTpf, IGSTic, HSNcode, subject):
    with open(file_path, 'r') as csv_file:
        read = csv.reader(csv_file)
        print(file_path)
        next(read)
        loop_failed = False     # Variable to track loop status
        loop_failed1 = False
        for line in read:
            if loanAccNo in line:
                print(line)
                print(file_path)
                if str(line[1]) == disburseDate:
                    print(
                        f"The Disbursement Date ({line[1]})=({disburseDate}) matches the value in the CSV.")
                else:
                    print(
                        f"The Disbursement Date ({line[1]})!=({disburseDate}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[2]) == loanAccNo:
                    print(
                        f"The loan Account Number ({line[2]})=({loanAccNo}) matches the value in the CSV.")
                else:
                    print(
                        f"The loan Account Number ({line[2]})!=({loanAccNo}) does not match the value in the CSV.")
                    loop_failed = True
                # if (line[3]) == assetCls:
                #     print(
                #         f"The Asset class ({line[3]})=({assetCls}) matches the value in the CSV.")
                # else:
                #     print(
                #         f"The Asset class ({line[3]})!=({assetCls}) does not match the value in the CSV.")
                #     loop_failed = True
                if str(line[4]) == partner:
                    print(
                        f"The Partner name ({line[4]})=({partner}) matches the value in the CSV.")
                else:
                    print(
                        f"The Partner name ({line[4]})!=({partner}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[5]) == client:
                    print(
                        f"The End Borrower Name ({line[5]})=({client}) matches the value in the CSV.")
                else:
                    print(
                        f"The End Borrower Name ({line[5]})!=({client}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[6]) == Type:
                    print(
                        f"The Type ({line[6]})=({Type}) matches the value in the CSV.")
                else:
                    print(
                        f"The Type ({line[6]})!=({Type}) does not match the value in the CSV.")
                    loop_failed = True
                if int(line[7]) == stateCode:
                    print(
                        f"The stateCode ({line[7]})=({stateCode}) matches the value in the CSV.")
                else:
                    print(
                        f"The stateCode ({line[7]})!=({stateCode}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[8]) == float(line[11]):
                    print(
                        f"Sub total ({line[8]}) and Item Price ({line[11]}) are same")
                else:
                    print(
                        f"Sub total ({line[8]}) and Item Price ({line[11]}) are not same")
                    loop_failed = True
                if int(line[7]) == int(33):
                    if float(line[14]) == float(9):
                        print(
                            f"The Tax Rate CGST% ({line[14]})=(9) matches the value in the CSV.")
                    else:
                        print(
                            f"The Tax Rate CGST% ({line[14]})!=(9) does not match the value in the CSV.")
                        loop_failed = True
                if int(line[7]) == int(33) and str(line[10]) == ProcessingFees:
                    if float(line[15]) == CGSTpf:
                        print(
                            f"The CGST Processing fees ({line[15]})=({CGSTpf}) matches the value in the CSV.")
                    else:
                        print(
                            f"The CGST Processing fees ({line[15]})!=({CGSTpf}) does not match the value in the CSV.")
                        loop_failed = True
                    if float(line[17]) == SGSTpf:
                        print(
                            f"The SGST Processing fees ({line[17]})=({SGSTpf}) matches the value in the CSV.")
                    else:
                        print(
                            f"The SGST Processing fees ({line[17]})!=({SGSTpf}) does not match the value in the CSV.")
                        loop_failed = True
                if int(line[7]) == int(33) and str(line[10]) == InsuranceCharges:
                    if float(line[15]) == CGSTic:
                        print(
                            f"The CGST Insurance Charges({line[15]})=({CGSTic}) matches the value in the CSV.")
                    else:
                        print(
                            f"The CGST Insurance Charges({line[15]})!=({CGSTic}) does not match the value in the CSV.")
                        loop_failed = True
                    if float(line[17]) == SGSTic:
                        print(
                            f"The SGST Insurance Charges({line[17]})=({SGSTic}) matches the value in the CSV.")
                    else:
                        print(
                            f"The SGST Insurance Charges({line[17]})!=({SGSTic}) does not match the value in the CSV.")
                        loop_failed = True
                if int(line[7]) != int(33) and str(line[10]) == ProcessingFees:
                    if float(line[19]) == IGSTpf:
                        print(
                            f"The IGST Processing fees ({line[19]})=({IGSTpf}) matches the value in the CSV.")
                    else:
                        print(
                            f"The IGST Processing fees  ({line[19]})!=({IGSTpf}) does not match the value in the CSV.")
                        loop_failed = True
                if int(line[7]) != int(33) and str(line[10]) == InsuranceCharges:
                    if float(line[19]) == IGSTic:
                        print(
                            f"The IGST Insurance Charges ({line[19]})=({IGSTic}) matches the value in the CSV.")
                    else:
                        print(
                            f"The IGST Insurance Charges ({line[19]})!=({IGSTic}) does not match the value in the CSV.")
                        loop_failed = True
                if int(line[7]) != int(33):
                    if float(line[18]) == int(18):
                        print(
                            f"The Tax Rate IGST % ({line[18]})=(18) matches the value in the CSV.")
                    else:
                        print(
                            f"The Tax Rate IGST % ({line[18]})!=(18) does not match the value in the CSV.")
                        loop_failed = True
                if int(line[7]) == int(33):
                    if float(line[16]) == float(9):
                        print(
                            f"The Tax Rate SGST % ({line[16]})=(9) matches the value in the CSV.")
                    else:
                        print(
                            f"The Tax Rate SGST % ({line[16]})!=(9) does not match the value in the CSV.")
                        loop_failed = True
                if str(line[10]) == ProcessingFees:
                    if float(line[9]) == processFees:
                        print(
                            f"The ProcessingFees ({line[9]})=({processFees}) matches the value in the CSV.")
                    else:
                        print(
                            f"The ProcessingFees ({line[9]})!=({processFees}) does not match the value in the CSV.")
                        loop_failed = True
                if str(line[10]) == InsuranceCharges:
                    if float(line[9]) == insCharges:
                        print(
                            f"The Insurance Charges ({line[9]})=({insCharges}) matches the value in the CSV.")
                    else:
                        print(
                            f"The Insurance Charges ({line[9]})!=({insCharges}) does not match the value in the CSV.")
                        loop_failed = True
                if str(line[12]) == billState:
                    print(
                        f"The Billing State ({line[12]})=({billState}) matches the value in the CSV.")
                else:
                    print(
                        f"The Billing State ({line[12]})!=({billState}) does not match the value in the CSV.")
                    loop_failed = True
                if str(line[13]) == billCountry:
                    print(
                        f"The Billing Country ({line[13]})=({billCountry}) matches the value in the CSV.")
                else:
                    print(
                        f"The Billing Country({line[13]})!=({billCountry}) does not match the value in the CSV.")
                    loop_failed = True
                if int(line[21]) == HSNcode:
                    print(
                        f"The HSN code ({line[21]})=({HSNcode}) matches the value in the CSV.")
                else:
                    print(
                        f"The HSN code ({line[21]})!=({HSNcode}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[22]) == subject:
                    print(
                        f"The Subject ({line[22]})=({subject}) matches the value in the CSV.")
                else:
                    print(
                        f"The Subject ({line[22]})!=({subject}) does not match the value in the CSV.")
                    loop_failed = True

    if loop_failed:
        return "FAIL"
        print(f"Loop number {index} failed!")
    return "PASS"


search_csv_by_loanAccountNumber(file_path, external_id, loanAccNo, client, Type, assetCls, partner, index, stateCode, processFees, processFeeAmount,
                                insCharges, insChargesAmount, totalChargesDeducted, totalGSTDeducted, disburseDate, billState, billCountry, CGSTpf, CGSTic, SGSTpf, SGSTic, IGSTpf, IGSTic, HSNcode, subject)

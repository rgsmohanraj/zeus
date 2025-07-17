import csv
import os
from datetime import datetime
from datetime import datetime, timedelta

current_directory = os.getcwd()

file_path = os.path.join(
    current_directory, 'zeus', 'robot', 'PageObjects', 'testData', 'Disbursement_Report.csv')
partnerIdRs = '${partnerIdRs}'
external_id = '${exId}'
loanAccNoRs = '${loanAccNoRs}'
client = '${clientName}'
partner = '${partner}'
principal = '${principal}'
tenure = '${tenure}'
interest = '${interest}'
index = '${index}'
processFees = '${processFees}'
insCharges = '${insCharges}'
TOARs = '${TOARs}'
xirrDB = '${xirrDB}'
vcplHurdleRs = '${vcplHurdleRs}'
transTypeRs = '${transTypeRs}'
pennyStatusRs = '${pennyStatusRs}'
UTRnumRs = '${UTRnumRs}'
firstRepay = '${1stRepay}'
maturesOn = '${maturesOn}'
totalChargesDeducted = '${totalChargesDeducted}'
totalGSTDeducted = '${totalGSTDeducted}'
netDisAmount = '${netDisAmount}'
insLifeCover = '${insLifeCover}'
insHospicash = '${insHospicash}'
stampDuty = '${stampDuty}'
disburseDate = '${disburseDate}'
statusRs = '${statusRs}'
Dvara = 'Dvara'


def search_csv_by_external_id(file_path, partnerIdRs, external_id, loanAccNoRs, client, partner, principal, tenure, interest, index, TOARs, xirrDB, vcplHurdleRs, transTypeRs, pennyStatusRs, UTRnumRs, firstRepay, processFees, insCharges, maturesOn, totalChargesDeducted, totalGSTDeducted, netDisAmount, insLifeCover, insHospicash, stampDuty, disburseDate, statusRs):
    with open(file_path, 'r') as csv_file:
        read = csv.reader(csv_file)
        print(file_path)
        next(read)
        loop_failed = False  # Variable to track loop status
        loop_failed1 = False
        for line in read:
            if external_id in line:
                print(line)
                values = line[6:9]
                print(values)
                if int(line[0]) == partnerIdRs:
                    print(
                        f"The Partner id ({line[0]})=({partnerIdRs}) matches the value in the CSV.")
                else:
                    print(
                        f"The Partner id ({line[0]})!=({partnerIdRs}) does not match the value in the CSV.")
                    loop_failed = True
                if str(line[1]) == partner:
                    print(
                        f"The Partner name ({line[1]})=({partner}) matches the value in the CSV.")
                else:
                    print(
                        f"The Partner name ({line[1]})!=({partner}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[2]) == client:
                    print(
                        f"The End Borrower Name ({line[2]})=({client}) matches the value in the CSV.")
                else:
                    print(
                        f"The End Borrower Name ({line[2]})!=({client}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[3]) == external_id:
                    print(
                        f"The External id ({line[3]})=({external_id}) matches the value in the CSV.")
                else:
                    print(
                        f"The External id ({line[3]})!=({external_id}) does not match the value in the CSV.")
                    loop_failed = True
                if (line[4]) == loanAccNoRs:
                    print(
                        f"The Loan Account No ({line[4]})=({loanAccNoRs}) matches the value in the CSV.")
                else:
                    print(
                        f"The Loan Account No ({line[4]})!=({loanAccNoRs}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[5]) == principal:
                    print(
                        f"The Principal ({line[5]})=({principal}) matches the value in the CSV.")
                else:
                    print(
                        f"The Principal ({line[5]})!=({principal}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[6]) == interest:
                    print(
                        f"The Interest ({line[6]})=({interest}) matches the value in the CSV.")
                else:
                    print(
                        f"The Interest ({line[6]})!=({interest}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[7]) == tenure:
                    print(
                        f"The Tenure ({line[7]})=({tenure}) matches the value in the CSV.")
                else:
                    print(
                        f"The Tenure ({line[7]})!=({tenure}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[8]) == totalChargesDeducted:
                    print(
                        f"The Total Charges Deducted ({line[8]})=({totalChargesDeducted}) matches the value in the CSV.")
                else:
                    print(
                        f"The Total Charges Deducted ({line[8]})!=({totalChargesDeducted}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[9]) == totalGSTDeducted:
                    print(
                        f"The Total GST Deducted ({line[9]})=({totalGSTDeducted}) matches the value in the CSV.")
                else:
                    print(
                        f"The Total GST Deducted ({line[9]})!=({totalGSTDeducted}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[10]) == netDisAmount:
                    print(
                        f"The Net Disbursement Amount ({line[10]})=({netDisAmount}) matches the value in the CSV.")
                else:
                    print(
                        f"The Net Disbursement Amount ({line[10]})!=({netDisAmount}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[11]) == TOARs:
                    print(
                        f"The Total outstanding amount ({line[11]})=({TOARs}) matches the value in the CSV.")
                else:
                    print(
                        f"The Total outstanding amount ({line[11]})!=({TOARs}) does not match the value in the CSV.")
                    loop_failed = True
                if str(line[12]) == xirrDB:
                    print(
                        f"The Initial XIRR ({line[12]})=({xirrDB}) matches the value in the CSV.")
                else:
                    print(
                        f"The Initial XIRR ({line[12]})!=({xirrDB}) does not match the value in the CSV.")
                    loop_failed = True
                if partner == Dvara:
                    if (line[13]) == vcplHurdleRs:
                        print(
                            f"The VCPL Hurdle Rate ({line[13]})=({vcplHurdleRs}) matches the value in the CSV.")
                    else:
                        print(
                            f"The VCPL Hurdle Rate ({line[13]})!=({vcplHurdleRs}) does not match the value in the CSV.")
                        loop_failed = True
                else:
                    if float(line[13]) == vcplHurdleRs:
                        print(
                            f"The VCPL Hurdle Rate ({line[13]})=({vcplHurdleRs}) matches the value in the CSV.")
                    else:
                        print(
                            f"The VCPL Hurdle Rate ({line[13]})!=({vcplHurdleRs}) does not match the value in the CSV.")
                        loop_failed = True
                if str(line[14]) == firstRepay:
                    print(
                        f"The FirstRepay ({line[14]})=({firstRepay}) matches the value in the CSV.")
                else:
                    print(
                        f"The FirstRepay ({line[14]})!=({firstRepay}) does not match the value in the CSV.")
                    loop_failed = True
                if str(line[15]) == maturesOn:
                    print(
                        f"The Loan Maturity Date ({line[15]})=({maturesOn}) matches the value in the CSV.")
                else:
                    print(
                        f"The Loan Matures On Date ({line[15]})!=({maturesOn}) does not match the value in the CSV.")
                    loop_failed = True
                if str(line[16]) == transTypeRs:
                    print(
                        f"The Transaction Type ({line[16]})=({transTypeRs}) matches the value in the CSV.")
                else:
                    print(
                        f"The Transaction Type ({line[16]})!=({transTypeRs}) does not match the value in the CSV.")
                    loop_failed = True
                if str(line[17]) == pennyStatusRs:
                    print(
                        f"The Penny Drop Status ({line[17]})=({pennyStatusRs}) matches the value in the CSV.")
                else:
                    print(
                        f"The Penny Drop Status ({line[17]})!=({pennyStatusRs}) does not match the value in the CSV.")
                    loop_failed = True
                if str(line[18]) == UTRnumRs:
                    print(
                        f"The UTR No ({line[18]})=({UTRnumRs}) matches the value in the CSV.")
                else:
                    print(
                        f"The UTR No ({line[18]})!=({UTRnumRs}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[19]) == processFees:
                    print(
                        f"The ProcessFees ({line[19]})=({processFees}) matches the value in the CSV.")
                else:
                    print(
                        f"The ProcessFees ({line[19]})!=({processFees}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[20]) == insCharges:
                    print(
                        f"The Insurance Charges ({line[20]})=({insCharges}) matches the value in the CSV.")
                else:
                    print(
                        f"The Insurance Charges ({line[20]})!=({insCharges}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[21]) == insLifeCover:
                    print(
                        f"The Insurance Life Cover ({line[21]})=({insLifeCover}) matches the value in the CSV.")
                else:
                    print(
                        f"The Insurance Life Cover ({line[21]})!=({insLifeCover}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[22]) == insHospicash:
                    print(
                        f"The Insurance Hospicash ({line[22]})=({insHospicash}) matches the value in the CSV.")
                else:
                    print(
                        f"The Insurance Hospicash ({line[22]})!=({insHospicash}) does not match the value in the CSV.")
                    loop_failed = True
                if float(line[23]) == stampDuty:
                    print(
                        f"The Stamp duty ({line[23]})=({stampDuty}) matches the value in the CSV.")
                else:
                    print(
                        f"The Stamp duty ({line[23]})!=({stampDuty}) does not match the value in the CSV.")
                    loop_failed = True
                if str(line[24]) == disburseDate:
                    print(
                        f"The Disbursement Date ({line[24]})=({disburseDate}) matches the value in the CSV.")
                else:
                    print(
                        f"The Disbursement Date ({line[24]})!=({disburseDate}) does not match the value in the CSV.")
                    loop_failed = True
                if str(line[25]) == statusRs:
                    print(
                        f"The Loan Status ({line[25]})=({statusRs}) matches the value in the CSV.")
                else:
                    print(
                        f"The Loan Status ({line[25]})!=({statusRs}) does not match the value in the CSV.")
                    loop_failed = True
                    # return FAIL
    if loop_failed:
        return "FAIL"
        print(f"Loop number {index} failed!")
    return "PASS"


search_csv_by_external_id(file_path, partnerIdRs, external_id, loanAccNoRs, client, partner,
                          principal, tenure, interest, index, TOARs, xirrDB, firstRepay, vcplHurdleRs, transTypeRs, pennyStatusRs, UTRnumRs, processFees, insCharges, maturesOn, totalChargesDeducted, totalGSTDeducted, netDisAmount, insLifeCover, insHospicash, stampDuty, disburseDate, statusRs)

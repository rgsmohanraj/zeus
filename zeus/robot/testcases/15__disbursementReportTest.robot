*** Settings *** 
Resource    ../PageObjects/15__disbursementReport.robot
Suite Setup    Partner,Product and loan count details

*** Test Cases ***
Disbursement report test
    [Documentation]  Check in disbursement report values with bulk loan excel values
    # Open Browser and login to Zeus    ${userName}    ${password}
    # Download Disbursement report
    Download Disbursement report using API
    Check in disbursement report

Disbursement report test for total outstanding amount,XIRR,LoanId
    [Tags]   Ignore
    [Documentation]  Check in disbursement report total outstanding amount with UI RS
    Check XIRR and Total outstanding from dispursement report
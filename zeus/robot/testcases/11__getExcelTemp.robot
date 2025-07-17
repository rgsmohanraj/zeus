*** Settings ***
Resource    ../keywords/common.robot
Resource    ../PageObjects/11__bulkLoanUpload.robot
Library    11__getRecentFile.py

*** Keywords ***
Process Recent Excel File
    ${file_path}      11__getRecentFile.Get Most Recent Excel File
    Set Global Variable    ${file_path}
    Log    The most recent Excel file: ${file_path}
    [Return]    ${file_path}

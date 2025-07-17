*** Settings ***
Resource    ../keywords/common.robot
Resource    ../PageObjects/01__loginPage.robot
Resource    ../PageObjects/13__bulkLoanCollection.robot

** Test Cases ***
Bulk collection test
    [Documentation]    Bulk loan collection test in zeus
    Collection write to excel
    Write to collection excel using loan details and response excel


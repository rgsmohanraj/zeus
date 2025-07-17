*** Settings ***
Resource    ../PageObjects/23__dailyAccurals.robot
Metadata    Partner   ${partner}
Metadata    Product   ${product}

*** Test Cases ***
Loan Daily Accurals Test
    [Documentation]   RS Loan daily accurals test
    Loan Daily Accurals 

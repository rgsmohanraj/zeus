*** Settings ***
Resource           ../keywords/common.robot

*** Variables ***
${feesName}=                    Fees
${chargeTimeType}=              Disbursement
${chargeCalculationType}=       Flat
${Amount}=                      1000

*** Keywords ***
Create New Charges
    [Documentation]    Create new charges in Zeus
    [Arguments]    ${feesName}    ${chargeTimeType}    ${chargeCalculationType}    ${Amount}
    Click Element     xpath:(//fa-icon[@class='ng-fa-icon mr-05'])[5]
    Sleep    2
    Click Element     xpath://button[text()='Products']
    Sleep    2
    Click Element     xpath://h4[text()='Charges']
    Sleep    4
    Click Element     xpath://button[@class='mat-focus-indicator mat-raised-button mat-button-base mat-primary ng-star-inserted']
    Sleep    4
    Click Element     xpath://mat-select[@formcontrolname='chargeAppliesTo']
    Sleep    2
    Click Element     xpath://span[text()=' Loan ']
    Sleep    2
    Click Element     xpath://mat-select[@formcontrolname='currencyCode']
    Sleep    2
    Click Element     xpath://span[text()=' Indian Rupee ']
    Sleep    2
    Input Text        xpath://input[@formcontrolname='name']     ${feesName}
    Sleep    2
    Click Element     xpath://mat-select[@formcontrolname='chargeTimeType']
    Sleep    2
    Click Element     xpath://span[text()=' ${chargeTimeType} ']
    Sleep    2
    Click Element     xpath://mat-select[@formcontrolname='chargeCalculationType']
    Sleep    2
    Click Element     xpath://span[text()=' ${chargeCalculationType} ']
    Sleep    2
    Click Element     xpath://mat-select[@formcontrolname='chargePaymentMode']
    Sleep    2
    Click Element     xpath://span[text()=' Regular ']
    Sleep    2
    Input Text        xpath://input[@formcontrolname='amount']    ${Amount}
    Sleep    2
    Click Element     xpath://mat-checkbox[@id='mat-checkbox-2']
    Sleep    2
   
Valid charges creation
    [Documentation]    verify valid charges creation
    Click Element    xpath://button[@class='mat-focus-indicator mat-raised-button mat-button-base mat-primary ng-star-inserted']
    Sleep    2
    Click Element    xpath://mat-select[@aria-label='Items per page:']
    Sleep    1
    Click Element    xpath://span[text()=' 100 ']
    Sleep    2
    Scroll Element Into View     xpath://td[text()=' ${feesName} ']
    Element Should Be Visible    xpath://td[text()=' ${feesName} ']
    Log     New Charges created in Zeus  
    Sleep    2

InValid charges creation
    [Documentation]    Invalid charges creation check
    Click Element     xpath://button[@class='mat-focus-indicator mat-raised-button mat-button-base mat-primary ng-star-inserted']
    Sleep    2
    Element Should Not Be Visible    xpath://td[text()=' ${feesName} ']
    Log     New Charges Not created in Zeus
    Sleep    2
    
Create charges and verify
    [Arguments]    ${feesName}    ${chargeTimeType}    ${chargeCalculationType}    ${Amount}
    Create New Charges    ${feesName}    ${chargeTimeType}    ${chargeCalculationType}    ${Amount}
    Valid charges creation
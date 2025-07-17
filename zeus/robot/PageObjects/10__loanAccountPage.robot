*** Settings ***
Resource           ../keywords/common.robot
Resource           ../PageObjects/02__clientPage.robot
Resource           ../PageObjects/06__productsPage.robot

*** Variables ***
${client}=                     ${getClient}
${clientName}=                   ABB kkkk
# ${DisbursementDate}=           20
# ${interestChargedFromDate}=    ${DisbursementDate}

*** Keywords ***
Create New Loan Account
    [Documentation]    Create New Loan Account for client in zeus
    Set Selenium Speed    0.4 seconds
    [Arguments]    ${client}    ${productName}   ${externalId}
    Click Element    xpath://a[@href='#/clients']
    # Sleep   2
    Click Element    xpath://mat-select[@aria-label='Items per page:']
    # Sleep    1
    Click Element    xpath://span[text()=' 100 ']
    Sleep    1
    Click Element    xpath://td[text()=' ${client} ']
    Sleep   2
    Click Element    xpath://span[contains(text(),'Applications')]
    # Sleep    1
    Click Element    xpath://button[contains(text(),'New Loan Account')]
    Sleep    3
    Wait Until Element Is Visible   xpath://mat-select[@formcontrolname='productId'] 
    Click Element    xpath://mat-select[@formcontrolname='productId']
    # Sleep    1
    Click Element    xpath://span[text()=' ${ProductName} ']
    # Sleep    1
    Click Element    xpath://input[@formcontrolname='expectedDisbursementDate']
    # Sleep    1

    ${current_date}    Get Current Date     result_format=%m/%d/%Y
    ${date_parts}    Split String    ${current_date}    /
    ${day}    Get From List    ${date_parts}    1
    ${DisbursementDate} =	Remove String	${day}	0	

    Click Element    xpath://div[text()=' ${DisbursementDate} ']
    # Sleep    1
    # ${external_id}    FakerLibrary.Numerify    text=##
    # Input Text    xpath://input[@formcontrolname='externalId']        ${externalId}
    # Sleep    1
    Click Element    xpath:(//span[contains(text(),'Next')])[1]
    # Sleep    3
    Click Element    xpath://input[@formcontrolname='repaymentsStartingFromDate']
    # Sleep    1
    Click Element    xpath://button[@aria-label='Next month']
    # Sleep    1
    Click Element    xpath://div[text()=' 5 ']   
    # Sleep    3
    Click Element    xpath://mat-select[@formcontrolname='repaymentFrequencyNthDayType']
    # Sleep    1
    Click Element    xpath://span[text()=' 5 ']
    # Sleep    1
    Click Element    xpath://input[@formcontrolname='interestChargedFromDate']
    # Sleep    1
    ${interestChargedFromDate}=    Set Variable      ${DisbursementDate}  
    # Sleep    1
    Click Element    xpath://div[text()=' ${interestChargedFromDate} ']
    # Sleep    1
    Click Element    xpath:(//span[contains(text(),'Next')])[2]
    # Sleep    3

    Click Element    xpath:(//mat-select[@role='combobox'])[10]
    # Sleep    1
    Click Element    xpath://span[text()=' ServiceFee ']
    # Sleep    1
    Click Element    xpath:(//span[contains(text(),'Add ')])[1]
    # Sleep    1
    Click Element    xpath:(//span[contains(text(),'Next')])[3]
    # Sleep    3
    # Pause Execution

Valid loan account creation
    [Documentation]    verify valid loan account creation
    Set Selenium Speed    0.4 seconds
    Click Element    xpath://span[text()=' Submit ']
    # Sleep    4
    ${loanStatus}=   Run Keyword And Return Status   Wait Until Element Is Not Visible  xpath:(//span[text()='Create Loans Account'])[2]
    Run Keyword If    '${loanStatus}' == 'False'    Log loan account creation error message
    Wait Until Element Is Visible    xpath://h3[contains(text(),' Loan Product: ${ProductName}')]
    Capture Page Screenshot
    Log     Loan account created in Zeus
    # Sleep    3

    # Approve loan
    Click Element   xpath://span[text()=' Approve ']
    # Sleep   4
    Input Text      xpath://textarea[@formcontrolname='note']    Approve
    Capture Page Screenshot
    # Sleep    1
    # Pause Execution
    Click Element   xpath://span[text()='Submit']
    # Sleep    3

    # Disburse loan
    Click Element    xpath://span[text()=' Disburse ']
    # Sleep    3
    Click Element    xpath://mat-select[@formcontrolname='paymentTypeId']
    # Sleep    1
    Click Element    xpath://span[text()=' Money Transfer ']
    # Sleep    1
    Input Text       xpath://textarea[@formcontrolname='note']    Disburse
    Capture Page Screenshot
    # Sleep    1
    # Pause Execution
    Click Element    xpath://span[text()='Submit']
    # Sleep    3

    # Make repayment
    Click Element    xpath://span[text()=' Make Repayment ']
    # Sleep    2
    Click Element    xpath://mat-select[@formcontrolname='paymentTypeId']
    # Sleep    1
    Click Element    xpath://span[text()=' Money Transfer ']
    # Sleep    1
    Input Text       xpath://textarea[@formcontrolname='note']    repayment
    Capture Page Screenshot
    # Sleep    1
    # Pause Execution
    Click Element    xpath://span[text()='Submit']
    Sleep    2
    Scroll Element Into View    xpath://p[text()='Version ']
    Capture Page Screenshot
    # Sleep    3

Log loan account creation error message
    Set Selenium Speed    0.4 seconds
    ${loanCreationErrorMsg}=    Get Text    xpath://div[text()=' Unknown data integrity issue with resource. ']
    Log     ${loanCreationErrorMsg} .Unable to create loan account for client   level=ERROR
    Capture Page Screenshot

InValid loan account creation
    [Documentation]    Invalid loan account creation
    Set Selenium Speed    0.4 seconds
    Click Element    xpath://span[text()=' Submit ']
    # Sleep    2
    Element Should Be Visible          xpath:(//span[text()='Create Loans Account'])[2]
    Wait Until Element Is Not Visible  xpath://h3[contains(text(),' Loan Product: ${ProductName}')]
    Log     Loan account not created in Zeus 
    # Sleep    4

Create loan account and verify
    [Arguments]    ${client}    ${productName}   ${externalId}
    Create New Loan Account    ${client}    ${productName}   ${externalId}
    Valid loan account creation
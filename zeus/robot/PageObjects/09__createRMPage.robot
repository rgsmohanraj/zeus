*** Settings ***
Resource           ../keywords/common.robot

*** Variables ***
${RMFirstName}=    saranya
${RMLastName}=     Jay
${RMmobileNo}=     8100072728

*** Keywords ***
Create a Relationship Manager
    [Documentation]    Create a new RM in Zeus
    [Arguments]    ${RMFirstName}    ${RMLastName}    ${RMmobileNo}
    Click Element    xpath:(//fa-icon[@class='ng-fa-icon mr-05'])[5]
    Sleep    2
    Click Element    xpath://button[text()='Organization']
    Sleep    2
    Click Element    xpath://h4[text()='Relationship Managers']
    Sleep    2
    Click Element    xpath://span[contains(text(),'Create RM ')]
    Sleep    2
    Click Element    xpath://mat-select[@formcontrolname='officeId']
    Sleep    1
    Click Element    xpath://span[text()=' Head Office ']
    Sleep    1
    Input Text       xpath://input[@formcontrolname='firstname']    ${RMFirstName}
    Sleep    1
    Input Text       xpath://input[@formcontrolname='lastname']    ${RMLastName}
    Sleep    1
    Click Element    xpath://span[text()=' Is Loan Officer ']
    Sleep    1
    Input Text       xpath://input[@formcontrolname='mobileNo']    ${RMmobileNo}
    Sleep    1
    Click Element    xpath://button[@aria-label='Open calendar']
    Sleep    1
    Click Element    xpath://div[text()=' 1 ']
    Sleep   1
    Click Element    xpath://span[text()='Submit']
    Sleep    3
    Click Element    xpath://mat-select[@aria-label='Items per page:']
    Sleep    1
    Click Element    xpath://span[text()=' 100 ']
    Sleep    2
    Element Should Be Visible    xpath://td[text()=' ${RMLastName}, ${RMFirstName} ']
    Log     RM onboarded into Zeus
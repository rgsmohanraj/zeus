*** Settings ***
Resource           ../keywords/common.robot

*** Variables ***
${currencyName}=    Argentine Peso

*** Keywords ***
Create a New Currency
    [Documentation]    Currency configuration in Zeus
    Set Selenium Speed    0.4 seconds
    Click Element    xpath:(//fa-icon[@class='ng-fa-icon mr-05'])[5]
    Sleep    2
    Click Element    xpath://button[text()='Organization']
    Sleep    2
    Click Element    xpath://h4[text()='Currency Configuration']
    Sleep    2
    Click Element    xpath://span[contains(text(),' Add/Edit ')]
    Sleep    2
    Click Element    xpath://mat-select[@formcontrolname='currency']
    Sleep    2
    Click Element    xpath://span[text()=' ${currencyName} ']
    Sleep    2
    Click Element    xpath://button[@color='primary']
    Sleep    1
    Log To Console    Currency configuration completed
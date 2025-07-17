*** Settings ***
Resource           ../keywords/common.robot

*** Variables ***
&{userName}=      QA_User=admin      UAT_User=admin
&{password}=      QA_PW=password     UAT_PW=password     

*** Keywords ***
Open Zeus in browser
    [Documentation]    Open Zeus in browser
    Set Selenium Speed    0.2 seconds
    Open Browser  ${url.QA}    ${browserName} 
    # Maximize Browser Window
    # Wait Until Page Contains Element  xpath://input[@formcontrolname='username']  timeout=10s
    Title Should Be    Login | Zeus
    Log Title

login to Zeus
    [Documentation]    login to Zeus
    Set Selenium Speed    0.2 seconds
    [Arguments]      ${userName}        ${password}
    Input Text       xpath://input[@formcontrolname='username']    ${userName.QA_User}
    Input Text       xpath://input[@formcontrolname='password']    ${password.QA_PW}
    Wait Until Page Contains Element   xpath://span[contains(text(),'Login')]   timeout=30s
    Click Element    xpath://span[contains(text(),'Login')]

Verify Correct Login
    [Documentation]    Verify the correct user login
    Set Selenium Speed    0.2 seconds
    Wait Until Element Is Visible        xpath://a[@href='#/home']
    Wait Until Element Is Not Visible    xpath=//mat-error[contains(text(),' Username is ')]
    Log     Login to Zeus

Incorrect Login Test Teardown
    [Documentation]    Incorrect user login
    Set Selenium Speed    0.4 seconds
    Wait Until Element Is Not Visible   xpath://a[@href='#/home']
    Wait Until Element Is Visible       xpath=//mat-error[contains(text(),' Username is ')]
    Log    Login Negative test Passed
 
Open Browser and login to Zeus
    [Arguments]        ${userName}        ${password}
    Open Zeus in browser
    login to Zeus      ${userName}        ${password}
    Verify Correct Login
    
Invalid login check in Zeus
    [Arguments]        ${userName}        ${password}
    Set Selenium Speed    0.4 seconds
    Open Zeus in browser
    login to Zeus      ${userName}        ${password}
    Incorrect Login Test Teardown
*** Settings ***
Resource           ../keywords/common.robot

*** Variables ***
# ${User}=               Ganesh
# ${User_mail}=          gan@gmail.com
# ${User_first_name}=    Ganesan
# ${User_last_name}=     a
${User_password}=      12345qwerty
${User_Repassword}=    12345qwerty

*** Keywords ***
Create New User
    [Documentation]    Create a new user in Zeus
    Set Selenium Speed    0.4 seconds
    [Arguments]   ${User}    ${User_mail}    ${User_first_name}    ${User_last_name}    ${User_password}    ${User_Repassword}
    ${def_wait}=     Get Selenium Implicit Wait
    Set Selenium Implicit Wait    5s
    ${Custom wait}=    Get Selenium Implicit Wait
    Set Selenium Implicit Wait    ${Custom wait}

    Click Element    xpath:(//fa-icon[@class='ng-fa-icon mr-05'])[5]
    # Sleep    2
    Click Element    xpath://button[text()='Users']
    # Sleep    2
    Click Element    xpath://span[contains(text(),'Create User')]
    # Sleep    2
    # Get Variable Value     ${User}      FakerLibrary.User Name
    Input Text       xpath://input[@formcontrolname='username']    ${User}
    # Sleep    1
    ${getUser}=    Get Element Attribute     xpath://input[@formcontrolname='username']    value
    Set Global Variable    ${getUser}
    # Sleep    1
    # Set Local Variable         ${User_mail}    FakerLibrary.Email
    Input Text       xpath://input[@formcontrolname='email']    ${User_mail}
    # Sleep    2
    # ${User_first_name}    FakerLibrary.First Name
    Input Text       xpath://input[@formcontrolname='firstname']    ${User_first_name}
    # Sleep    2
    # ${User_last_name}    FakerLibrary.Last Name
    Input Text       xpath://input[@formcontrolname='lastname']    ${User_last_name}
    # Sleep    2
    Click Element    xpath://span[text()=' Auto generate password ']
    # Sleep    2
    Input Text       xpath://input[@formcontrolname='password']    ${User_password}
    # Sleep    2
    Input Text       xpath://input[@formcontrolname='repeatPassword']    ${User_Repassword}
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='officeId']
    # Sleep    1
    Click Element    xpath://span[contains(text(),' Head Office ')]
    Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='staffId']
    Sleep    1
    Click Element    xpath://span[contains(text(),'Thameem')]
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='roles']
    # Sleep    1
    Click Element    xpath://span[text()=' Super user ']
    # Sleep    2
    Double Click Element    xpath://span[text()='Submit']
    # Sleep    2
   
Verify Valid User Onboarding
    [Documentation]    Verify Valid Partner Onboarding
    Set Selenium Speed    0.4 seconds
    Wait Until Element Is Visible    xpath://div[text()=' ${getUser} ']
    Log     User onboarded into Zeus

Log user error message
    Set Selenium Speed    0.4 seconds
    ${userErrorMsg}=    Get Text    xpath://div[contains(text(),' Client with externalId ')]
    Log     ${userErrorMsg} .Unable to onboard user   level=ERROR

Invalid User Onboarding
    [Documentation]    Invalid Partner Onboarding
    Set Selenium Speed    0.4 seconds
    Wait Until Element Is Not Visible    xpath://div[text()=' ${getUser} ']
    # Sleep    1
    Element Should Not Be Visible        xpath://span[contains(text(),' Edit ')]
    Capture Page Screenshot
    Log     User Not onboarded into Zeus

Create New User and verify
    [Arguments]   ${User}    ${User_mail}    ${User_first_name}    ${User_last_name}    ${User_password}    ${User_Repassword}
    Set Selenium Speed    0.2 seconds
    Create New User    ${User}    ${User_mail}    ${User_first_name}    ${User_last_name}    ${User_password}    ${User_Repassword}
    Verify valid User Onboarding
    Capture Page Screenshot
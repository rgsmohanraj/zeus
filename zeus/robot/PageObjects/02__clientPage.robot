*** Settings ***
Resource           ../keywords/common.robot

*** Keywords ***
Create New Client
    [Documentation]    Create a new client in zeus
    Set Selenium Speed    0.4 seconds
    [Arguments]    ${first_name}    ${last_name}    ${external_id}    ${mobile_num}    ${mail_id}
    Set Selenium Implicit Wait     5s
    Click Element    xpath://a[@href='#/clients']
    # Sleep    2    
    Click Element    xpath:/html/body/mifosx-web-app/mifosx-shell/mat-sidenav-container/mat-sidenav-content/mifosx-content/mifosx-clients/mat-card/div[1]/div[2]/button[2]
    # Sleep    3
    Click Element   xpath://mat-select[@formcontrolname='officeId']
    # Sleep    1
    Click Element    xpath://span[contains(text(),'Head Office')]
    # Sleep    1
    Double Click Element    xpath://span[text()='PERSON']
    # Sleep    2
    # ${first_name}    FakerLibrary.First Name
    Input Text    xpath://input[@formcontrolname='firstname']    ${first_name}
    # Sleep    2
    ${getClientFirstName}=    Get Element Attribute    xpath://input[@formcontrolname='firstname']    value
    Set Test Variable    ${getClientFirstName}  
    # ${last_name}    FakerLibrary.Last Name
    Input Text       xpath://input[@formcontrolname='lastname']    ${last_name}
    # Sleep    2
    ${getClientLastName}=      Get Element Attribute    xpath://input[@formcontrolname='lastname']     value
    Set Test Variable    ${getClientLastName}  
    Click Element    xpath:(//button[@aria-label='Open calendar'])[1]
    # Sleep    2
    Click Element    xpath://div[@class='mat-calendar-arrow']
    # Sleep    1
    Click Element    xpath:(//div[contains(text(),'2000')])[2]
    # Sleep    1
    Click Element    xpath://div[contains(text(),'JAN')]
    # Sleep    1
    Click Element    xpath://div[text()=' 1 ']
    # Sleep    2
    # ${external_id}    FakerLibrary.Numerify    text=##
    # Input Text       xpath://input[@formcontrolname='externalId']    ${external_id}
    Sleep    1
    # ${mobile_num}    FakerLibrary.Phone Number
    Input Text       xpath://input[@type='number']    ${mobile_num}
    # Sleep    2
    # ${mail_id}    FakerLibrary.Email
    Input Text       xpath://input[@formcontrolname='emailAddress']    ${mail_id}
    # Sleep    2
    Click Element    xpath:(//button[@aria-label='Open calendar'])[2]
    # Sleep    2
    Click Element    xpath://div[text()=' 1 ']
    # Sleep    1
    Click Element    xpath://span[@class='mat-checkbox-inner-container']
    # Sleep    3
    Click Element    xpath:(//button[@aria-label='Open calendar'])[3]
    # Sleep    2
    Click Element    xpath://div[text()=' 1 ']
    # Sleep    2
   
Verify Valid Client Onboarding
    [Documentation]    Verify correct client onboarding
    Set Selenium Speed    0.5 seconds
    [Arguments]    ${first_name}    ${last_name}
    Click Element  xpath:( //button[@type='submit'])[1]
    Sleep    2
    # Pause Execution
    Click Element    xpath:(//*[contains(text(),'Submit')])[3]
    Sleep    2
    ${clientStatus}=   Run Keyword And Return Status   Element Should Be Visible  xpath://div[contains(text(),' Client with externalId ')]
    Run Keyword If    '${clientStatus}' == 'True'    Log client error message
    Element Should Be Visible    xpath://h3[text()=' Client Name : ${first_name} ${last_name} ']
    Sleep    1
    Log     Client onboarded into Zeus

Log client error message
    Set Selenium Speed    0.4 seconds
    ${clientErrorMsg}=    Get Text    xpath://div[contains(text(),' Client with externalId ')]
    Log     ${clientErrorMsg} .Unable to onboard client   level=ERROR
    Capture Page Screenshot

InValid Client Onboarding    
    [Documentation]    Invalid client onboarding
    Set Selenium Speed    0.4 seconds
    Click Element    xpath:( //button[@type='submit'])[1]
    ${clientStatus1}=   Run Keyword And Return Status   Wait Until Element Is Not Visible       xpath:(//*[contains(text(),'Submit')])[3]
    Run Keyword If    '${clientStatus1}' == 'True'   Log    Unable to goto next page in client  level=ERROR    ELSE    Next page in client
    Sleep    2

Next page in client
    Set Selenium Speed    0.4 seconds
    Click Element    xpath:(//*[contains(text(),'Submit')])[3]
    Sleep    3
    Element Should Not Be Visible    xpath://span[text()='Edit']
    Sleep    1
    Log     Client Not onboarded into Zeus  

Create New Client and verify
    [Arguments]    ${first_name}    ${last_name}    ${external_id}    ${mobile_num}    ${mail_id}
    Set Selenium Speed    0.2 seconds
    Create New Client    ${first_name}    ${last_name}    ${external_id}    ${mobile_num}    ${mail_id}
    Verify Valid Client Onboarding     ${first_name}    ${last_name}
    Capture Page Screenshot
    ${getClient}=     Catenate   ${getClientFirstName}    ${getClientLastName}
    Set Suite Variable    ${getClient}


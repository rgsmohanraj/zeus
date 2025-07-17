*** Settings ***
Resource           ../keywords/common.robot

*** Keywords ***
Run DPD Scheduler
    [Documentation]    Run the DPD scheduler
    Set Selenium Speed    0.3s
    Set Window Size    1920    1080
    Click Element    xpath://*[@data-icon='shield-alt']
    Click Element    xpath://button[text()='System']
    Click Element    xpath://h4[text()='Scheduler Jobs']
    Click Element    xpath:(//mat-checkbox[@class='mat-checkbox mat-accent'])[8]
    Click Element    xpath://fa-icon[@icon='play']
    Sleep   10s
    Click Element    xpath:(//fa-icon[@icon='sync'])[2]
    Sleep   1s
    Click Element    xpath:(//mat-checkbox[@class='mat-checkbox mat-accent'])[6]
    Click Element    xpath://fa-icon[@icon='play']
    Sleep   10s
    Click Element    xpath:(//fa-icon[@icon='sync'])[2]
    Log  DPD and Accural scheduler run successfully


    

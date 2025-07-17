*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot    
Resource           ../PageObjects/04__partnerPage.robot

*** Test Cases ***
TC_PA_01_Test Create New Partner in Zeus
    [Documentation]    Create a new Partner in Zeus
    01__loginPage.Open Browser and login to Zeus    ${userName}   ${password}
    04__partnerPage.Create New Partner
    Log     Partner Testcase
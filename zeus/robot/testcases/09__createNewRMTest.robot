*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/09__createRMPage.robot

*** Test Cases ***
TC_RM_01_Test Create New RM in Zeus
    [Documentation]    Create a new RM in Zeus
    01__loginPage.Open Browser and login to Zeus    ${userName}     ${password}
    09__createRMPage.Create a Relationship Manager  ${RMFirstName}  ${RMLastName}    ${RMmobileNo}
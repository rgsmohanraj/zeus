*** Settings ***
Documentation     Login test for Zeus
Metadata    Version    ZEUS 1.0
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Suite Setup        Open Zeus in browser
Suite Teardown     Verify Correct Login

*** Test Cases ***
TC_LI_01_Test login zeus
    [Documentation]    Open Zeus in chrome
    01__loginPage.login to Zeus    ${userName}        ${password}






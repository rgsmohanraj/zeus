*** Settings ***
Resource    ../keywords/common.robot
Resource    ../PageObjects/01__loginPage.robot
Resource    ../PageObjects/12__schedulerPage.robot

*** Test Cases ***
Run DPD scheduler test
   [Documentation]    Run the zeus DPD scheduler
   01__loginPage.Open Browser and login to Zeus     ${userName}        ${password}
   Run DPD Scheduler

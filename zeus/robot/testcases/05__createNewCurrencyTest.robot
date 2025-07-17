*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/05__currencyConfigPage.robot

*** Test Cases ***
TC_CC_01_Test Select new currecy
    [Documentation]    Create a new Currency in Zeus
    01__loginPage.Open Browser and login to Zeus    ${userName}   ${password}
    05__currencyConfigPage.Create a New Currency
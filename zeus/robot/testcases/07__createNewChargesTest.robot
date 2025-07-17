*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/07__chargesPage.robot

*** Test Cases ***
TC_CH_01_Test Create New Charges
    [Documentation]    Create a new Charges in Zeus
    01__loginPage.Open Browser and login to Zeus    ${userName}   ${password} 
    07__chargesPage.Create charges and verify       ${feesName}   ${chargeTimeType}    ${chargeCalculationType}    ${Amount}
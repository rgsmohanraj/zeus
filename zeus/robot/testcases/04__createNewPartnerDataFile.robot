*** Settings *** 
Resource           ../keywords/common.robot    
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/04__partnerPage.robot
Test Template      Create New Partner
Test Teardown      InValid Partner Onboarding
Suite Setup        Open Browser and login to Zeus    ${userName}    ${password}

*** Test Cases ***                  partnerName        partnerLimit    externalId
TC_PA_02_Missing Partner name          ${EMPTY}             1000000         94
# TC_PA_03_Invalid Partner name        Si0@               1000000         101
# TC_PA_04_Invalid Partner limit       Mint               1(*?000         103
# TC_PA_05_Invalid External id         ITC                5000000         10$
# TC_PA_06_Missing External id         Lint               100000          ${EMPTY}

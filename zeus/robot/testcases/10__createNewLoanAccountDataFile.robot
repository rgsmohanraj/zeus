*** Settings ***
Resource            ../keywords/common.robot
Resource            ../PageObjects/01__loginPage.robot
Resource            ../PageObjects/10__loanAccountPage.robot
Test Template       Create New Loan Account
Test Teardown       InValid loan account creation
Suite Setup         Open Browser and login to Zeus    ${userName}    ${password}


*** Test Cases ***          client        productName        externalId
TC_LA_02_Create Loan       VAM Tec       Vehicle Loan          30
TC_LA_03_Create Loan       VAM Tec       Agriculture loan      31
# TC_LA_04_Create Loan       VAM Tec        Edu loan             32
# TC_LA_05_Create Loan       VAM Tec        Krazy Bee Loan       33
# TC_LA_06_Create Loan       VAM Tec        House Loan           34
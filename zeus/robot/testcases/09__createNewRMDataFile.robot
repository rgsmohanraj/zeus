*** Settings *** 
Resource           ../keywords/common.robot    
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/09__createRMPage.robot
Test Template      Create a Relationship Manager
Suite Setup        Open Browser and login to Zeus    ${userName}    ${password}

*** Test Cases ***             RMFirstName    RMLastName    RMmobileNo
TC_RM_02_Missing FirstName      ${EMPTY}        kedia        9839383383
# TC_RM_03_Invalid RMFirstName    Veer@           ragav        9139383383
# TC_RM_04_Invalid RMLastName     Lal             ku!@?        9779383383
# TC_RM_05_Invalid RMmobileNo     Vijay           kumar        98393833!#$
# TC_RM_06_Missing RMmobileNo     Bala            Mani         ${EMPTY}
# TC_RM_07_Invalid RMmobileNo     Saranya         Jayapal      98399


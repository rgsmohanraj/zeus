*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/10__loanAccountPage.robot

*** Test Cases ***
TC_LA_01_Test Create New Loan Account for Client
    [Documentation]    Create a new Loan Account in Zeus
    01__loginPage.Open Browser and login to Zeus          ${userName}    ${password}
    ${externalId}=        FakerLibrary.Numerify    text=## 
    ${ProductName}=     Set Variable   Amrit Loan
    10__loanAccountPage.Create loan account and verify    ${clientName}    ${productName}   ${externalId}
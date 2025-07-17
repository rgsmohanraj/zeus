*** Settings ***   
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/06__productsPage.robot

*** Test Cases ***
TC_PR_01_Test Create New Product in Zeus
    [Documentation]    Create a new Products in Zeus
    Set Selenium Speed    0.4 seconds
    01__loginPage.Open Browser and login to Zeus     ${userName}     ${password}
    ${partner}=     Set Variable   RMRL
    06__productsPage.Create new product and verify   ${ProductName}  ${shortName}  ${desc}  ${decDigit}  ${minPrincipal}  ${defPrincipal}  ${maxPrincipal}  ${minNOP}  ${defNOP}  ${maxNOP}  ${minInterest}  ${defInterest}  ${maxInterest}  ${repayEvery}  ${minDay}  ${Amortization}  ${interestMethod}  ${interestCalPeriod}  ${brokenInterestStrategy}  ${daysInYearType}  ${daysInMonthType}  ${selfPrincipalShare}  ${selfFeesShare}  ${selfPenaltyShare}  ${selfOverpaidShare}  ${clientInterest}  ${selfInterest}  ${partnerPrincipalShare}  ${partnerFeesShare}  ${partnerPenaltyShare}  ${partnerOverpaidShare}  ${partnerInterest}    ${partner}
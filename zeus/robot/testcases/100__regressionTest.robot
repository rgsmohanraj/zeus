*** Settings ***   
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/04__partnerPage.robot
Resource           ../PageObjects/06__productsPage.robot
Resource           ../PageObjects/02__clientPage.robot
Resource           ../PageObjects/10__loanAccountPage.robot
Resource           ../PageObjects/03__createUserPage.robot

*** Test Cases ***
End to End Test case
    [Documentation]    Regression test for Zeus
    Set Suite Metadata     version:Zeus 1.0    description:This is a Regression suite
    01__loginPage.Open Browser and login to Zeus        ${userName}        ${password}
    ${User}              FakerLibrary.User Name
    ${User_mail}         FakerLibrary.Email
    ${User_first_name}   FakerLibrary.First Name
    ${User_last_name}    FakerLibrary.Last Name
    03__createUserPage.Create New User and verify       ${User}    ${User_mail}    ${User_first_name}    ${User_last_name}    ${User_password}    ${User_Repassword}
    ${partnerName}=      FakerLibrary.first_name
    # ${partnerLimit}=    800000
    ${externalId}=      Get Variable Value    ${exId}
    04__partnerPage.Create new partner and verify       ${partnerName}    ${partnerLimit}    ${externalId}
    06__productsPage.Create new product and verify      ${ProductName}  ${shortName}  ${desc}  ${decDigit}  ${minPrincipal}  ${defPrincipal}  ${maxPrincipal}  ${minNOP}  ${defNOP}  ${maxNOP}  ${minInterest}  ${defInterest}  ${maxInterest}  ${repayEvery}  ${minDay}  ${Amortization}  ${interestMethod}  ${interestCalPeriod}  ${brokenInterestStrategy}  ${daysInYearType}  ${daysInMonthType}  ${selfPrincipalShare}  ${selfFeesShare}  ${selfPenaltyShare}  ${selfOverpaidShare}  ${clientInterest}  ${selfInterest}  ${partnerPrincipalShare}  ${partnerFeesShare}  ${partnerPenaltyShare}  ${partnerOverpaidShare}  ${partnerInterest}    ${getPartner}    
    ${first_name}=        FakerLibrary.First Name
    ${last_name}=         FakerLibrary.Last Name
    ${mobile_num}=        FakerLibrary.Phone Number
    ${mail_id}=           FakerLibrary.Email
    02__clientPage.Create New Client and verify          ${first_name}    ${last_name}    ${external_id}    ${mobile_num}    ${mail_id}
    10__loanAccountPage.Create loan account and verify   ${getClient}    ${productName}   ${externalId}
    Log To Console    Regression Test Completed


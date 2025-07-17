*** Settings ***   
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/03__createUserPage.robot
Test Teardown      Verify Valid User Onboarding

*** Variables ***
${User_password}=      12345qwerty
${User_Repassword}=    12345qwerty

*** Test Cases ***
TC_US_01_Test Create New User in Zeus
    [Documentation]    Create a new User in Zeus
    01__loginPage.Open Browser and login to Zeus    ${userName}        ${password}
    ${User}               FakerLibrary.User Name
    ${User_mail}          FakerLibrary.Email
    ${User_first_name}    FakerLibrary.First Name
    ${User_last_name}     FakerLibrary.Last Name
    03__createUserPage.Create New User and verify   ${User}    ${User_mail}    ${User_first_name}    ${User_last_name}    ${User_password}    ${User_Repassword}
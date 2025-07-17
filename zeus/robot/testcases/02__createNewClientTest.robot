*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/02__clientPage.robot

*** Test Cases ***
TC_CL_01_Test Create New Client in Zeus
    [Documentation]    Create a new client in Zeus
    01__loginPage.Open Browser and login to Zeus    ${userName}        ${password}
    ${first_name}=        FakerLibrary.First Name
    ${last_name}=         FakerLibrary.Last Name
    ${external_id}=       FakerLibrary.Numerify    text=##
    ${mobile_num}=        FakerLibrary.Phone Number
    ${mail_id}=           FakerLibrary.Email
    02__clientPage.Create New Client and verify    ${first_name}    ${last_name}    ${external_id}    ${mobile_num}    ${mail_id}
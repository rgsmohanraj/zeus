*** Settings *** 
Resource    ../PageObjects/26__bulkLoanTemplateCheck.robot
Resource    ../PageObjects/11__bulkLoanUploadApi.robot

*** Test Cases ***
Bulk Loan template test based on partner and Product
   [Documentation]  Check bulk loan excel template 
   Open Browser and login to Zeus    ${userName}    ${password}
   11__bulkLoanUploadApi.Download loan temp
   Bulk Loan template test
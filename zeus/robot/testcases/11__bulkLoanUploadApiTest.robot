*** Settings ***
Resource    ../keywords/common.robot
Resource    ../PageObjects/11__bulkLoanUploadApi.robot
Resource    ../PageObjects/26__bulkLoanTemplateCheck.robot
Suite Setup    Partner,Product and loan count details

*** Test Cases ***
Test Bulk loan creation excel in Zeus
  [Documentation]   Bulk loan creation test in zeus
  # Set Selenium Speed   0.3s
  Open Browser and login to Zeus     ${userName}        ${password}
  Bulk Loan Download using API
  # 11__bulkLoanUploadApi.Download loan temp
  26__bulkLoanTemplateCheck.Bulk Loan template test
  11__bulkLoanUploadApi.Create a Bulk loan using product
  11__bulkLoanUploadApi.Bulk loan date insert to excel
  # 11__bulkLoanUploadApi.Upload the created excel loan
  Log  Bulk loan successfully created in Zeus

# Test Bulk loan Upload excel in zeus
#   [Documentation]   Bulk loan creation and upload test in zeus
#   11__bulkLoanUploadApi.Upload the created excel loan
#   Log  Bulk loan successfully created and uploaded in Zeus
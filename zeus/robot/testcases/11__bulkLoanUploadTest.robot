*** Settings ***
Resource    ../keywords/common.robot
Resource    ../PageObjects/11__bulkLoanUpload.robot
Library     ../PageObjects/11__testDateXl.py

*** Test Cases ***
Test Bulk loan creation excel in Zeus
  [Documentation]   Bulk loan creation test in zeus
  Set Selenium Speed   0.3s
  Open Browser and login to Zeus     ${userName}        ${password}
  Download loan temp
  Create a Bulk loan
  Log  Bulk loan successfully created in Zeus

Test Bulk loan Upload excel in zeus
  [Documentation]   Bulk loan creation and upload test in zeus
  Date Creation Xl
  # Upload the created excel loan
  Log  Bulk loan successfully created and uploaded in Zeus


  
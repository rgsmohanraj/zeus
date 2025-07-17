*** Settings ***
Resource    ../keywords/common.robot
Resource    ../PageObjects/01__loginPage.robot
Resource    ../PageObjects/17__gstReport.robot
Suite Setup    Partner,Product and loan count details

*** Test Cases ***
GST report test
  [Documentation]  Check in GST report values with bulk loan excel values and API
  # Open Browser and login to Zeus     ${userName}        ${password}
  # 17__gstReport.Download GST report
  Download GST report using API
  17__gstReport.Check in GST report
   
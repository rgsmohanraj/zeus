*** Settings ***
Resource    ../keywords/common.robot
Resource    ../PageObjects/01__loginPage.robot
Resource    ../PageObjects/28__posReport.robot
Suite Setup    Partner,Product and loan count details

*** Test Cases ***
POS report test
  [Documentation]  Check in Bureau reporiot values with bulk loan excel values and API
  # Open Browser and login to Zeus     ${userName}   ${password}
  28__posReport.Check in POS report
    
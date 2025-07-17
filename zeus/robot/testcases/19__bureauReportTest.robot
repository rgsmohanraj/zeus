*** Settings ***
Resource    ../keywords/common.robot
Resource    ../PageObjects/01__loginPage.robot
Resource    ../PageObjects/19__bureauReport.robot
Suite Setup    Partner,Product and loan count details

*** Test Cases ***
Bureau report test
  [Documentation]  Check in Bureau report values with bulk loan excel values and API
  Open Browser and login to Zeus     ${userName}   ${password}
  19__bureauReport.Check in Bureau report

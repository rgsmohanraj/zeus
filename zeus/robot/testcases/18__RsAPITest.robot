*** Settings ***
Resource       ../PageObjects/18__RsAPI.robot
Suite Setup    Partner,Product and loan count details

*** Test Cases ***
Repayment schedule test
  [Documentation]  Test Repayment schedule based on load id
  18__RsAPI.RS ALL Test
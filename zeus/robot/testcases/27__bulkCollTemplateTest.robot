*** Settings *** 
Resource    ../PageObjects/27__bulkCollTemplateCheck.robot

*** Test Cases ***
Bulk Loan template test based on partner and Product
   [Documentation]  Check bulk loan excel template 
   Open Browser and login to Zeus    ${userName}    ${password}
   Download Bulk collection template
   Bulk collection template test

test
    ${a}    Set Variable    53.3898
    ${b}    Set Variable    2119
    ${c1}    Evaluate    ${a}*9/100
    ${c2}    Evaluate     ${b}*9/100
    ${d1}    Evaluate    round(${a},${2})
    ${d2}    Evaluate    round(${b}*9/100,${0})
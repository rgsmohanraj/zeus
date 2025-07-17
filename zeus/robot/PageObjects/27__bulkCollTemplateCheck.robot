*** Settings ***
Resource    ../keywords/common.robot
Resource    ../testcases/11__getExcelTemp.robot

*** Keywords ***
Download Bulk collection template
  [Documentation]   Download bulk collection template
  Set Selenium Speed  0.3s
  Sleep    1s
  Click Element    xpath://a[@href='#/organization/bulk-import']
  Click Element    xpath://h4[text()='Bulk Collections ']
  Sleep    1s
  Click Element    xpath://i[@class='fa fa-download']
  Sleep    3s
  log  Bulk collection template downloaded

Bulk collection template test
  [Documentation]    Check the Bulk collection excel template file
  ${bulkCollFile}    11__getRecentFile.Get Most Recent Excel File
  Open Workbook   ${bulkCollFile}
  ${SnoXl}             Read From Cell    A${1}
  Should Be Equal      ${SnoXl}          S.NO*
  ${loanAccNoXl}       Read From Cell    B${1}
  Should Be Equal      ${loanAccNoXl}    Loan Account No*
  ${exIdXl}            Read From Cell    C${1}
  Should Be Equal      ${exIdXl}         External Id*
  ${installmentXl}     Read From Cell    D${1}
  Should Be Equal      ${installmentXl}  Installment
  ${transAmountXl}     Read From Cell    E${1}
  Should Be Equal      ${transAmountXl}  Transaction Amount*
  ${transDateXl}       Read From Cell    F${1}
  Should Be Equal      ${transDateXl}    Transaction Date*
  ${reciptRefXl}       Read From Cell    G${1}
  Should Be Equal      ${reciptRefXl}    Receipt Reference Number
  ${partnerUTRXl}      Read From Cell    H${1}
  Should Be Equal      ${partnerUTRXl}   Partner Transfer UTR
  ${parTrasnsDateXl}   Read From Cell    I${1}
  Should Be Equal      ${parTrasnsDateXl}  Partner Transfer Date
  ${repayModeXl}       Read From Cell    J${1}
  Should Be Equal      ${repayModeXl}    Repayment Mode
  Log   bulk Collection excel template checked successfully
*** Settings ***   
Metadata    LoanCount    ${loanCount}
Resource    ../keywords/common.robot
Resource    ../PageObjects/11__bulkLoanUploadApi.robot
Resource    ../PageObjects/26__bulkLoanTemplateCheck.robot
Resource    ../PageObjects/12__schedulerPage.robot
Resource    ../PageObjects/24__mysqlConnect.robot
Resource    ../PageObjects/23__dailyAccurals.robot
Resource    ../PageObjects/15__disbursementReport.robot
Resource    ../PageObjects/17__gstReport.robot
Resource    ../PageObjects/19__bureauReport.robot
Resource    ../PageObjects/18__RsAPI.robot

*** Keywords ***
Regression bulk loan creation and report Check
  11__bulkLoanUploadApi.Bulk Loan Download using API
  26__bulkLoanTemplateCheck.Bulk Loan template test
  11__bulkLoanUploadApi.Create a Bulk loan using product
  11__bulkLoanUploadApi.Bulk loan date insert to excel
  11__bulkLoanUploadApi.Upload the created excel loan
  Log  Bulk loan successfully created in Zeus
  12__schedulerPage.Run DPD Scheduler
  24__mysqlConnect.Take Loan Id from Mysql DB
  23__dailyAccurals.Loan Daily Accurals
  15__disbursementReport.Download Disbursement report using API
  15__disbursementReport.Check in disbursement report
  17__gstReport.Download GST report using API
  17__gstReport.Check in GST report
  # 19__bureauReport.Download Bureau report using API
  # 19__bureauReport.Check in Bureau report
  18__RsAPI.RS ALL Test

*** Test Cases ***
Regression bulk loan creation test
  [Documentation]    Regression bulk loan creation test for Zeus
  Set Suite Metadata     version:Zeus 1.0    description:This is a Regression suite
  Open Browser and login to Zeus     ${userName}   ${password}
  Set Window Size    1920    1080
  ${partner}    Set Variable    NOCPL
  ${product}    Set Variable    NOCPL
  Set Global Variable    ${partner}    
  Set Global Variable    ${product}
  Regression bulk loan creation and report Check
  # ${partner}    Set Variable    Dvara
  # ${product}    Set Variable    Dvara
  # Set Global Variable    ${partner}    
  # Set Global Variable    ${product}
  # Regression bulk loan creation and report Check
  ${partner}    Set Variable    Navdhan
  ${product}    Set Variable    Navdhan
  Set Global Variable    ${partner}
  Set Global Variable    ${product}
  Regression bulk loan creation and report Check
  ${partner}    Set Variable    Seeds
  ${product}    Set Variable    Seeds
  Set Global Variable    ${partner}
  Set Global Variable    ${product}
  Regression bulk loan creation and report Check
  
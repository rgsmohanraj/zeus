*** Settings ***   
Resource      ../keywords/common.robot
Resource      ../PageObjects/11__bulkLoanUploadApi.robot
Resource      ../PageObjects/15__disbursementReport.robot
Resource      ../PageObjects/17__gstReport.robot
Resource      ../PageObjects/18__RsAPI.robot
Resource      ../PageObjects/19__bureauReport.robot

*** Test Cases ***
Test Bulk loan creation
    [Documentation]    Bulk loan template download and create
    Open Browser and login to Zeus     ${userName}        ${password}
    11__bulkLoanUploadApi.Download loan temp
    11__bulkLoanUploadApi.Create a Bulk loan using product
    11__bulkLoanUploadApi.Bulk loan date insert to excel
    11__bulkLoanUploadApi.Upload the created excel loan
    Log  Bulk loan successfully created and uploaded in Zeus
    
Test Bulk loan upload
    [Documentation]    Bulk loan date creation and upload
    11__bulkLoanUpload.Upload the created excel loan
    Log  Bulk loan successfully created and uploaded in Zeus

Bulk loan disbursement report test
    [Documentation]    Test Bulk loan disbursement report
    Open Browser and login to Zeus     ${userName}        ${password}
    15__disbursementReport.Download Disbursement report
    15__disbursementReport.Check in disbursement report
    Log   disbursement report successfully tested with bulk loan upload file

Bulk loan GST report test
    [Documentation]    Test Bulk loan GST report
    Open Browser and login to Zeus     ${userName}        ${password}
    17__gstReport.Download GST report
    17__gstReport.Check in GST report
    Log   GST report successfully tested with bulk loan upload file

Bulk loan Bureau report test
    [Documentation]    Test Bulk loan Bureau report
    Open Browser and login to Zeus     ${userName}        ${password}
    19__bureauReport.Download Bureau report
    19__bureauReport.Check in Bureau report
    Log    Bureau report successfully tested with bulk loan upload file

Bulk loan RS API test
    [Documentation]    Test Bulk loan RS API
    18__RsAPI.RS ALL Test
*** Settings ***
Resource     ../keywords/common.robot
Resource     ../PageObjects/11__bulkLoanUploadApi.robot

*** Variables ***
${dbName}    zeus_colending
${dbUser}    vcpl_zeus_view
${dbPwd}     8a@9Wla(@$WZhVJaX
${dbHost}    10.100.10.158
${dbPort}    3306
${extId}     HHZ

*** Keywords ***
QA Mysql DB connection
    [Documentation]    Zeus QA Mysql db connection
    ${loanIdList}    Create List
    ${currentDate}   Get Current Date    result_format=%d-%m-%Y
    ${partnerId}      Get From Dictionary    ${partnerId}    key=${product}
    Connect To Database    pymysql  ${dbName}  ${dbUser}  ${dbPwd}  ${dbHost}  ${dbPort}

Take Loan Id from Mysql DB
    [Documentation]    After bulk loan upload take loan id from DB
    ${loanIdList}    Create List
    ${currentDate}   Get Current Date       result_format=%d-%m-%Y
    ${partnerId}     Get From Dictionary    ${partnerId}    key=${product}
    Connect To Database    pymysql  ${dbName}  ${dbUser}  ${dbPwd}  ${dbHost}  ${dbPort}
    ${output}   Query      select id from m_loan K where createdon_date > '${currentDate}' and partner_id ='${partnerId}' and loan_status_id='300' and external_id like '${extId}%';
    ${idList}   Evaluate   [item[0] for item in ${output}]
    ${startLoanId}    Set Variable   ${idList[0]}
    ${endLoanId}      Set Variable   ${idList[-1]}
    ${endLoanId}      Evaluate       ${endLoanId}+${1}
    Set Global Variable    ${startLoanId}
    Set Global Variable    ${endLoanId}
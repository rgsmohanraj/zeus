*** Settings ***
Resource    ../resources/imports.robot
Resource    ../keywords/variables.robot
Variables    var.yaml

*** Keywords ***
Partner,Product and loan count details
    Set Suite Metadata   Partner      ${partner}
    Set Suite Metadata   Product      ${product}
    Set Suite Metadata   LoanCount    ${loanCount}

Get data from RS API
  [Documentation]    Get required datas from RS API
  create session     RS     ${baseUrl.QA}    
  ${header}          Create Dictionary       Authorization=Basic YWRtaW46cGFzc3dvcmQ=   Fineract-Platform-TenantId=default  Content-Type=application/json
  ${Rurl}            Set Variable            /lms/api/v1/loans/${index}?associations=all&exclude=guarantors,futureSchedule
  ${response}        GET On Session          RS       ${Rurl}     headers=${header} 
  ${status_code}     convert to string       ${response.status_code}
  should be equal    ${status_code}          ${expCode}
  ${jsonData}        Convert String To Json  ${response.content}
  Set Test Variable    ${jsonData}
  #From RS API
  ${clientName}      Get Value From Json    ${jsonData}    clientName
  ${clientName}      Get From List          ${clientName}  0
  Set Test Variable    ${clientName}
  ${clientIdRs}      Get Value From Json    ${jsonData}    clientId
  ${clientIdRs}      Get From List          ${clientIdRs}  0
  Set Test Variable    ${clientIdRs}
  ${productClsRs}    Get Value From Json    ${jsonData}    loanProductData.assetClass.name
  ${productClsRs}    Get From List          ${productClsRs}  0
  Set Test Variable    ${productClsRs}
  ${loanAccNo}       Get Value From Json    ${jsonData}    accountNo
  ${loanAccNo}       Get From List          ${loanAccNo}   0
  Set Test Variable    ${loanAccNo}
  ${exId}            Get Value From Json    ${jsonData}    externalId
  ${exId}            Get From List          ${exId}        0
  Set Test Variable    ${exId}
  ${productNameRs}   Get Value From Json    ${jsonData}    loanProductName
  ${posRs}           Get Value From Json    ${jsonData}    summary.principalOutstanding
  ${posRs}           Get From List          ${posRs}       0
  Set Test Variable    ${posRs}
  ${loanStatusRs}    Get Value From Json    ${jsonData}    status.value
  ${loanStatusRs}    Get From List          ${loanStatusRs}       0  
  Set Test Variable    ${loanStatusRs}
  ${principal}       Get Value From Json    ${jsonData}    principal
  ${principal}       Get From List          ${principal}         0
  Set Test Variable    ${principal}
  ${interest}        Get Value From Json    ${jsonData}    annualInterestRate
  ${interest}        Get From List          ${interest}          0
  Set Test Variable    ${interest}
  ${tenure}          Get Value From Json    ${jsonData}    numberOfRepayments
  ${tenure}          Get From List          ${tenure}         0
  Set Test Variable    ${tenure}
  ${disDate}         Get Value From Json    ${jsonData}    timeline.actualDisbursementDate
  ${disDate}         Get From List          ${disDate}         0
  ${disDateMonth}     Run Keyword If        ${disDate[1]} != "0" and ${disDate[1]} < 10    Set Variable    0${disDate[1]}    ELSE    Set Variable    ${disDate[1]}
  ${disDateDay}       Run Keyword If        ${disDate[2]} != "0" and ${disDate[2]} < 10    Set Variable    0${disDate[2]}    ELSE    Set Variable    ${disDate[2]}
  ${disDate}          Convert Date          ${disDate[0]}-${disDateMonth}-${disDateDay}    result_format=%Y-%m-%d 
  ${disburDate}       Convert Date          ${disDate}    result_format=%d-%m-%Y 
  Set Test Variable    ${disDate}
  Set Test Variable    ${disburDate}
  ${1stRepay}         Get Value From Json    ${jsonData}      repaymentSchedule.periods[1].dueDate
  ${1stRepay}         Get From List          ${1stRepay}         0
  ${1stRepayMonth}     Run Keyword If        ${1stRepay[1]} != "0" and ${1stRepay[1]} < 10    Set Variable    0${1stRepay[1]}    ELSE    Set Variable    ${1stRepay[1]}
  ${1stRepayDay}       Run Keyword If        ${1stRepay[2]} != "0" and ${1stRepay[2]} < 10    Set Variable    0${1stRepay[2]}    ELSE    Set Variable    ${1stRepay[2]}
  ${1stRepay}          Convert Date          ${1stRepay[0]}-${1stRepayMonth}-${1stRepayDay}   result_format=%Y-%m-%d 
  Set Test Variable    ${1stRepay}
  ${expMaturityDate}   Get Value From Json    ${jsonData}      timeline.expectedMaturityDate
  ${expMaturityDate}   Get From List          ${expMaturityDate}         0
  ${dueDay}            Set Variable           ${expMaturityDate[2]}
  Set Test Variable    ${dueDay}

Get RS API Response
  [Documentation]    Get RS API Response and sent JsonData
  create session     RS     ${baseUrl.QA}    
  ${header}          Create Dictionary       Authorization=Basic YWRtaW46cGFzc3dvcmQ=   Fineract-Platform-TenantId=default  Content-Type=application/json
  ${Rurl}            Set Variable            /lms/api/v1/loans/${index}?associations=all&exclude=guarantors,futureSchedule
  ${response}        GET On Session          RS       ${Rurl}     headers=${header} 
  ${status_code}     convert to string       ${response.status_code}
  should be equal    ${status_code}          ${expCode}
  ${jsonData}        Convert String To Json  ${response.content}
  Set Test Variable    ${jsonData}



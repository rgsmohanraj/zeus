*** Settings ***
Resource    ../keywords/common.robot

*** Variables ***
${myState}            Tamil Nadu
${accuralType}        Daily

*** Keywords ***
NPA and DPD buket
    [Documentation]   In RS Loan NPA and DPD buket
    FOR    ${index}   IN RANGE    ${startLoanId}   ${endLoanId}
     create session     RPS     ${baseUrl.QA}
     ${header}     Create Dictionary    Authorization=Basic YWRtaW46cGFzc3dvcmQ=     Fineract-Platform-TenantId=default  Content-Type=application/json
     ${Rurl}       Set Variable         /lms/api/v1/loans/${index}?associations=all&exclude=guarantors,futureSchedule
     ${response}   GET On Session       RPS      ${Rurl}     headers=${header} 
     ${jsonData}   Convert String To Json        ${response.content}
     ${clientNameRs}      Get Value From Json    ${jsonData}    clientName
     ${productNameRs}     Get Value From Json    ${jsonData}    loanProductName
     ${exIdRs}            Get Value From Json    ${jsonData}    externalId
     ${loanStatus}        Get Value From Json    ${jsonData}    status.value
     ${loanStatus}        Get From List          ${loanStatus}  0
    END
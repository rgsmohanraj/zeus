*** Settings ***
Resource    ../keywords/common.robot

*** Variables ***
${myState}            Tamil Nadu
${accuralType}        Daily

*** Keywords ***
Loan Daily Accurals
    [Documentation]   RS Loan daily accurals
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
     ${matureDate}        Get Value From Json    ${jsonData}    timeline.expectedMaturityDate    
     ${matureDate}        Get From List          ${matureDate}  0
     ${matureDateYear}    Get From List          ${matureDate}  0
     ${matureDateMonth}   Get From List          ${matureDate}  1
     ${matureDateDay}     Get From List          ${matureDate}  2
     ${matureDateMonth}   Run Keyword If    ${matureDateMonth} != "0" and ${matureDateMonth} < 10    Set Variable    0${matureDateMonth}    ELSE    Set Variable    ${matureDateMonth}
     ${matureDateDay}     Run Keyword If    ${matureDateDay} != "0" and ${matureDateDay} < 10      Set Variable    0${matureDateDay}     ELSE    Set Variable    ${matureDateDay}
     ${matureDateRS}      Convert Date      ${matureDateYear}-${matureDateMonth}-${matureDateDay}    result_format=%Y-%m-%d
     ${closedDate}        Get Value From Json    ${jsonData}    timeline.closedOnDate
     ${disDateRS}       Get Value From Json    ${jsonData}    repaymentSchedule.periods[0].dueDate
     ${disDateRS}       Get From List          ${disDateRs}   0
     ${disDate}         Get From List          ${disDateRs}   2
     ${disDate}         Evaluate               str(${disDate})
     ${disMonth}        Get From List          ${disDateRs}   1
     ${disMonth}        Evaluate               str(${disMonth})
     ${disYear}         Get From List          ${disDateRs}   0
     ${disMonth}        Run Keyword If    ${disMonth} != "0" and ${disMonth} < 10    Set Variable    0${disMonth}    ELSE    Set Variable    ${disMonth}
     ${disDate}         Run Keyword If    ${disDate} != "0" and ${disDate} < 10      Set Variable    0${disDate}     ELSE    Set Variable    ${disDate}
     ${disDateRS}       Convert Date      ${disYear}-${disMonth}-${disDate}    result_format=%Y-%m-%d
     IF  ${closedDate} != [] and '${loanStatus}' == 'foreclosed'
       ${closedDate}      Get From List     ${closedDate}    0
       ${closedDateYear}    Get From List   ${closedDate}  0
       ${closedDateMonth}   Get From List   ${closedDate}  1
       ${closedDateDay}     Get From List   ${closedDate}  2
       ${closedDateMonth}   Run Keyword If  ${closedDateMonth} != "0" and ${closedDateMonth} < 10    Set Variable    0${closedDateMonth}    ELSE    Set Variable    ${closedDateMonth}
       ${closedDateDay}     Run Keyword If  ${closedDateDay} != "0" and ${closedDateDay} < 10      Set Variable    0${closedDateDay}     ELSE    Set Variable    ${closedDateDay}
       ${closedDateRS}      Convert Date    ${closedDateYear}-${closedDateMonth}-${closedDateDay}
       ${endTransNum}     Subtract Date From Date   ${closedDateRS}  ${disDateRS}    verbose
     ELSE
       ${currDate}        Get Current Date       result_format=%Y-%m-%d
       ${endTransNum}     Subtract Date From Date   ${currDate}  ${disDateRS}    verbose
       ${closedDateRS}    Set Variable    ${matureDateRS}
     END
       IF  '${endTransNum}' == '1 day'
        ${endTransNum}    Get Substring           ${endTransNum}    0    -4
       ELSE
        ${endTransNum}    Get Substring           ${endTransNum}    0    -5
       END
       ${endTransNum}    Convert To Integer    ${endTransNum}
       FOR    ${T}   IN RANGE    0    ${endTransNum}
        ${accFromDate}     Add Time To Date       ${disDateRS}   ${T}days    result_format=%Y-%m-%d
        Exit For Loop If   '${accFromDate}' == '${matureDateRS}'
        Exit For Loop If   '${accFromDate}' == '${closedDateRS}' and '${loanStatus}' == 'foreclosed'
        ${installmentNum}  Get Value From Json    ${jsonData}    loanAccruals[${T}].installment
        ${accuralTypeRs}   Get Value From Json    ${jsonData}    loanAccruals[${T}].accrualType
        ${fromDate}        Get Value From Json    ${jsonData}    loanAccruals[${T}].fromDate
        ${toDate}          Get Value From Json    ${jsonData}    loanAccruals[${T}].toDate
        ${accAmount}       Get Value From Json    ${jsonData}    loanAccruals[${T}].accruedAmount
        ${selfAccAmount}   Get Value From Json    ${jsonData}    loanAccruals[${T}].selfAccruedAmount
        ${parAccAmount}    Get Value From Json    ${jsonData}    loanAccruals[${T}].partnerAccruedAmount
        ${installmentNum}  Get From List          ${installmentNum}  0
        ${fromDate}        Get From List          ${fromDate}        0
        ${fromDateYear}    Get From List          ${fromDate}        0
        ${fromDateMonth}   Get From List          ${fromDate}        1
        ${fromDateDay}     Get From List          ${fromDate}        2
        ${fromDateMonth}   Run Keyword If    ${fromDateMonth} != "0" and ${fromDateMonth} < 10    Set Variable    0${fromDateMonth}    ELSE    Set Variable    ${fromDateMonth}
        ${fromDateDay}     Run Keyword If    ${fromDateDay} != "0" and ${fromDateDay} < 10      Set Variable    0${fromDateDay}     ELSE    Set Variable    ${fromDateDay}
        ${fromDateRS}      Convert Date      ${fromDateYear}-${fromDateMonth}-${fromDateDay}    result_format=%Y-%m-%d
        ${toDate}          Get From List          ${toDate}          0
        ${accAmount}       Get From List          ${accAmount}       0
        ${selfAccAmount}   Get From List          ${selfAccAmount}   0
        ${parAccAmount}    Get From List          ${parAccAmount}    0
        ${accuralTypeRs}   Get From List          ${accuralTypeRs}   0
        Should Be Equal    ${accFromDate}         ${fromDateRS}
        IF  '${accuralType}' == 'Daily'
          Should Be Equal   ${1}     ${accuralTypeRs}
        END
        ${interestRs}      Get Value From Json   ${jsonData}      repaymentSchedule.periods[${installmentNum}].interestDue
        ${dueDateRs}       Get Value From Json   ${jsonData}      repaymentSchedule.periods[${installmentNum}].dueDate
        ${numOfDays}       Get Value From Json   ${jsonData}      repaymentSchedule.periods[${installmentNum}].daysInPeriod
        ${interestRs}      Get From List         ${interestRs}    0
        ${dueDateRs}       Get From List         ${dueDateRs}     0
        ${dueDateYear}     Get From List         ${dueDateRs}     0
        ${dueDateMonth}    Get From List         ${dueDateRs}     1
        ${dueDateDay}      Get From List         ${dueDateRs}     2
        ${numOfDays}       Get From List         ${numOfDays}     0
        ${dueDateMonth}   Run Keyword If     ${dueDateMonth} != "0" and ${dueDateMonth} < 10    Set Variable    0${dueDateMonth}    ELSE    Set Variable    ${dueDateMonth}
        ${dueDateDay}     Run Keyword If     ${dueDateDay} != "0" and ${dueDateDay} < 10      Set Variable    0${dueDateDay}     ELSE    Set Variable    ${dueDateDay}
        ${dueDateRs}      Convert Date       ${dueDateYear}-${dueDateMonth}-${dueDateDay}    result_format=%Y-%m-%d
        ${perDayAccural}    Evaluate         ${interestRs}/${numOfDays}
        Should Be Equal As Numbers     ${accAmount}    ${perDayAccural}   ${selfAccAmount}   precision=0
        Should Be Equal                ${0}            ${parAccAmount}
       END
    END
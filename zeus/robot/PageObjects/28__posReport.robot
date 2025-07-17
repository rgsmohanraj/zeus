*** Settings ***
Resource    ../keywords/common.robot
Resource    ../PageObjects/19__bureauReport.robot
Library     28__posReport.py

*** Variables ***
${loanStatusRs}        Active  
${activeLoanStatus}    Active

*** Keywords ***
Download POS report
  [Documentation]    Download the POS report   
  Getting report dates
  Click Element    xpath://a[@href='#/reports']
  Sleep    1
  Click Element    xpath://td[text()=' Principal Outstanding Report ']
  FOR  ${date}  IN  @{DATES}
    Log    Report downloaded date : ${date}
    ${day}     Convert Date    ${date}    result_format=%d
    ${day}     Run Keyword If    "${day[0]}" == "0"  Evaluate   "${day[1:]}"  ELSE  Set Variable  ${day}
    ${month}   Convert Date    ${date}    result_format=%b
    ${month}   Convert To Upper Case    ${month}
    ${year}    Convert Date    ${date}    result_format=%Y
    Sleep    1s
    Click Element    xpath://*[text()='As On']//ancestor::div[1]
    Click Element    xpath://button[@cdkarialive='polite']
    Click Element    xpath://div[text()=' ${year} ']
    Click Element    xpath://div[text()=' ${month} ']
    Click Element    xpath://div[text()=' ${day} ']
    Click Element    xpath:(//mat-select[@role='combobox'])[1]
    Sleep    1s
    Click Element    xpath://*[contains(text(),'${partner}')]
    Click Element    xpath://*[@icon="cogs"]
    Sleep  3s
    Log    POS report downloaded
    ${reportDate}    Convert Date    ${date}    result_format=%Y-%m-%d
    Set Test Variable    ${reportDate}
  END

Download POS report using API
  ${currDay}     Get Current Date     result_format=%d
  ${currMonth}   Get Current Date     result_format=%B
  ${partnerId}   Get From Dictionary    ${partnerId}    key=${product}
  create session     getPOSReport     ${baseUrl.QA}
  ${header}     Create Dictionary    Authorization=Basic YWRtaW46cGFzc3dvcmQ=     Fineract-Platform-TenantId=default  Content-Type=application/json
  ${Rurl}       Set Variable         /lms/api/v1/runreports/Principal%20Outstanding%20Report?R_asOn=${day}%20${month}%20${year}&R_partnerId=${partnerId}&locale=en&dateFormat=dd%20MMMM%20yyyy&exportCSV=true
  ${response}   GET On Session       getPOSReport      ${Rurl}     headers=${header}
  ${currDay}       Get Time  
  ${currDay}       Replace String      ${currDay}        :    ${EMPTY}
  Create Binary File    ${CURDIR}//testData//POS_Report_${currDay}_${date}.csv      ${response.content}

Check in POS report 
    [Documentation]    Check in POS report values with bulk loan excel values
    ${DATES}           Create List
    ${failed_loanId}   Create List
    ${dateReported}    Get Current Date    result_format=%d%m%Y
    ${dateReported}    Set Variable   '${dateReported}
    ${currDate}    Get Current Date     result_format=%Y-%m-%d
    # Take values from Bulk loan excel file
    FOR    ${index}    IN RANGE    ${startLoanId}   ${endLoanId}
     Set Test Variable    ${index}
     IF  '${takeValue}' == 'excel'
      ${rowNo}         Evaluate     (${index}-${startLoanId}+2)
      Open Workbook    ${bulkLoanFile}
      ${exId}         Read From Cell    B${rowNo}
      ${firstName}    Read From Cell    F${rowNo}
      ${middleName}   Read From Cell    G${rowNo}
      ${lastName}     Read From Cell    H${rowNo}
      IF  '${middleName}'=='None'
        ${clientName}   Set Variable      ${firstName} ${lastName}
      ELSE
        ${clientName}   Set Variable      ${firstName} ${middleName} ${lastName} 
      END             
      ${tenure}        Read From Cell   AI${rowNo}
      ${disburseDate}  Read From Cell   AG${rowNo}
      ${disburDate}    Convert Date     ${disburseDate}    result_format=%d-%m-%Y
      ${disDate}       Convert Date     ${disburseDate}    result_format=%Y-%m-%d
      ${disDate}       Replace String   ${disDate}         00:00:00.000    ${EMPTY}
      ${principal}     Read From Cell    AH${rowNo}
      ${dueDay}        Read From Cell    AK${rowNo}
      Set Test Variable   ${dueDay}
      ${interest}      Read From Cell    AL${rowNo}
      ${tenure}        Read From Cell    AI${rowNo}
      Set Test Variable    ${tenure}
      ${1stRepay}       Read From Cell    AJ${rowNo}
      Set Test Variable   ${1stRepay}
      Close Workbook
      Get RS API Response
     ELSE
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
     END
      ${2ndDate}       Add Time To Date    ${disDate}         20 days
      ${2ndDate}       Replace String      ${2ndDate}         00:00:00.000    ${EMPTY}
      Run Keyword If    '${2ndDate}' <= '${currDate}'    Append To List   ${DATES}     ${2ndDate}
      ${3rdDate}       Add Time To Date    ${disDate}         33 days
      ${3rdDate}       Replace String      ${3rdDate}         00:00:00.000    ${EMPTY}
      Run Keyword If    '${3rdDate}' <= '${currDate}'    Append To List   ${DATES}     ${3rdDate}
      ${4thDate}       Add Time To Date    ${disDate}         157 days
      ${4thDate}       Replace String      ${4thDate}         00:00:00.000    ${EMPTY}
      Run Keyword If    '${4thDate}' <= '${currDate}'    Append To List   ${DATES}     ${4thDate}
      ${5thDate}       Add Time To Date    ${disDate}         212 days
      ${5thDate}       Replace String      ${5thDate}         00:00:00.000    ${EMPTY}
      Run Keyword If    '${5thDate}' <= '${currDate}'    Append To List   ${DATES}     ${5thDate}
      Set Suite Variable    ${DATES}
      # Click Element    xpath://a[@href='#/reports']
      # Sleep    1
      # Click Element    xpath://td[text()=' Principal Outstanding Report ']
      FOR   ${c}  ${date}  IN ENUMERATE    @{DATES}
        Log    Report downloaded date : ${date}
        Set Global Variable    ${date}
        ${day}     Convert Date    ${date}    result_format=%d
        ${day}     Run Keyword If    "${day[0]}" == "0"  Evaluate   "${day[1:]}"  ELSE  Set Variable  ${day}
        ${month}   Convert Date    ${date}    result_format=%B
        ${year}    Convert Date    ${date}    result_format=%Y
        Set Global Variable    ${day}
        Set Global Variable    ${month}
        Set Global Variable    ${year}
        Download POS report using API
        # Sleep    1s
        # Click Element    xpath://*[text()='As On']//ancestor::div[1]
        # Click Element    xpath://button[@cdkarialive='polite']
        # Click Element    xpath://div[text()=' ${year} ']
        # Click Element    xpath://div[text()=' ${month} ']
        # Click Element    xpath://div[text()=' ${day} ']
        # IF   ${c} == 0
        #  Click Element    xpath:(//mat-select[@role='combobox'])[1]
        #  Sleep    0.5s
        #  Click Element    xpath://*[contains(text(),'${partner}')]
        # END
        # Click Element    xpath://*[@icon="cogs"]
        # Sleep  3s
        Log    POS report downloaded
        ${posReportPath}    11__getRecentFile.Get Most Recent Csv File
        ${reportDate}    Convert Date    ${date}    result_format=%Y-%m-%d
        Set Test Variable    ${reportDate}
        Comment   POS Maturity Date test
        ${maturityDate}      POS Find Maturity date
        ${remTenureEmi}      POS Find remaining tenure,EMI and ageing
        ${remTenure}    Set Variable    ${remTenureEmi[0]}
        ${emi}          Set Variable    ${remTenureEmi[1]}
        ${aging}          Set Variable    ${remTenureEmi[2]}
        ${lastPay}      POS last paid date and amount test
        ${lastPaidDate}      Set Variable    ${lastPay[0]}
        ${lastPaidAmount}    Set Variable    ${lastPay[1]}
        ${statusClosedDate}    Find POS Loan status and closed date
        ${closingDate}      Set Variable    ${statusClosedDate[0]}
        ${statusLoan}       Set Variable    ${statusClosedDate[1]}
        ${result}         28__posReport.Search pos Csv By LoanAccountNumber   ${posReportPath}  ${clientIdRs}  ${clientName}  ${productClsRs}  ${loanAccNo}  ${disburDate}  ${maturityDate}  ${tenure}  ${remTenure}  ${interest}  ${principal}   ${emi}  ${aging}  ${closingDate}  ${statusLoan}  ${lastPaidDate}  ${lastPaidAmount}
        # ${result}         28__posReport.Search pos Csv By LoanAccountNumber   ${posReportPath}  ${clientIdRs}  ${clientName}  ${productClsRs}  ${disDate}  ${maturityDate}  ${tenure}  ${lastPaidDate}  ${dateClosed}   ${emiRs}   ${dateReported}  ${principal}  ${interest}  ${loanAccNo}  ${currentBalance}  ${dpd}  ${odAmountRs}  ${status}
        Run Keyword If    '${result}' != 'PASS'    Run Keyword And Continue On Failure     Log   Loop number ${index} failed!
        Run Keyword If    '${result}' != 'PASS'    Append To List    ${failed_loanId}    ${index}=${date}
      END
      FOR   ${elem}    IN   @{DATES}
        Remove From List    ${DATES}   0
      END  
    END
    Run Keyword If    ${failed_loanId} != []    Set Test Message    The following Loan id's failed in POS report: ${failed_loanId}

POS Find Maturity date
    ${month_days}      Create Dictionary     1=31    2=28    3=31    4=30    5=31    6=30   7=31    8=31    9=30    10=31    11=30    12=31
    ${1stRepay}    Convert Date   ${1stRepay}    result_format=%Y-%m-%d
    ${date}        Convert Date   ${1stRepay}    result_format=%d
    ${month}       Convert Date   ${1stRepay}    result_format=%m
    ${year}        Convert Date   ${1stRepay}    result_format=%Y
    ${month}       Run Keyword If    "${month[0]}" == "0"    Evaluate    "${month[1:]}"    ELSE    Set Variable    ${month}
    ${maturesOnMonth}=    Evaluate    ${month}+${tenure}-${1}
    IF  "${month[0]}" != "0" and ${month} < 10
      ${month}  Run Keyword If  ${month} !=0  Set Variable    0${month}
    ELSE
      ${month}  Set Variable    ${month}
    END
    ${1stRepay}    Set Variable    ${date}-${month}-${year}
    ${1stRepay}    Set Variable    ${date}-${month}-${year}
    IF  12 < ${maturesOnMonth} <= 24 
      ${maturesOnMonth}=    Evaluate    str(int(${maturesOnMonth})-${12})
      ${year}=    Evaluate    ${year}+1
    ELSE IF  24 < ${maturesOnMonth} <= 36
      ${maturesOnMonth}=    Evaluate    str(int(${maturesOnMonth})-${24})
      ${year}=    Evaluate    ${year}+2
    ELSE IF  36 < ${maturesOnMonth} <= 48
      ${maturesOnMonth}=    Evaluate    str(int(${maturesOnMonth})-${36})
      ${year}=    Evaluate    ${year}+3
    ELSE
      ${maturesOnMonth}=    Evaluate    str(int(${maturesOnMonth}))
      ${year}=    Set Variable    ${year}
    END
    ${max_days}     Set Variable      ${month_days}[${maturesOnMonth}]
    ${maturesOnMonth}=    Evaluate    str(${maturesOnMonth})
    ${dueDay2}      Run Keyword If   ${dueDay} > ${max_days}    Set Variable    ${max_days}   ELSE   Set Variable   ${dueDay}
    ${dueDay2}      Run Keyword If   ${year} == ${2020} or ${year} == ${2024} and ${maturesOnMonth} == ${2} and ${dueDay} > ${28}   Set Variable    29   ELSE   Set Variable   ${dueDay2}
    IF  "${maturesOnMonth[0]}" != "0" and ${maturesOnMonth} < 10
      ${maturesOnMonth}  Run Keyword If  ${maturesOnMonth[0]} !=0  Set Variable    0${maturesOnMonth}
    ELSE    
      ${maturesOnMonth}  Set Variable    ${maturesOnMonth}
    END
    ${dueDay2}    Evaluate    str(${dueDay2})
    IF  "${dueDay2[0]}" != "0" and ${dueDay2} < 10
      ${dueDay2}  Run Keyword If  ${dueDay2[0]} !=0  Set Variable    0${dueDay2}
    ELSE    
      ${dueDay2}  Set Variable    ${dueDay2}
    END
    ${maturesOn}              Set Variable      ${year}-${maturesOnMonth}-${dueDay2}
    ${maturesOnMonth}         Convert Date      ${maturesOn}    result_format=%m
    ${maturityDate}           Set Variable      ${dueDay2}-${maturesOnMonth}-${year}
    [Return]       ${maturityDate} 

POS Find remaining tenure,EMI and ageing
   ${paidDueCount}       Set Variable    ${0}
   ${pendingDueCount}    Set Variable    ${0}
   ${1stNotPaidDue}      Set Variable    ${0}
   FOR    ${te}    IN RANGE    1    ${tenure}
    ${emi}      Get Value From Json    ${jsonData}    repaymentSchedule.periods[${te}].totalOriginalDueForPeriod
    ${emi}      Get From List          ${emi}         0
    IF  '${reportDate}' <= '${1stRepay}'
     ${remTenure}    Set Variable   ${tenure}
     ${emi}          Set Variable   ${emi}
     ${aging}        Set Variable   ${EMPTY}
     Exit For Loop
    END
    ${nextDueDate}      Get Value From Json    ${jsonData}       repaymentSchedule.periods[${te}].dueDate
    ${nextDueDate}      Get From List          ${nextDueDate}    0
    ${nextDueMonth}     Run Keyword If         ${nextDueDate[1]} != "0" and ${nextDueDate[1]} < 10    Set Variable    0${nextDueDate[1]}    ELSE    Set Variable    ${nextDueDate[1]}
    ${nextDueDay}       Run Keyword If         ${nextDueDate[2]} != "0" and ${nextDueDate[2]} < 10    Set Variable    0${nextDueDate[2]}    ELSE    Set Variable    ${nextDueDate[2]}
    ${nextDueDueDate}   Convert Date           ${nextDueDate[0]}-${nextDueMonth}-${nextDueDay}   result_format=%Y-%m-%d 
    Run Keyword If    '${reportDate}' <= '${nextDueDueDate}'    Exit For Loop
    ${dueStatus}        Get Value From Json    ${jsonData}     repaymentSchedule.periods[${te}].complete
    ${dueStatus}        Get From List          ${dueStatus}    0
    Set Global Variable    ${te}
    Run Keyword      Find Last obligation met date
    IF  '${dueStatus}' == 'True'
     ${paidDueCount}   Run Keyword If   '${reportDate}' >= '${lastOblMetDate}'   Evaluate   ${paidDueCount} + ${1}   ELSE    Set Variable     ${paidDueCount}
     ${aging}          Run Keyword If   '${reportDate}' <= '${lastOblMetDate}'   Subtract Date From Date    ${reportDate}  ${nextDueDueDate}   verbose    ELSE    Set Variable   ${0}
     IF  '${aging}' != '${EMPTY}' and '${aging}' != '${0}'
      IF  '${aging}' == '1 day'
       ${aging}    Get Substring        ${aging}    0    -4
      ELSE
       ${aging}    Get Substring        ${aging}    0    -5
      END
       ${aging}    Convert To Integer   ${aging}
     END
    ELSE
     ${paidDueCount}    Set Variable     ${paidDueCount}
     IF  ${1stNotPaidDue} == 0
      ${1stNotPaidDue}   Evaluate        ${1stNotPaidDue}+${1}
      ${dueCalDate}      Set Variable    ${nextDueDueDate}
      ${aging}    Subtract Date From Date    ${reportDate}  ${dueCalDate}   verbose
      IF  '${aging}' != '${EMPTY}' and '${aging}' != '${0}'
       IF  '${aging}' == '1 day'
        ${aging}    Get Substring        ${aging}    0    -4
       ELSE
        ${aging}    Get Substring        ${aging}    0    -5
       END
        ${aging}   Convert To Integer   ${aging}
      END
     END
    END
    # ${paidDueCount}     Run Keyword If    '${dueStatus}' == 'True'    Evaluate   ${paidDueCount} + ${1}     ELSE    Set Variable     ${paidDueCount}
    ${pendingDueCount}  Run Keyword If    '${dueStatus}' == 'False'   Evaluate   ${pendingDueCount} + ${1}  ELSE    Set Variable     ${pendingDueCount}
    ${remTenure}        Evaluate    ${tenure} - ${paidDueCount}
   END
   [Return]     ${remTenure}   ${emi}  ${aging}

POS last paid date and amount test
   Comment   Bureau Last paid date test
   ${fees1Rs}         Get Value From Json    ${jsonData}    charges[0].name
   ${fees2Rs}         Get Value From Json    ${jsonData}    charges[1].name
   FOR  ${transNum}   IN RANGE     1    50
    ${lastPayDateRs}     Get Value From Json    ${jsonData}   transactions[${transNum}].date
    ${lastPayAmountRs}   Get Value From Json    ${jsonData}   transactions[${transNum}].amount
    IF  ${transNum} != 1 or ${fees1Rs} and ${fees2Rs} == []
     IF   ${lastPayDateRs} == [] and ${transNum} < ${2}
       ${lastPaidDate}     Set Variable         ${EMPTY}
       ${lastPaidAmount}   Set Variable         ${EMPTY}
       Exit For Loop
     END
     Run Keyword If       ${lastPayDateRs} == []    Exit For Loop
     ${lastPayDateRs}     Get From List          ${lastPayDateRs}    0
     ${lastPayDateRs0}    Set Variable           ${lastPayDateRs[0]}
     ${lastPayDateRs1}    Set Variable           ${lastPayDateRs[1]}
     ${lastPayDateRs2}    Set Variable           ${lastPayDateRs[2]}
     ${lastPayDateRs1}    Run Keyword If         ${lastPayDateRs[1]} != "0" and ${lastPayDateRs[1]} < 10    Set Variable    0${lastPayDateRs[1]}    ELSE    Set Variable    ${lastPayDateRs[1]}
     ${lastPayDateRs2}    Run Keyword If         ${lastPayDateRs[2]} != "0" and ${lastPayDateRs[2]} < 10    Set Variable    0${lastPayDateRs[2]}    ELSE    Set Variable    ${lastPayDateRs[2]}
     ${lastPaidDateRs}    Set Variable           ${lastPayDateRs0}-${lastPayDateRs1}-${lastPayDateRs2}
     IF  '${reportDate}' >= '${lastPaidDateRs}'
      ${lastPaidDate}   Set Variable   ${lastPayDateRs2}-${lastPayDateRs1}-${lastPayDateRs0}
      ${lastPaidAmount}     Get From List    ${lastPayAmountRs}    0
     END
     Run Keyword If      '${reportDate}' <= '${lastPaidDateRs}'    Exit For Loop
    ELSE
     ${lastPaidDate}   Set Variable    ${EMPTY}
     ${lastPaidAmount}   Set Variable    ${EMPTY}
    END
   END
   IF  ${transNum} == 1 or (${transNum} == 0 and ${fees1Rs} == [] and ${fees2Rs} == [])
    ${lastPaidDate}    Set Variable   ${EMPTY}
   END
   [Return]    ${lastPaidDate}    ${lastPaidAmount}
   
Find Last obligation met date
  ${lastOblMetDate}    Get Value From Json    ${jsonData}   repaymentSchedule.periods[${te}].obligationsMetOnDate
  IF  ${lastOblMetDate} != []
   ${lastOblMetDate}    Get From List          ${lastOblMetDate}    0
   ${lastOblMetMonth}   Run Keyword If         ${lastOblMetDate[1]} != "0" and ${lastOblMetDate[1]} < 10    Set Variable    0${lastOblMetDate[1]}    ELSE    Set Variable    ${lastOblMetDate[1]}
   ${lastOblMetDay}     Run Keyword If         ${lastOblMetDate[2]} != "0" and ${lastOblMetDate[2]} < 10    Set Variable    0${lastOblMetDate[2]}    ELSE    Set Variable    ${lastOblMetDate[2]}
   ${lastOblMetDate}    Convert Date           ${lastOblMetDate[0]}-${lastOblMetMonth}-${lastOblMetDay}   result_format=%Y-%m-%d 
  ELSE
   ${lastOblMetDate}    Set Variable   ${0}
  END
  Set Global Variable    ${lastOblMetDate}

Find POS Loan status and closed date
  ${loanStatus}      Get Value From Json    ${jsonData}    status.value
  ${loanStatus}      Get From List          ${loanStatus}  0
  # IF  ${te} == ${tenure} or '${loanStatus}' == 'Foreclosed'
  #  ${closedDate}      Get Value From Json    ${jsonData}    timeline.closedOnDate
  #  ${loanStatus}      Set Variable           ${loanStatus}
  #  IF  ${closedDate} != []
  #   ${closedDate}      Get From List          ${closedDate}  0 
  #   ${closedMonth}     Run Keyword If         ${closedDate[1]} != "0" and ${closedDate[1]} < 10    Set Variable    0${closedDate[1]}    ELSE    Set Variable    ${closedDate[1]}
  #   ${closedDay}       Run Keyword If         ${closedDate[2]} != "0" and ${closedDate[2]} < 10    Set Variable    0${closedDate[2]}    ELSE    Set Variable    ${closedDate[2]}
  #   ${closedDate}      Convert Date           ${closedDate[0]}-${closedMonth}-${closedDay}   result_format=%Y-%m-%d 
  #  ELSE
  #   ${closedDate}      Set Variable    ${EMPTY}
  #  END 
  # ELSE
  #   ${closedDate}      Set Variable    ${EMPTY}
  # END
  IF  '${loanStatus}' != 'Active'
   ${closedDate}      Get Value From Json    ${jsonData}    timeline.closedOnDate
   ${closedDate}      Get From List          ${closedDate}  0 
   ${closedMonth}     Run Keyword If         ${closedDate[1]} != "0" and ${closedDate[1]} < 10    Set Variable    0${closedDate[1]}    ELSE    Set Variable    ${closedDate[1]}
   ${closedDay}       Run Keyword If         ${closedDate[2]} != "0" and ${closedDate[2]} < 10    Set Variable    0${closedDate[2]}    ELSE    Set Variable    ${closedDate[2]}
   ${closedDate}      Convert Date           ${closedDate[0]}-${closedMonth}-${closedDay}   result_format=%Y-%m-%d
   IF  '${reportDate}' >= '${closedDate}'
     ${loanStatus}      Run Keyword If   '${loanStatus}' == 'Foreclosed'  Set Variable  Foreclosed  ELSE   Set Variable   Closed
   ELSE
     ${loanStatus}      Set Variable    ${activeLoanStatus}
     ${closedDate}      Set Variable    ${EMPTY}
   END    
  ELSE
   ${closedDate}      Set Variable    ${EMPTY}
   ${loanStatus}      Set Variable    ${activeLoanStatus}
  END
  Set Global Variable     ${closedDate}
  Set Global Variable     ${loanStatus}
  [Return]    ${closedDate}  ${loanStatus}
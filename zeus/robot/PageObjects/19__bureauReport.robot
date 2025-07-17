*** Settings ***
Resource    ../keywords/common.robot
Library    ../testcases/11__getRecentFile.py
Library    19__bureaReport.py

*** Variables ***
${loanStatusRs}    Active  

*** Keywords ***
Getting report dates
  [Documentation]    Random date report download
  ${DATES}         Create List
  ${currDay}       Get Time  
  ${currDay}       Run Keyword If    "${currDay[0]}" == "0"  Evaluate  "${currDay[1:]}"  ELSE  Set Variable   ${currDay}
  ${2ndDate}       Subtract Time From Date    ${currDay}   20 days
  ${3rdDate}       Subtract Time From Date    ${currDay}   33 days
  ${4thDate}       Subtract Time From Date    ${currDay}   157 days
  ${5thDate}       Subtract Time From Date    ${currDay}   398 days
  Append To List   ${DATES}    ${currDay}   ${2ndDate}    ${3rdDate}  ${4thDate}  ${5thDate} 
  Set Suite Variable    ${DATES}

Download Bureau report
  [Documentation]    Download the Bureau report   
  Getting report dates
  Click Element    xpath://a[@href='#/reports']
  Sleep    1
  Click Element    xpath://td[text()=' Bureau Report ']
  FOR  ${date}  IN  @{DATES}
    Log    Report downloaded date : ${date}
    ${day}     Convert Date    ${date}    result_format=%d
    ${day}     Run Keyword If    "${day[0]}" == "0"  Evaluate   "${day[1:]}"  ELSE  Set Variable  ${day}
    ${month}   Convert Date    ${date}    result_format=%b
    ${month}   Convert To Upper Case    ${month}
    ${year}    Convert Date    ${date}    result_format=%Y
    Sleep    1
    Click Element    xpath://*[text()='As On']//ancestor::div[1]
    Click Element    xpath://button[@cdkarialive='polite']
    Click Element    xpath://div[text()=' ${year} ']
    Click Element    xpath://div[text()=' ${month} ']
    Click Element    xpath://div[text()=' ${day} ']
    Click Element    xpath://*[@icon="cogs"]
    Sleep  3s
    Log    Bureau report downloaded
    ${reportDate}    Convert Date    ${date}    result_format=%Y-%m-%d
    Set Test Variable    ${reportDate}
  END

Download Bureau report using API
  ${currDay}     Get Current Date     result_format=%d
  ${currMonth}   Get Current Date     result_format=%B
  create session     getBuraeuReport     ${baseUrl.QA}
  ${header}     Create Dictionary    Authorization=Basic YWRtaW46cGFzc3dvcmQ=     Fineract-Platform-TenantId=default  Content-Type=application/json
  ${Rurl}       Set Variable         /lms/api/v1/runreports/Bureau%20Report?R_asOn=${currDay}%20${currMonth}%202024&locale=en&dateFormat=dd%20MMMM%20yyyy&exportCSV=true
  ${response}   GET On Session       getBuraeuReport      ${Rurl}     headers=${header}
  ${currDay}       Get Time  
  ${currDay}       Replace String      ${currDay}        :    ${EMPTY}
  Create Binary File    ${CURDIR}//testData//Bureau_Report_${currDay}.csv      ${response.content}

Check in Bureau report 
    [Documentation]    Check in Bureau report values with bulk loan excel values
    ${DATES}           Create List
    ${failed_loanId}   Create List
    ${genderList}      Create Dictionary     Male=1    Female=2    Transgender=3
    ${accType}         Set Variable      5
    ${writeOffAmount}  Set Variable      0
    ${writeOffPriAmount}  Set Variable   0
    ${stateCode}       Create Dictionary     Jammu and Kashmir=1   Himachal Pradesh=2  Punjab=3   Chandigarh=4   Uttarakhand=5  Haryana=6  Delhi=7  Rajasthan=8   Uttar pradesh=9  Bihar=10   Sikkim=11   Arunachal pradesh=12  Nagaland=13  Manipur=14  Mizoram=15  Tripura=16  Meghalaya=17  Assam=18  West Bengal=19  Jharkhand=20  Odisha=21  Chattisgarh=22  Madhya pradesh=23  Gujarat=24  Dadra and Nagar Haveli and Daman and Diu=26  Maharashtra=27  Andhra Pradesh=28  Karnataka=29  Goa=30  Lakshadweep=31  Kerala=32  Tamil Nadu=33  Puducherry=34  Andaman and Nicobar Islands=35   Telangana=36  Ladakh=38
    ${dateReported}    Get Current Date    result_format=%d%m%Y
    ${dateReported}    Set Variable   '${dateReported}
    ${currDate}    Get Current Date     result_format=%Y-%m-%d
    # Take values from Bulk loan excel file
    FOR    ${index}    IN RANGE    ${startLoanId}   ${endLoanId}
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
      ${dob}          Read From Cell    K${rowNo}
      ${dob}          Convert Date      ${dob}    result_format=%d%m%Y
      ${gender1}      Read From Cell    J${rowNo}
      ${gender}       Set Variable      ${genderList}[${gender1}]
      ${pan}          Read From Cell    L${rowNo}
      ${passport}     Read From Cell    O${rowNo}
      ${voterId}      Read From Cell    N${rowNo}
      ${DL}           Read From Cell    P${rowNo}
      ${mobNum}       Read From Cell    S${rowNo}
      ${email}        Read From Cell    T${rowNo}
      ${address}      Read From Cell    Q${rowNo}
      ${clientState}  Read From Cell    V${rowNo}
      ${stateCode1}   Set Variable      ${stateCode}[${clientState}]
      ${pinCode}      Read From Cell    R${rowNo}
      ${tenure}       Read From Cell    AI${rowNo}
      ${disburseDate}  Read From Cell   AG${rowNo}
      ${disDate}      Convert Date   ${disburseDate}    result_format=%Y-%m-%d
      ${disburseDate}    Convert Date   ${disburseDate}    result_format=%d%m%Y
      ${principal}    Read From Cell    AH${rowNo}
      ${interest}     Read From Cell    AL${rowNo}
      ${tenure}       Read From Cell    AI${rowNo}
      ${firstRepayOn}   Read From Cell    AJ${rowNo}
      ${firstRepayOn}   Convert Date     ${firstRepayOn}
      ${firstRepayOn}   Replace String    ${firstRepayOn}    00:00:00.000    ${EMPTY}
      Close Workbook
      create session     RS     ${baseUrl.QA}    
      ${header}          Create Dictionary       Authorization=Basic YWRtaW46cGFzc3dvcmQ=   Fineract-Platform-TenantId=default  Content-Type=application/json
      ${Rurl}            Set Variable            /lms/api/v1/loans/${index}?associations=all&exclude=guarantors,futureSchedule
      ${response}        GET On Session          RS       ${Rurl}     headers=${header} 
      ${status_code}     convert to string       ${response.status_code}
      should be equal    ${status_code}          ${expCode}
      ${jsonData}        Convert String To Json  ${response.content}
      #From RS API
      ${clientNameRs}    Get Value From Json    ${jsonData}    clientName
      ${loanAccNo}       Get Value From Json    ${jsonData}    accountNo
      ${loanAccNo}       Get From List          ${loanAccNo}   0
      ${exIdRs}          Get Value From Json    ${jsonData}    externalId
      ${productNameRs}   Get Value From Json    ${jsonData}    loanProductName
      ${assetClassify}   Get Value From Json    ${jsonData}    loanProductData.assetClass.name
      IF  ${assetClassify} != []
        ${assetClassify}   Get From List        ${assetClassify}       0
      ELSE
        ${assetClassify}   Set Variable         ${EMPTY}
      END
      ${fees1Rs}         Get Value From Json    ${jsonData}    charges[0].name
      ${fees2Rs}         Get Value From Json    ${jsonData}    charges[1].name
      ${posRs}           Get Value From Json    ${jsonData}    summary.principalOutstanding
      ${posRs}           Get From List          ${posRs}       0
      ${loanStatusRs}    Get Value From Json    ${jsonData}    status.value
      ${loanStatusRs}    Get From List          ${loanStatusRs}       0  
      ${2ndDate}       Add Time To Date    ${disDate}         20 days
      Run Keyword If    '${2ndDate}' <= '${currDate}'    Append To List   ${DATES}     ${2ndDate}
      ${3rdDate}       Add Time To Date    ${disDate}         33 days
      Run Keyword If    '${3rdDate}' <= '${currDate}'    Append To List   ${DATES}     ${3rdDate}
      ${4thDate}       Add Time To Date    ${disDate}         157 days
      Run Keyword If    '${4thDate}' <= '${currDate}'    Append To List   ${DATES}     ${4thDate}
      ${5thDate}       Add Time To Date    ${disDate}         212 days
      Run Keyword If    '${5thDate}' <= '${currDate}'    Append To List   ${DATES}     ${5thDate}
      Set Suite Variable    ${DATES}
      Click Element    xpath://a[@href='#/reports']
      Sleep    1
      Click Element    xpath://td[text()=' Bureau Report ']
      FOR  ${date}  IN  @{DATES}
        Log    Report downloaded date : ${date}
        ${day}     Convert Date    ${date}    result_format=%d
        ${day}     Run Keyword If    "${day[0]}" == "0"  Evaluate   "${day[1:]}"  ELSE  Set Variable  ${day}
        ${month}   Convert Date    ${date}    result_format=%b
        ${month}   Convert To Upper Case    ${month}
        ${year}    Convert Date    ${date}    result_format=%Y
        Sleep    1
        Click Element    xpath://*[text()='As On']//ancestor::div[1]
        Click Element    xpath://button[@cdkarialive='polite']
        Click Element    xpath://div[text()=' ${year} ']
        Click Element    xpath://div[text()=' ${month} ']
        Click Element    xpath://div[text()=' ${day} ']
        Click Element    xpath://*[@icon="cogs"]
        Sleep  3s
        Log    Bureau report downloaded
        ${bureauReportPath}    11__getRecentFile.Get Most Recent Csv File
        ${reportDate}    Convert Date    ${date}    result_format=%Y-%m-%d
        Set Test Variable    ${reportDate}
        Comment   Bureau Last paid date test
        FOR  ${transNum}   IN RANGE     1    50
          ${lastPayDateRs}     Get Value From Json    ${jsonData}   transactions[${transNum}].date
         IF  ${transNum} != 1
          IF   ${lastPayDateRs} == [] and ${transNum} < ${2}
            ${lastPaidDate}    Set Variable           ${EMPTY}
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
            ${lastPaidDate}   Set Variable   '${lastPayDateRs2}${lastPayDateRs1}${lastPayDateRs0}
          END
          Run Keyword If      '${reportDate}' <= '${lastPaidDateRs}'    Exit For Loop
         ELSE
          ${lastPaidDate}   Set Variable    ${EMPTY}
         END
        END
        IF  ${transNum} == 1 or (${transNum} == 0 and ${fees1Rs} == [] and ${fees2Rs} == [])
          ${lastPaidDate}    Set Variable   ${EMPTY}
        END
        ${dateClosed}      Get Value From Json    ${jsonData}    timeline.closedOnDate
        IF  ${dateClosed} != []
         ${dateClosed}     Get From List        ${dateClosed}   0
         ${dateClosed0}    Set Variable         ${dateClosed[0]}
         ${dateClosed1}    Set Variable         ${dateClosed[1]}
         ${dateClosed2}    Set Variable         ${dateClosed[2]}
         ${dateClosed1}    Run Keyword If       ${dateClosed[1]} != "0" and ${dateClosed[1]} < 10    Set Variable    0${dateClosed[1]}    ELSE    Set Variable    ${dateClosed[1]}
         ${dateClosed2}    Run Keyword If       ${dateClosed[2]} != "0" and ${dateClosed[2]} < 10    Set Variable    0${dateClosed[2]}    ELSE    Set Variable    ${dateClosed[2]}
         ${dateClosed}     Set Variable         ${dateClosed0}-${dateClosed1}-${dateClosed2}
         ${dateClosed}     Run Keyword If   '${reportDate}' >= '${dateClosed}'   Set Variable  '${dateClosed2}${dateClosed1}${dateClosed0}  ELSE  Set Variable  ${EMPTY}
        ELSE
         ${dateClosed}     Set Variable          ${EMPTY}
        END 
        Comment   Bureau EMI amount test
        FOR    ${lt}    IN RANGE    1    ${tenure}
         ${dueDate}        Get Value From Json    ${jsonData}    repaymentSchedule.periods[${lt}].dueDate
         ${dueDate}        Get From List     ${dueDate}    0
         ${dueMonth}       Run Keyword If    ${dueDate[1]} != "0" and ${dueDate[1]} < 10    Set Variable    0${dueDate[1]}    ELSE    Set Variable    ${dueDate[1]}
         ${dueDay}         Run Keyword If    ${dueDate[2]} != "0" and ${dueDate[2]} < 10    Set Variable    0${dueDate[2]}    ELSE    Set Variable    ${dueDate[2]}
         ${dueYear}        Set Variable      ${dueDate[0]} 
         ${dueDate1}       Convert Date      ${dueYear}-${dueMonth}-${dueDay}    result_format=%Y-%m-%d
         IF  '${reportDate}' <= '${dueDate1}'
          ${lt}            Evaluate    ${lt} - 1
          ${emiRs}         Get Value From Json    ${jsonData}    repaymentSchedule.periods[${lt}].totalOriginalDueForPeriod 
          ${emiRs}         Get From List     ${emiRs}    0
          ${posRs}         Get Value From Json    ${jsonData}    repaymentSchedule.periods[${lt}].principalOriginalDue
          ${posRs}         Run Keyword If    ${lt} == 0  Set Variable   0   ELSE   Get From List    ${posRs}    0
          ${emiRs}         Run Keyword If    ${posRs} == 0  Set Variable  ${EMPTY}  ELSE  Set Variable  ${emiRs} 
          IF  ${lt} == 0 
            ${emiRs}         Get Value From Json    ${jsonData}    repaymentSchedule.periods[1].totalOriginalDueForPeriod 
            ${emiRs}         Get From List          ${emiRs}       0
            ${posRs}         Get Value From Json    ${jsonData}    repaymentSchedule.periods[1].principalOriginalDue
            ${posRs}         Get From List          ${posRs}       0
          END
          Exit For Loop
         END
        END
        Comment   Bureau DPD test
        FOR    ${te}    IN RANGE    1    ${tenure}
         ${nextDueDate}    Get Value From Json    ${jsonData}       repaymentSchedule.periods[${te}].dueDate
         ${nextDueDate}    Get From List          ${nextDueDate}    0
         ${nextDueMonth}     Run Keyword If    ${nextDueDate[1]} != "0" and ${nextDueDate[1]} < 10    Set Variable    0${nextDueDate[1]}    ELSE    Set Variable    ${nextDueDate[1]}
         ${nextDueDay}       Run Keyword If    ${nextDueDate[2]} != "0" and ${nextDueDate[2]} < 10    Set Variable    0${nextDueDate[2]}    ELSE    Set Variable    ${nextDueDate[2]}
         ${nextDueDueDate}   Convert Date      ${nextDueDate[0]}-${nextDueMonth}-${nextDueDay}   result_format=%Y-%m-%d 
         ${dueStatus}      Get Value From Json    ${jsonData}     repaymentSchedule.periods[${te}].complete
         ${dueStatus}      Get From List          ${dueStatus}    0
          IF  '${dueStatus}' == 'False'
           ${te}           Evaluate    ${te} - 1
          #  IF  '${nextDueDueDate}' >= '${reportDate}'
           IF  ${te} != 0
            ${dueCalDate}        Get Value From Json    ${jsonData}       repaymentSchedule.periods[${te}].dueDate
            ${dueCalDate}        Get From List          ${dueCalDate}    0
            ${dueCalMonth}       Run Keyword If    ${dueCalDate[1]} != "0" and ${dueCalDate[1]} < 10    Set Variable    0${dueCalDate[1]}    ELSE    Set Variable    ${dueCalDate[1]}
            ${dueCalDay}         Run Keyword If    ${dueCalDate[2]} != "0" and ${dueCalDate[2]} < 10    Set Variable    0${dueCalDate[2]}    ELSE    Set Variable    ${dueCalDate[2]}
            ${dueCalDate}        Convert Date      ${dueCalDate[0]}-${dueCalMonth}-${dueCalDay}   result_format=%Y-%m-%d
            ${lastOblMetDate}    Get Value From Json    ${jsonData}   repaymentSchedule.periods[${te}].obligationsMetOnDate
            IF  ${lastOblMetDate} != []
             ${lastOblMetDate}    Get From List          ${lastOblMetDate}    0
             ${lastOblMetMonth}   Run Keyword If         ${lastOblMetDate[1]} != "0" and ${lastOblMetDate[1]} < 10    Set Variable    0${lastOblMetDate[1]}    ELSE    Set Variable    ${lastOblMetDate[1]}
             ${lastOblMetDay}     Run Keyword If         ${lastOblMetDate[2]} != "0" and ${lastOblMetDate[2]} < 10    Set Variable    0${lastOblMetDate[2]}    ELSE    Set Variable    ${lastOblMetDate[2]}
             ${lastOblMetDate}    Convert Date           ${lastOblMetDate[0]}-${lastOblMetMonth}-${lastOblMetDay}   result_format=%Y-%m-%d 
            ELSE
             ${lastOblMetDate}    Set Variable   ${0}
            END
           ELSE
            ${dpd}    Subtract Date From Date    ${reportDate}  ${nextDueDueDate}   verbose
           END
           Comment   Current POS test
           ${principalPaidRs}    Evaluate    ${te} + ${1}
           ${principalPaidRs}   Get Value From Json    ${jsonData}    repaymentSchedule.periods[${principalPaidRs}].principalPaid
           ${currentBalance}    Get Value From Json    ${jsonData}    repaymentSchedule.periods[${te}].principalLoanBalanceOutstanding
           IF  ${currentBalance} != []
            ${principalPaidRs}  Run Keyword If   ${principalPaidRs} != []  Get From List    ${principalPaidRs}  0   ELSE    Set Variable  ${0}
            IF  '${reportDate}' < '${nextDueDueDate}'
             ${currentBalance}   Get From List    ${currentBalance}   0
            ELSE
             ${currentBalance}   Run Keyword If   '${principalPaidRs}' != '0'  Evaluate   ${currentBalance} - ${principalPaidRs}   ELSE    Set Variable    ${currentBalance}
            END
           ELSE
            ${currentBalance}   Set Variable    ${principal}
           END
          ELSE
           ${te}           Evaluate    ${te} - 1 
           IF  ${te} != 0
            ${dueCalDate}        Get Value From Json    ${jsonData}       repaymentSchedule.periods[${te}].dueDate
            ${dueCalDate}        Get From List          ${dueCalDate}    0
            ${dueCalMonth}       Run Keyword If    ${dueCalDate[1]} != "0" and ${dueCalDate[1]} < 10    Set Variable    0${dueCalDate[1]}    ELSE    Set Variable    ${dueCalDate[1]}
            ${dueCalDay}         Run Keyword If    ${dueCalDate[2]} != "0" and ${dueCalDate[2]} < 10    Set Variable    0${dueCalDate[2]}    ELSE    Set Variable    ${dueCalDate[2]}
            ${dueCalDate}        Convert Date      ${dueCalDate[0]}-${dueCalMonth}-${dueCalDay}   result_format=%Y-%m-%d
            ${lastOblMetDate}    Get Value From Json    ${jsonData}   repaymentSchedule.periods[${te}].obligationsMetOnDate
            IF  ${lastOblMetDate} != []
             ${lastOblMetDate}    Get From List          ${lastOblMetDate}    0
             ${lastOblMetMonth}   Run Keyword If         ${lastOblMetDate[1]} != "0" and ${lastOblMetDate[1]} < 10    Set Variable    0${lastOblMetDate[1]}    ELSE    Set Variable    ${lastOblMetDate[1]}
             ${lastOblMetDay}     Run Keyword If         ${lastOblMetDate[2]} != "0" and ${lastOblMetDate[2]} < 10    Set Variable    0${lastOblMetDate[2]}    ELSE    Set Variable    ${lastOblMetDate[2]}
             ${lastOblMetDate}    Convert Date           ${lastOblMetDate[0]}-${lastOblMetMonth}-${lastOblMetDay}   result_format=%Y-%m-%d 
            ELSE
             ${lastOblMetDate}    Set Variable   ${0}
            END
            #  ${lastOblMetDate}    Run Keyword If   '${dueCalDate}' < '${lastOblMetDate}'  Set Variable  ${lastOblMetDate}  ELSE  Set Variable  ${dueCalDate}
           ELSE
            ${dpd}               Set Variable    ${EMPTY}
            # ${lastOblMetDate}    Set Variable    ${0}
           END
           Comment   Current POS test
           ${principalPaidRs}    Evaluate    ${te} + ${1}
           ${principalPaidRs}   Get Value From Json    ${jsonData}    repaymentSchedule.periods[${principalPaidRs}].principalPaid
           ${currentBalance}    Get Value From Json    ${jsonData}    repaymentSchedule.periods[${te}].principalLoanBalanceOutstanding
           IF  ${currentBalance} != [] or '${reportDate}' < '${firstRepayOn}'
            ${principalPaidRs}  Run Keyword If   ${principalPaidRs} != []  Get From List    ${principalPaidRs}  0   ELSE    Set Variable  ${0}
            IF  '${reportDate}' < '${nextDueDueDate}'
             ${currentBalance}   Get From List    ${currentBalance}   0
            ELSE
             ${currentBalance}   Run Keyword If   '${principalPaidRs}' != '0'  Evaluate   ${currentBalance} - ${principalPaidRs}   ELSE    Set Variable    ${currentBalance}
            END
           ELSE
            ${currentBalance}   Set Variable    ${principal}
           END
          END
          # Exit For Loop
         Run Keyword If     '${dueStatus}' == 'False'               Exit For Loop
         Run Keyword If     '${reportDate}' <= '${nextDueDueDate}'  Exit For Loop
        END
        IF   '${nextDueDueDate}' >= '${reportDate}' <= '${firstRepayOn}'      
         ${dpd}    Set Variable    ${EMPTY}
        ELSE IF   '${nextDueDueDate}' > '${reportDate}' >= '${firstRepayOn}' 
         IF  '${lastOblMetDate}' <= '${dueCalDate}'
          # Advancely paid the due so DPD is 0
          ${dpd}    Set Variable    ${0}
         ELSE
          # paid the due on late so taking lastOblMetDate
          ${dpd}    Subtract Date From Date  ${reportDate}   ${lastOblMetDate}    verbose
         END
        ELSE IF   '${nextDueDueDate}' < '${reportDate}' >= '${firstRepayOn}'
         ${dpd}    Subtract Date From Date  ${reportDate}   ${nextDueDueDate}    verbose
        ELSE IF   '${nextDueDueDate}' == '${reportDate}'
         ${dpd}    Set Variable    ${0}
        END
        IF  '${dpd}' != '${EMPTY}' and '${dpd}' != '${0}'
         IF  '${dpd}' == '1 day'
          ${dpd}    Get Substring        ${dpd}    0    -4
         ELSE
          ${dpd}    Get Substring        ${dpd}    0    -5
         END
         ${dpdRs}   Convert To Integer   ${dpd}
        END
        ${odAmountRs}     Get Value From Json    ${jsonData}    summary.totalOverdue
        ${odAmountRs}     Get From List          ${odAmountRs}  0
        ${odSinceRs}      Get Value From Json    ${jsonData}    summary.overdueSinceDate
        ${status}         Get Value From Json    ${jsonData}    status.value
        ${status}         Get From List          ${status}      0
        ${result}          19__bureaReport.Search Bureau Csv By LoanAccountNumber  ${bureauReportPath}  ${clientName}  ${dob}  ${gender}  ${pan}  ${passport}  ${voterId}  ${DL}  ${mobNum}  ${email}  ${address}  ${stateCode1}  ${pinCode}   ${disburseDate}  ${lastPaidDate}  ${dateClosed}   ${emiRs}   ${dateReported}  ${principal}  ${interest}  ${tenure}  ${accType}  ${assetClassify}  ${writeOffAmount}  ${writeOffPriAmount}  ${loanAccNo}  ${currentBalance}  ${dpd}  ${odAmountRs}  ${status}
        Run Keyword If    '${result}' != 'PASS'    Run Keyword And Continue On Failure     Log   Loop number ${index} failed!
        Run Keyword If    '${result}' != 'PASS'    Append To List    ${failed_loanId}    ${index}=${DATES}
      END 
      FOR   ${elem}    IN   @{DATES}
        Remove From List    ${DATES}   0
      END       
    END
    Run Keyword If    '${failed_loanId}' != []    Set Test Message    The following Loan id's failed: ${failed_loanId}
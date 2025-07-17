*** Settings ***
Resource     ../keywords/common.robot
Resource    ../PageObjects/01__loginPage.robot
Resource    ../testcases/11__getExcelTemp.robot
Resource    ../PageObjects/24__mysqlConnect.robot
Library     15__disReport.py
# Library   15__disReport1.py

*** Variables ***
${processFeesGSTMethod}    1    # 1-Inclusive  2-Exclusive
${insChargesGSTMethod}     1

*** Keywords ***
Download Disbursement report
    [Documentation]    Download the Disbursement report
    Set Selenium Speed    0.5s
    Click Element    xpath://a[@href='#/reports']
    Sleep    1
    Click Element    xpath://td[text()=' Disbursement Report ']
    Sleep    1
    Click Element    xpath://*[text()='startDate']//ancestor::div[1]
    Click Element    xpath://button[@cdkarialive='polite']
    Click Element    xpath://div[text()=' 2020 ']
    Click Element    xpath://div[text()=' JAN ']
    Click Element    xpath://div[text()=' 1 ']
    Click Element    xpath://*[text()='endDate']//ancestor::div[1]
    ${currDay}       Get Time	return day
    ${currDay}       Run Keyword If    "${currDay[0]}" == "0"    Evaluate    "${currDay[1:]}"    ELSE    Set Variable    ${currDay}
    Click Element    xpath://div[text()=' ${currDay} ']
    Click Element    xpath:(//mat-select[@role='combobox'])[1]
    Click Element    xpath://*[contains(text(),'${partner}')]
    Click Element    xpath://*[@icon="cogs"]
    Log    Disbursement report downloaded for ${partner}
    Sleep    4s
    
Download Disbursement report using API
  ${partnerId}   Get From Dictionary    ${partnerId}    key=${product}
  ${currDay}     Get Current Date     result_format=%d
  ${currMonth}   Get Current Date     result_format=%B
  create session     getDisReport     ${baseUrl.QA}
  ${header}     Create Dictionary    Authorization=Basic YWRtaW46cGFzc3dvcmQ=     Fineract-Platform-TenantId=default  Content-Type=application/json
  ${Rurl}       Set Variable         /lms/api/v1/runreports/Disbursement%20Report?R_startDate=01%20January%202022&R_endDate=${currDay}%20${currMonth}%202024&R_partnerId=${partnerId}&locale=en&dateFormat=dd%20MMMM%20yyyy&exportCSV=true
  ${response}   GET On Session       getDisReport      ${Rurl}     headers=${header}
  ${currDay}       Get Time  
  ${currDay}       Replace String      ${currDay}        :    ${EMPTY}
  Create Binary File    ${CURDIR}//testData//Disbursement_Report_${currDay}.csv      ${response.content}

Check in disbursement report 
    [Documentation]    Check in disbursement report values with bulk loan excel values
    ${disReportpath}   11__getRecentFile.Get Most Recent Csv File
    Set Global Variable    ${disReportpath}
    ${month_days}      Create Dictionary     1=31    2=28    3=31    4=30    5=31    6=30   7=31    8=31    9=30    10=31    11=30    12=31
    ${failed_loops}    Create List
    ${failed_loops1}   Create List
    # Take values from Bulk loan excel file
   FOR    ${index}    IN RANGE    ${startLoanId}      ${endLoanId}
    ${rowNo}        Evaluate          (${index}-${startLoanId}+2)
    Open Workbook   ${bulkLoanFile}
    ${exId}         Read From Cell    B${rowNo}
    ${firstName}    Read From Cell    F${rowNo}
    ${middleName}   Read From Cell    G${rowNo}
    ${lastName}     Read From Cell    H${rowNo}
    IF  '${middleName}'=='None'
       ${clientName}   Set Variable      ${firstName} ${lastName}
    ELSE
       ${clientName}   Set Variable      ${firstName} ${middleName} ${lastName} 
    END
    ${principal}    Read From Cell    AH${rowNo}
    ${tenure}       Read From Cell    AI${rowNo}
    ${dueDay}       Read From Cell    AK${rowNo}
    ${interest}     Read From Cell    AL${rowNo}
    ${disburseDate}  Read From Cell   AG${rowNo}
    ${disburseDate}    Convert Date   ${disburseDate}    result_format=%d-%m-%Y
    ${1stRepay}     Read From Cell    AJ${rowNo}
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
    ${maturesOn}              Set Variable      ${dueDay2}-${maturesOnMonth}-${year}
    ${processFees}            Read From Cell    AM${rowNo}
    # If processFees & Insurance charges empty ,while computing its getting failed so set to zero
    IF  '${processFees}' == 'None'
      ${processFees}    Set Variable    0
    END
    ${processFees}         Evaluate       float(${processFees})
    ${processFeeAmountEx}  Set Variable   ${processFees}
    IF  ${processFeesGSTMethod} == ${1}
     IF  '${partner}' == 'Seeds'
      ${processFeeAmountEx}    Evaluate         round(${processFees}*${100}/${118},0) 
     ELSE
      ${processFeeAmountEx}    Evaluate         round(${processFees}*${100}/${118},2)
     END
      ${processFeeGSTAmount}   Evaluate         ${processFees}-${processFeeAmountEx} 
    ELSE IF  ${processFeesGSTMethod} == ${2}
      ${processFeeGSTAmount}   Evaluate         round(${processFees}*${18}/${100},2)
    END
    IF  '${partner}' != 'Seeds' and '${partner}' != 'Navdhan'
      ${insCharges}         Read From Cell    AN${rowNo}
      IF  '${insCharges}' == 'None'
       ${insCharges}     Set Variable    0
      END
      ${insCharges}                 Evaluate        float(${insCharges})
      ${insChargesAmountEx}         Set Variable    ${insCharges}
      IF  ${insChargesGSTMethod} == ${1}
        ${insChargesAmountEx}       Evaluate        round(${insCharges}*${100}/${118},2)
        ${insChargesGSTAmount}      Evaluate        ${insCharges}-${insChargesAmountEx}
      ELSE IF  ${insChargesGSTMethod} == ${2}
        ${insChargesAmountEx}       Evaluate        round(${insCharges}*${18}/${100},2)
        ${insChargesGSTAmount}      Evaluate        round(${insChargesAmountEx}*${18}/${100},2)
      END 
      ${insLifeCover}            Set Variable    ${0}
      ${insLifeCoverEx}          Set Variable    ${0}
      ${insLifeCoverGSTAmount}   Set Variable    ${0}
    ELSE
      ${insLifeCover}           Read From Cell    AN${rowNo}
      ${insLifeCoverEx}         Set Variable      ${insLifeCover}
      ${insLifeCoverGSTAmount}  Set Variable      ${0}
      ${insCharges}             Set Variable      ${0}
      ${insChargesAmountEx}     Set Variable      ${0}
      ${insChargesGSTAmount}    Set Variable      ${0}
    END
    IF  '${partner}' == 'Seeds'
      ${stampDuty}              Read From Cell    AO${rowNo}
      ${insHospicash}           Read From Cell    AP${rowNo}
      ${insHospicashGSTAmount}  Set Variable      ${0}
    ELSE
      ${stampDuty}               Set Variable    ${0} 
      ${insHospicash}            Set Variable    ${0}
      ${insHospicashGSTAmount}   Set Variable    ${0}
    END
    ${totalChargesDeducted}   Evaluate          round(${processFeeAmountEx}+${insChargesAmountEx}+${stampDuty}+${insLifeCoverEx}+${insHospicash},2)
    ${totalGSTDeducted}       Evaluate          round(${processFeeGSTAmount}+${insChargesGSTAmount}+${insLifeCoverGSTAmount}+${insHospicashGSTAmount},2)
    ${netDisAmount}           Evaluate          round(${principal}-(${totalChargesDeducted}+${totalGSTDeducted}),2)
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
    ${loanAccNoRs}     Get Value From Json    ${jsonData}    accountNo
    ${exIdRs}          Get Value From Json    ${jsonData}    externalId
    ${partnerIdRs}     Get Value From Json    ${jsonData}    partner.id
    ${TOARs}           Get Value From Json    ${jsonData}    summary.totalOutstanding
    ${XIRRRs}          Get Value From Json    ${jsonData}    xirrValue
    ${vcplHurdleRs}    Get Value From Json    ${jsonData}    servicerFeeData.vclHurdleRate
    ${transTypeRs}     Get Value From Json    ${jsonData}    bankTranscationData
    ${pennyStatusRs}   Get Value From Json    ${jsonData}    bankTranscationData.pennyDropTransaction[0].action
    ${UTRnumRs}        Get Value From Json    ${jsonData}    bankTranscationData.disbursementTransaction[0].utr
    ${loanAccNoRs}     Get From List          ${loanAccNoRs}    0
    ${partnerIdRs}     Get From List          ${partnerIdRs}    0
    ${TOARs}           Get From List          ${TOARs}          0
    IF  '${partner}' == 'Dvara'
      ${vcplHurdleRs}    Set Variable    ${EMPTY}
    ELSE
      ${vcplHurdleRs}    Get From List   ${vcplHurdleRs}   0
    END
    IF  ${transTypeRs} != [{}]
      ${transTypeRs}     Get From List   ${transTypeRs}    0
      ${pennyStatusRs}   Get From List   ${pennyStatusRs}  0
      ${UTRnumRs}        Get From List   ${UTRnumRs}       0
    ELSE
      ${transTypeRs}     Set Variable    ${EMPTY}
      ${pennyStatusRs}   Set Variable    ${EMPTY}
      ${UTRnumRs}        Set Variable    ${EMPTY}
    END 
    ${statusRs}          Get Value From Json    ${jsonData}    status.value
    ${statusRs}          Get From List          ${statusRs}    0
    IF  '${statusRs}' == 'Closed (obligations met)'
     ${statusRs}         Set Variable           Closed
    ELSE IF  '${statusRs}' == 'foreclosed'
     ${statusRs}         Set Variable           Foreclosed
    END
    24__mysqlConnect.QA Mysql DB connection
    ${xirrDB}    Query   select xirr_value from m_loan_xirr_history_details where loan_event=1 and loan_id=${index}
    ${xirrDB}    Convert To String    ${xirrDB}
    ${xirrDB}    Get Substring   ${xirrDB}  11     -6  
    ${result}    15__disReport.Search Csv By External Id    ${disReportpath}  ${partnerIdRs}  ${exId}  ${loanAccNoRs}  ${clientName}  ${partner}   ${principal}  ${tenure}  ${interest}  ${index}  ${TOARs}  ${xirrDB}  ${vcplHurdleRs}  ${transTypeRs}  ${pennyStatusRs}  ${UTRnumRs}  ${1stRepay}  ${processFees}  ${insCharges}  ${maturesOn}  ${totalChargesDeducted}  ${totalGSTDeducted}  ${netDisAmount}  ${insLifeCover}  ${insHospicash}  ${stampDuty}  ${disburseDate}  ${statusRs}
    Run Keyword If    '${result}' != 'PASS'    Run Keyword And Continue On Failure     Log   Loop number ${index} failed!
    Run Keyword If    '${result}' != 'PASS'    Append To List    ${failed_loops}    ${index}    
   END
   Run Keyword If    '${failed_loops}' != []    Set Test Message    The following loops failed: ${failed_loops}

Check XIRR and Total outstanding from dispursement report
    [Documentation]   In dispursement report check XIRR & Total outstanding amount from UI
   ${failed_loops1}    Create List
   ${failed_loops2}    Create List
   01__loginPage.Open Browser and login to Zeus  ${userName}        ${password}
   FOR    ${index}    IN RANGE    1      ${loanCount}
    Open Workbook    ${bulkLoanFile}
    ${exId}         Read From Cell    B${index+1}
    ${firstName}    Read From Cell    F${index+1}
    ${middleName}    Read From Cell    G${index+1}
    ${lastName}     Read From Cell    H${index+1}
    ${clientName}   Set Variable      ${firstName} ${middleName} ${lastName}
    ${mobile_no}    Read From Cell    S${index+1}
    Close Workbook
    ${loanId}            15__disReport1.Search LoanId                     ${disReportpath}  ${exId}   
    ${XIRR}              15__disReport1.Search XIRR                       ${disReportpath}  ${exId}   
    ${totalOutstanding}  15__disReport1.Search Total Outstanding Amount   ${disReportpath}  ${exId}   
    Click Element    xpath://a[@href='#/clients']
    # Sleep    0.2
    # Click Element    xpath://mat-select[@aria-label='Items per page:']
    # Sleep    0.2
    # Click Element    xpath://span[text()=' 100 ']
    Sleep    0.5
    Click Element    xpath://button[@class='mat-focus-indicator mat-tooltip-trigger mat-paginator-navigation-last mat-icon-button mat-button-base ng-star-inserted']
    Sleep    0.5
    ${result}=    Run Keyword And Return Status   Element Should Be Visible    xpath://tr/td[contains(text(),'${clientName}')]/following::td[contains(text(),'${mobile_no}')]
    ${i}=    Set Variable    1
    Sleep    1
    WHILE  ${result}==False
        Click Element    xpath://button[@aria-label='Previous page']
        Sleep    1
        ${result}=    Run Keyword And Return Status   Element Should Be Visible    xpath://tr/td[contains(text(),'${clientName}')]/following::td[contains(text(),'${mobile_no}')]
        ${i}=    Evaluate    ${i}+1
        IF    ${i}==50    BREAK
    END
    Sleep    2
    Click Element    xpath://tr/td[contains(text(),'${clientName}')]/following::td[contains(text(),'${mobile_no}')]
    Sleep    2
    Click Element    xpath://td[text()=' ${loanId} ']
    Sleep    2.5
    Click Element    xpath://a[text()=' Repayment Schedule ']
    ${rpsTotOutstanding}   Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-outstanding mat-column-outstanding ng-star-inserted']
    ${rpsTotOutstanding}    Remove String    ${rpsTotOutstanding}    ,
    Run Keyword If    '${rpsTotOutstanding}' == '${totalOutstanding}'    Log    Total outstanding amount ${rpsTotOutstanding} matches with CSV
    Run Keyword If    '${rpsTotOutstanding}' != '${totalOutstanding}'    Run Keyword And Continue On Failure     Log   Loop number ${index} failed!
    Run Keyword If    '${rpsTotOutstanding}' != '${totalOutstanding}'    Append To List    ${failed_loops1}    ${index}
    Sleep    1
    Click Element    xpath://a[text()=' Account Details ']
    ${rpsXIRR}   Get Text    xpath:(//SPAN[@fxflex='50%'])[4]
    Run Keyword If    '${rpsXIRR}' == '${XIRR}'    Log    XIRR ${rpsXIRR} matches with CSV
    Run Keyword If    '${rpsXIRR}' != '${XIRR}'    Run Keyword And Continue On Failure     Log   Loop number ${index} failed!
    Run Keyword If    '${rpsXIRR}' != '${XIRR}'    Append To List    ${failed_loops2}    ${index}
   END
   Run Keyword If    '${failed_loops1}' != []    Set Test Message    The following loops failed due to Incorrect Total outstanding amount: ${failed_loops1}
   Run Keyword If    '${failed_loops2}' != []    Set Test Message    The following loops failed due to Incorrect XIRR: ${failed_loops2}
*** Settings ***
Resource    ../keywords/common.robot

*** Variables ***
${expCode}            200
${myState}            Tamil Nadu
${startLoanId}        926
${endLoanId}          930
${bulkLoanFile}       D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads//Dvara//300823//Dvara&NOCPL_300823.xlsx

*** Keywords ***
RS Due date Test
    [Documentation]    Check the RS Due dates using Bulk loan excel file and RS Api
    # Run only successfully created loans ,in ${bulkLoanFile} put success loan only remove failure loans
    ${month_dict}      Create Dictionary     January=1     February=2     March=3     April=4      May=5     June=6     July=7      August=8     September=9     October=10      November=11      December=12
    ${month_dict1}     Create Dictionary     ${1}=January  ${2}=February  ${3}=March  ${4}=April   ${5}=May  ${6}=June  ${7}=July   ${8}=August  ${9}=September  ${10}=October   ${11}=November   ${12}=December
    ${month_days}      Create Dictionary     ${1}=31    ${2}=28    ${3}=31    ${4}=30    ${5}=31    ${6}=30   ${7}=31    ${8}=31    ${9}=30    ${10}=31    ${11}=30    ${12}=31
    ${failedLoanList}  Create List
    ${RSList}          Create List
    ${currDate}        Get Current Date    result_format=%Y-%m-%d
   FOR    ${index}   IN RANGE    ${startLoanId}   ${endLoanId}
    Open Workbook    ${bulkLoanFile}
    ${rowNo}          Evaluate          str(${index}-${startLoanId}+2)
    ${exId1}          Read From Cell    B${rowNo}
    ${tenure}         Read From Cell    AI${rowNo}
    ${tenure}         Evaluate          ${tenure} + 1
    ${disDateXl}      Read From Cell    AG${rowNo}
    ${disDateXl}      Convert Date      ${disDateXl}    result_format=%d %B %Y
    ${principalXl}    Read From Cell    AH${rowNo}
    ${1stDueXl}       Read From Cell    AJ${rowNo}
    ${1stDueXl}       Convert Date      ${1stDueXl}     result_format=%d %B %Y
    ${dueDayXl1}      Read From Cell    AK${rowNo}
    ${dueDayXl1}      Evaluate          str(${dueDayXl1})
    ${dueDayXl}       Run Keyword If    ${dueDayXl1[0]} != "0" and ${dueDayXl1} < 10    Set Variable    0${dueDayXl1[0]}    ELSE    Set Variable    ${dueDayXl1}
    ${interestXl}     Read From Cell    AL${rowNo}
    Set Global Variable    ${dueDayXl}
    Close Workbook
    ${dueDateList}            Create List
    create session     RPS     ${baseUrl}
    ${header}     Create Dictionary    Authorization=Basic YWRtaW46cGFzc3dvcmQ=     Fineract-Platform-TenantId=default  Content-Type=application/json
    ${Rurl}       Set Variable         /lms/api/v1/loans/${index}?associations=all&exclude=guarantors,futureSchedule
    ${response}   GET On Session       RPS      ${Rurl}     headers=${header} 
    ${jsonData}   Convert String To Json        ${response.content}
    ${clientNameRs}          Get Value From Json    ${jsonData}    clientName
    ${productNameRs}         Get Value From Json    ${jsonData}    loanProductName
    ${exIdRs}                Get Value From Json    ${jsonData}    externalId
    ${principalRs}           Get Value From Json    ${jsonData}    principal
    ${interestRs}            Get Value From Json    ${jsonData}    annualInterestRate
    ${noOfRepayRs}           Get Value From Json    ${jsonData}    numberOfRepayments
    FOR    ${i}    IN RANGE    0    ${tenure}
     ${fromDate}              Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].fromDate
     ${dueDate}               Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].dueDate
     IF  ${i}!=0
        #  Taking principal & Interest here bz in i=oth place no Principal & Interest
        ${dueDateYr}      Get From List     ${dueDate}    0
        ${dueDateYr}      Get From List     ${dueDateYr}  0
        ${daysInYear}     Run Keyword If    ${dueDateYr}==2020 or ${dueDateYr}==2024   Set Variable     ${366}    ELSE   Set Variable   ${365}
        ${fromDate}       Get From List     ${fromDate}    0
        ${fromMonth}      Run Keyword If    ${fromDate[1]} != "0" and ${fromDate[1]} < 10    Set Variable    0${fromDate[1]}    ELSE    Set Variable    ${fromDate[1]}
        ${fromDay}        Run Keyword If    ${fromDate[2]} != "0" and ${fromDate[2]} < 10    Set Variable    0${fromDate[2]}    ELSE    Set Variable    ${fromDate[2]}
        ${fromDate}       Convert Date      ${fromDate[0]}-${fromMonth}-${fromDay}   result_format=%Y-%m-%d
        ${dueDate}        Get From List     ${dueDate}    0
        ${month}          Run Keyword If    ${dueDate[1]} != "0" and ${dueDate[1]} < 10    Set Variable    0${dueDate[1]}    ELSE    Set Variable    ${dueDate[1]}
        ${day}            Run Keyword If    ${dueDate[2]} != "0" and ${dueDate[2]} < 10    Set Variable    0${dueDate[2]}    ELSE    Set Variable    ${dueDate[2]}
        ${year}           Set Variable      ${dueDate[0]} 
        ${dueDate1}       Convert Date      ${year}-${month}-${day}    result_format=%d-%m-%Y
        Log Many    ${dueDate1}    ${fromDate}
        Append To List     ${dueDateList}    ${dueDate1}
     END    
    END
    Set Test Message    ${dueDateList}
    Set Test Message    ${RsList}
    ${disDateRS}       Get Value From Json    ${jsonData}    repaymentSchedule.periods[0].dueDate
    ${disDateRS}       Get From List          ${disDateRs}   0
    ${disDate}         Get From List          ${disDateRs}   2
    ${disDate}         Evaluate               str(${disDate})
    ${disMonth}        Get From List          ${disDateRs}   1
    ${disMonth}        Evaluate               str(${disMonth})
    ${disYear}         Get From List          ${disDateRs}   0
    ${disMonth}        Run Keyword If    ${disMonth} != "0" and ${disMonth} < 10    Set Variable    0${disMonth}    ELSE    Set Variable    ${disMonth}
    ${disDate}         Run Keyword If    ${disDate} != "0" and ${disDate} < 10      Set Variable    0${disDate}     ELSE    Set Variable    ${disDate}
    ${disDateRS}       Convert Date      ${disYear}-${disMonth}-${disDate}    result_format=%d %B %Y
    Should Be Equal    ${disDateRS}      ${disDateXl}
    ${1stDueRS}        Get From List     ${dueDateList}    0
    ${1stDueRS}        Split String      ${1stDueRS}       -
    ${1stDueDateRs}    Get From List     ${1stDueRS}       0
    ${1stDueMonthRs}   Get From List     ${1stDueRS}       1
    ${1stDueyearRs}    Get From List     ${1stDueRS}       2
    ${1stDueRS}        Convert Date      ${1stDueyearRs}-${1stDueMonthRs}-${1stDueDateRs}    result_format=%d %B %Y
    Should Be Equal    ${1stDueRS}       ${1stDueXl}
    ${1stDueXl}        Split String      ${1stDueXl}       
    ${1stDueDate}      Set Variable      ${1stDueXl}[0]
    ${1stDueMonth}     Set Variable      ${1stDueXl}[1]
    ${year}            Set Variable      ${1stDueXl}[2]
    FOR    ${i}    IN RANGE    2    ${tenure}
     ${getDueIndex}    Evaluate     ${i} - 1
     ${a}    Evaluate    ${i} - 1
     ${MonthNum}     Set Variable     ${month_dict}[${1stDueMonth}]
     ${MonthNum}     Evaluate         ${MonthNum} + ${a}
    IF   ${MonthNum} == 13 or ${MonthNum} == 25 or ${MonthNum} == 37
      ${year}       Evaluate   ${year}+${1}
    END
    IF   12 < ${MonthNum} <= 24 
      ${MonthNum}   Evaluate   ${MonthNum} - ${12}
    END
    IF   24 < ${MonthNum} <= 36
      ${MonthNum}   Evaluate   ${MonthNum} - ${24}
    END
    IF   36 < ${MonthNum} <= 48
      ${MonthNum}   Evaluate   ${MonthNum} - ${36}
    END
    ${DueMonth}     Set Variable      ${month_dict1}[${MonthNum}]
    ${DueRS}        Get From List     ${dueDateList}    ${getDueIndex}
    ${DueRS}        Split String      ${DueRS}       -
    ${DueDateRs}    Get From List     ${DueRS}       0
    ${DueMonthRs}   Get From List     ${DueRS}       1
    ${DueyearRs}    Get From List     ${DueRS}       2
    ${DueRS}        Convert Date      ${DueyearRs}-${DueMonthRs}-${DueDateRs}    result_format=%d %B %Y
    ${max_days}     Set Variable      ${month_days}[${MonthNum}]
    # Run Keyword If     ${MonthNum} == 2 and (${year} % 4 == 0 and (${year} % 100 != 0 or ${year} % 400 == 0))    Set Variable    ${max_days}    29
    ${dueDayXl2}      Set Variable     ${dueDayXl}
    ${dueDayXl2}      Run Keyword If   ${dueDayXl1} > ${max_days}    Set Variable    ${max_days}   ELSE   Set Variable   ${dueDayXl}
    ${dueDayXl2}      Run Keyword If   (${year} == ${2020} or ${year} == ${2024}) and (${MonthNum} == ${2} and ${dueDayXl1} > ${28})   Set Variable    29   ELSE   Set Variable   ${dueDayXl2}
    ${DueXl}          Set Variable     ${dueDayXl2} ${DueMonth} ${year}
    Should Be Equal   ${DueRS}         ${DueXl}
    END
   END






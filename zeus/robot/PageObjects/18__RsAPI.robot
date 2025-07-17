*** Settings ***
Resource    ../keywords/common.robot

*** Variables ***
${myState}       Tamil Nadu

*** Keywords ***
RS ALL Test
    [Documentation]    Check the RS using Bulk loan excel file and RS Api
    # Run only successfully created loans ,in ${bulkLoanFile} put success loan only remove failure loans
    ${month_dict}      Create Dictionary     January=1     February=2     March=3     April=4      May=5     June=6     July=7      August=8     September=9     October=10      November=11      December=12
    ${month_dict1}     Create Dictionary     ${1}=January  ${2}=February  ${3}=March  ${4}=April   ${5}=May  ${6}=June  ${7}=July   ${8}=August  ${9}=September  ${10}=October   ${11}=November   ${12}=December
    ${month_days}      Create Dictionary     ${1}=31    ${2}=28    ${3}=31    ${4}=30    ${5}=31    ${6}=30   ${7}=31    ${8}=31    ${9}=30    ${10}=31    ${11}=30    ${12}=31
    ${failedLoanList}  Create List
    ${RSList}          Create List
    ${currDate}        Get Current Date    result_format=%Y-%m-%d
    ${chargesXlList}          Create List
    ${chargeApiNames}         Create List
    ${chargeCalTypeList}      Create List
    ${chargeAmountList}       Create List
    ${chargeFeeAmountList}    Create List
    ${chargeGSTEnableList}    Create List
    ${chargeGSTEnableList}    Create List
    ${chargeIgstList}         Create List
    ${chargeCgstList}         Create List
    ${chargeSgstList}         Create List
    ${chargeSelfShareList}    Create List
    ${chargePartnerGSTList}   Create List
    ${chargeTotalGSTList}     Create List
    ${chargeGstTypeList}      Create List
    ${roundVal}    Run Keyword If  '${partner}' == 'Seeds'    Set Variable    0   ELSE  Set Variable    2
    Open Workbook   ${bulkLoanFile}
    ${charge1Xl}    Read From Cell     AM${1}
    ${charge2Xl}    Read From Cell     AN${1}
    ${charge3Xl}    Read From Cell     AO${1}
    ${charge4Xl}    Read From Cell     AP${1}
    ${charge5Xl}    Read From Cell     AQ${1}
    Close Workbook
    Append To List   ${chargesXlList}   ${charge1Xl}  ${charge2Xl}  ${charge3Xl}  ${charge4Xl}
    ${ChargeCount}    Run Keyword If  '${partner}' == 'Seeds'  Set Variable  ${4}    ELSE    Set Variable   ${2}
    ${productId}    Get From Dictionary    ${productId}    key=${product}
   FOR    ${index}   IN RANGE    ${startLoanId}   ${endLoanId}
    Open Workbook     ${bulkLoanFile}
    ${rowNo}          Evaluate          str(${index}-${startLoanId}+2)
    ${exId1}          Read From Cell    B${rowNo}
    ${stateXl}        Read From Cell    V${rowNo}
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
    ${charge1XlVal}  Read From Cell     AM${rowNo}
    ${charge2XlVal}   Read From Cell    AN${rowNo}
    ${charge3XlVal}   Read From Cell    AO${rowNo}
    ${charge4XlVal}   Read From Cell    AP${rowNo}
    ${charge5XlVal}   Read From Cell    AQ${rowNo}
    Set Global Variable    ${dueDayXl}
    Close Workbook
    ${dueDateList}         Create List
    create session     RPS     ${baseUrl.QA}
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
    ${approvePrincipal}      Get Value From Json    ${jsonData}    approvedPrincipal
    ${proposePrincipal}      Get Value From Json    ${jsonData}    proposedPrincipal
    ${netDisAmountRs}        Get Value From Json    ${jsonData}    netDisbursalAmount
    ${netSelfDisAmountRs}    Get Value From Json    ${jsonData}    netSelfDisbursalAmount
    ${netParDisAmountRs}     Get Value From Json    ${jsonData}    netPartnerDisbursalAmount
    ${netParDisAmountRs}     Get Value From Json    ${jsonData}    daysInMonthType.value
    ${netParDisAmountRs}     Get Value From Json    ${jsonData}    daysInYearType.id
    ${matureMonth}           Evaluate               ${tenure} - 1
    ${matureDate}            Get Value From Json    ${jsonData}    repaymentSchedule.periods[${matureMonth}].dueDate
    ${expMatureDate}         Get Value From Json    ${jsonData}    timeline.expectedMaturityDate
    ${principalRs}           Get From List          ${principalRs}         0
    ${interestRs}            Get From List          ${interestRs}          0
    ${noOfRepayRs}           Get From List          ${noOfRepayRs}         0
    ${approvePrincipal}      Get From List          ${approvePrincipal}    0
    ${proposePrincipal}      Get From List          ${proposePrincipal}    0
    ${netDisAmountRs}        Get From List          ${netDisAmountRs}      0
    ${netSelfDisAmountRs}    Get From List          ${netSelfDisAmountRs}  0
    ${matureDate}            Get From List          ${matureDate}          0
    ${matureMonth}           Run Keyword If         ${matureDate[1]} != "0" and ${matureDate[1]} < 10    Set Variable    0${matureDate[1]}    ELSE    Set Variable    ${matureDate[1]}
    ${matureDay}             Run Keyword If         ${matureDate[2]} != "0" and ${matureDate[2]} < 10    Set Variable    0${matureDate[2]}    ELSE    Set Variable    ${matureDate[2]}
    ${matureYear}            Set Variable           ${matureDate[0]} 
    ${matureDate}            Convert Date           ${matureYear}-${matureMonth}-${matureDay}    result_format=%Y-%m-%d
    ${expMatureDate}         Get From List          ${expMatureDate}          0
    ${expMatureMonth}        Run Keyword If         ${expMatureDate[1]} != "0" and ${expMatureDate[1]} < 10    Set Variable    0${expMatureDate[1]}    ELSE    Set Variable    ${expMatureDate[1]}
    ${expMatureDay}          Run Keyword If         ${expMatureDate[2]} != "0" and ${expMatureDate[2]} < 10    Set Variable    0${expMatureDate[2]}    ELSE    Set Variable    ${expMatureDate[2]}
    ${expMatureYear}         Set Variable           ${expMatureDate[0]} 
    ${expMatureDate}         Convert Date           ${expMatureYear}-${expMatureMonth}-${expMatureDay}    result_format=%Y-%m-%d
    Should Be Equal          ${principalXl}         ${approvePrincipal}   ${proposePrincipal}
   Comment   In RS Charges and GST details calculation
   IF  ((${charge1XlVal} or ${charge2XlVal}) != 0) or '${partner}' == 'Seeds'
    FOR    ${i}    IN RANGE    0    ${chargeCount}
      ${chargeName}          Get Value From Json     ${jsonData}            charges[${i}].name
      IF  ${chargeName} == []  Exit For Loop
      ${chargeName}          Get From List           ${chargeName}          0
      Append To List         ${chargeApiNames}       ${chargeName}
      ${chargeCalType}       Get Value From Json     ${jsonData}            charges[${i}].chargeCalculationType.value
      ${chargeCalType}       Get From List           ${chargeCalType}       0
      Append To List         ${chargeCalTypeList}    ${chargeCalType}
      ${chargeAmountRs}      Get Value From Json     ${jsonData}            charges[${i}].amount
      ${chargeAmountRs}      Get From List           ${chargeAmountRs}      0
      Set Global Variable    ${chargeAmountRs[${i}]}    ${chargeAmountRs}
      Append To List         ${chargeAmountList}     ${chargeAmountRs}
      ${chargeFeeAmountRs}   Get Value From Json     ${jsonData}            charges[${i}].amountPaid
      ${chargeFeeAmountRs}   Get From List           ${chargeFeeAmountRs}   0
      Append To List         ${chargeFeeAmountList}  ${chargeFeeAmountRs}
      ${chargeGSTEnableRs}   Get Value From Json     ${jsonData}            charges[${i}].enableGst
      ${chargeGSTEnableRs}   Get From List           ${chargeGSTEnableRs}   0
      Append To List         ${chargeGSTEnableList}  ${chargeGSTEnableRs}
      ${chargeIgstRs}        Get Value From Json     ${jsonData}            charges[${i}].igstAmount
      ${chargeIgstRs}        Get From List           ${chargeIgstRs}        0
      Append To List         ${chargeIgstList}       ${chargeIgstRs}
      ${chargeCgstRs}        Get Value From Json     ${jsonData}            charges[${i}].cgstAmount
      ${chargeCgstRs}        Get From List           ${chargeCgstRs}        0
      Append To List         ${chargeCgstList}       ${chargeCgstRs}
      ${chargeSgstRs}        Get Value From Json     ${jsonData}            charges[${i}].sgstAmount
      ${chargeSgstRs}        Get From List           ${chargeSgstRs}        0
      Append To List         ${chargeSgstList}   ${chargeSgstRs}
      # Set Global Variable    ${chargeName[${i}]}    ${chargeName}
    END  
    Log    ${chargeApiNames}
    Log     ${chargeAmountList}
    FOR   ${chargeXlName}  IN  @{chargesXlList}
      FOR    ${index}    ${chargeApiName}    IN ENUMERATE   @{chargeApiNames}
        IF   '${chargeXlName}' == '${chargeApiName}' 
          Log    ${chargeXlName}
          Log    ${chargeApiName}
          IF  '${charge1Xl}' != 'None' and '${charge1Xl}' == '${chargeApiName}'
           ${charge1CalType}       Set Variable       ${chargeCalTypeList[${index}]}
           ${charge1AmountRs}      Set Variable       ${chargeAmountList[${index}]}
           ${charge1FeeAmountRs}   Set Variable       ${chargeFeeAmountList[${index}]}
           ${charge1GSTEnableRs}   Set Variable       ${chargeGSTEnableList[${index}]}
           ${charge1IgstRs}        Set Variable       ${chargeIgstList[${index}]}
           ${charge1CgstRs}        Set Variable       ${chargeCgstList[${index}]}
           ${charge1SgstRs}        Set Variable       ${chargeSgstList[${index}]}
           IF  '${charge1GSTEnableRs}' == 'True'
            ${charge1FeeAmount}    Evaluate   round(${charge1AmountRs}*${100}/${118},${roundVal}) 
            IF  '${stateXl}' == 'Tamil Nadu' 
              ${charge1Cgst}       Evaluate    round(${charge1FeeAmount}*(9/100),${roundVal})
              ${c1Sgst}            Evaluate    ${charge1AmountRs} - ${charge1FeeAmount}
              ${charge1Sgst}       Evaluate    ${c1Sgst} - ${charge1Cgst}
              Should Be Equal As Numbers    ${charge1Cgst}    ${charge1CgstRs}  precision=1
              Should Be Equal As Numbers    ${charge1Sgst}    ${charge1SgstRs}  precision=1
            ELSE
              ${charge1Igst}     Evaluate          round(${charge1AmountRs}-${charge1FeeAmount},${roundVal})
              Should Be Equal    ${charge1Igst}    ${charge1IgstRs}
            END 
           ELSE
            ${charge1FeeAmount}    Set Variable       ${charge1AmountRs}
           END
          Should Be Equal    ${charge1FeeAmount}    ${charge1FeeAmountRs}
          END
          IF  '${charge2Xl}' != 'None' and '${charge2Xl}' == '${chargeApiName}'
           ${charge2CalType}       Set Variable       ${chargeCalTypeList[${index}]}
           ${charge2AmountRs}      Set Variable       ${chargeAmountList[${index}]}
           ${charge2FeeAmountRs}   Set Variable       ${chargeFeeAmountList[${index}]}
           ${charge2GSTEnableRs}   Set Variable       ${chargeGSTEnableList[${index}]}
           ${charge2IgstRs}        Set Variable       ${chargeIgstList[${index}]}
           ${charge2CgstRs}        Set Variable       ${chargeCgstList[${index}]}
           ${charge2SgstRs}        Set Variable       ${chargeSgstList[${index}]}
           IF  '${charge2GSTEnableRs}' == 'True'
            ${charge2FeeAmount}    Evaluate   round(${charge2AmountRs}*${100}/${118},${roundVal}) 
            IF  '${stateXl}' == 'Tamil Nadu' 
              ${charge2Cgst}       Evaluate    round(${charge2FeeAmount}*(9/100),${roundVal})
              ${c2Sgst}            Evaluate    ${charge2AmountRs} - ${charge2FeeAmount}
              ${charge2Sgst}       Evaluate    ${c2Sgst} - ${charge2Cgst}
              Should Be Equal As Numbers    ${charge2Cgst}    ${charge2CgstRs}  precision=1
              Should Be Equal As Numbers    ${charge2Sgst}    ${charge2SgstRs}  precision=1
            ELSE
              ${charge2Igst}     Evaluate          round(${charge2AmountRs}-${charge2FeeAmount},${roundVal})
              Should Be Equal    ${charge2Igst}    ${charge2IgstRs}
            END 
           ELSE
            ${charge2FeeAmount}    Set Variable       ${charge2AmountRs}
           END
          Should Be Equal    ${charge2FeeAmount}      ${charge2FeeAmountRs}
          END
          IF  '${charge3Xl}' != 'None' and '${charge3Xl}' == '${chargeApiName}'
           ${charge3CalType}       Set Variable       ${chargeCalTypeList[${index}]}
           ${charge3AmountRs}      Set Variable       ${chargeAmountList[${index}]}
           ${charge3FeeAmountRs}   Set Variable       ${chargeFeeAmountList[${index}]}
           ${charge3GSTEnableRs}   Set Variable       ${chargeGSTEnableList[${index}]}
           ${charge3IgstRs}        Set Variable       ${chargeIgstList[${index}]}
           ${charge3CgstRs}        Set Variable       ${chargeCgstList[${index}]}
           ${charge3SgstRs}        Set Variable       ${chargeSgstList[${index}]}
           IF  '${charge3GSTEnableRs}' == 'True'
            ${charge3FeeAmount}    Evaluate   round(${charge3AmountRs}*${100}/${118},${roundVal}) 
            IF  '${stateXl}' == 'Tamil Nadu' 
              ${charge3Cgst}       Evaluate    round(${charge3FeeAmount}*(9/100),${roundVal})
              ${c3Sgst}            Evaluate    ${charge3AmountRs} - ${charge3FeeAmount}
              ${charge3Sgst}       Evaluate    ${c3Sgst} - ${charge3Cgst}
              Should Be Equal As Numbers    ${charge3Cgst}    ${charge3CgstRs}  precision=1
              Should Be Equal As Numbers    ${charge3Sgst}    ${charge3SgstRs}  precision=1
            ELSE
              ${charge3Igst}     Evaluate          round(${charge3AmountRs}-${charge3FeeAmount},${roundVal})
              Should Be Equal    ${charge3Igst}    ${charge3IgstRs}
            END 
           ELSE
            ${charge3FeeAmount}    Set Variable       ${charge3AmountRs}
           END
          Should Be Equal    ${charge3FeeAmount}    ${charge3FeeAmountRs}
          END
          IF  '${charge4Xl}' != 'None' and '${charge4Xl}' == '${chargeApiName}'
           ${charge4CalType}       Set Variable       ${chargeCalTypeList[${index}]}
           ${charge4AmountRs}      Set Variable       ${chargeAmountList[${index}]}
           ${charge4FeeAmountRs}   Set Variable       ${chargeFeeAmountList[${index}]}
           ${charge4GSTEnableRs}   Set Variable       ${chargeGSTEnableList[${index}]}
           ${charge4IgstRs}        Set Variable       ${chargeIgstList[${index}]}
           ${charge4CgstRs}        Set Variable       ${chargeCgstList[${index}]}
           ${charge4SgstRs}        Set Variable       ${chargeSgstList[${index}]}
           IF  '${charge4GSTEnableRs}' == 'True'
            ${charge4FeeAmount}    Evaluate   round(${charge4AmountRs}*${100}/${118},${roundVal}) 
            IF  '${stateXl}' == 'Tamil Nadu' 
              ${charge4Cgst}       Evaluate    round(${charge4FeeAmount}*(9/100),${roundVal})
              ${c4Sgst}            Evaluate    ${charge4AmountRs} - ${charge4FeeAmount}
              ${charge4Sgst}       Evaluate    ${c4Sgst} - ${charge4Cgst}
              Should Be Equal As Numbers    ${charge4Cgst}    ${charge4CgstRs}  precision=1
              Should Be Equal As Numbers    ${charge4Sgst}    ${charge4SgstRs}  precision=1
            ELSE
              ${charge4Igst}     Evaluate          round(${charge4AmountRs}-${charge4FeeAmount},${roundVal})
              Should Be Equal    ${charge4Igst}    ${charge4IgstRs}
            END 
           ELSE
            ${charge4FeeAmount}    Set Variable       ${charge4AmountRs}
           END
          Should Be Equal    ${charge4FeeAmount}    ${charge4FeeAmountRs}
          END
          BREAK
        END
      END
    END
    ${charge3XlVal}    Run Keyword If   '${charge3XlVal}' != 'None'  Set Variable  ${charge3XlVal}  ELSE  Set Variable  0
    ${charge4XlVal}    Run Keyword If   '${charge4XlVal}' != 'None'  Set Variable  ${charge4XlVal}  ELSE  Set Variable  0
    ${totalFeeAmount}    Evaluate    ${charge1XlVal}+${charge2XlVal}+${charge3XlVal}+${charge4XlVal}
    ${netDisAmount}    Evaluate    ${principalXl}-${totalFeeAmount}
    # IF  '${chargeXlName}' == 'None'    Exit For Loop
    FOR    ${index}    ${elem}    IN ENUMERATE   @{chargeApiNames}
      # Remove values from list    ${chargeApiNames}    ${elem}
      Remove From List    ${chargeApiNames}       0
      Remove From List    ${chargeCalTypeList}    0
      Remove From List    ${chargeAmountList}     0
      Remove From List    ${chargeFeeAmountList}  0
      Remove From List    ${chargeGSTEnableList}  0
      Remove From List    ${chargeIgstList}       0
      Remove From List    ${chargeCgstList}       0
      Remove From List    ${chargeSgstList}       0
    END
   ELSE
    ${netDisAmount}    Set Variable    ${principalXl}
   END
    Should Be Equal           ${netDisAmount}   ${netDisAmountRs}     ${netSelfDisAmountRs}
    Comment   In RS for all tenure EMI,Principal,Interest & POS Calculation
    FOR    ${i}    IN RANGE    0    ${tenure}
     ${fromDate}              Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].fromDate
     ${dueDate}               Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].dueDate
     ${emiRs}                 Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].totalDueForPeriod
     ${selfEmiRs}             Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].totalSelfDueForPeriod
     ${parEmiRs}              Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].totalPartnerDueForPeriod
     ${principalDueRs}        Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].principalDue
     ${selfPrincipalDueRs}    Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].selfPrincipal
     ${parPrincipalDueRs}     Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].partnerPrincipal
     ${interestRs}            Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].interestDue
     ${selfInterestRs}        Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].selfInterestCharged
     ${parInterestRs}         Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].partnerInterestCharged
     ${principalOutRs}        Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].principalLoanBalanceOutstanding
     ${selfPrincipalOutRs}    Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].selfPrincipalLoanBalanceOutstanding
     ${parPrincipalOutRs}     Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].partnerPrincipalLoanBalanceOutstanding
     ${dpdRs}                 Get Value From Json    ${jsonData}    repaymentSchedule.periods[${i}].daysPastDue    
     ${emiRs}                 Get From List          ${emiRs}                 0
     ${selfEmiRs}             Get From List          ${selfEmiRs}             0
     ${parEmiRs}              Get From List          ${parEmiRs}              0
     ${principalOutRs}        Get From List          ${principalOutRs}        0
     ${selfPrincipalOutRs}    Get From List          ${selfPrincipalOutRs}    0
     ${parPrincipalOutRs}     Get From List          ${parPrincipalOutRs}     0
     IF  ${i} == 0
        #In RS Fees coumn details
        ${feesRs}        Get Value From Json     ${jsonData}    repaymentSchedule.periods[${i}].feeChargesDue
        ${selfFeesRs}    Get Value From Json     ${jsonData}    repaymentSchedule.periods[${i}].selfFeeChargesDue
        ${parFeesRs}     Get Value From Json     ${jsonData}    repaymentSchedule.periods[${i}].partnerFeeChargesDue
        ${feesRs}        Get From List           ${feesRs}      0
        ${selfFeesRs}    Get From List           ${selfFeesRs}  0
        ${parFeesRs}     Get From List           ${parFeesRs}   0
        Should Be Equal  ${feesRs}               ${selfFeesRs}  
        #In RS Paid coumn details
        ${feesPaidRs}        Get Value From Json     ${jsonData}    repaymentSchedule.periods[${i}].feeChargesPaid
        ${selfFeesPaidRs}    Get Value From Json     ${jsonData}    repaymentSchedule.periods[${i}].selfFeeChargesPaid
        ${parFeesPaidRs}     Get Value From Json     ${jsonData}    repaymentSchedule.periods[${i}].partnerFeeChargesPaid
        ${feesPaidRs}        Get From List           ${feesPaidRs}      0
        ${selfFeesPaidRs}    Get From List           ${selfFeesPaidRs}  0
        ${parFeesPaidRs}     Get From List           ${parFeesPaidRs}   0
        Should Be Equal      ${feesRs}               ${selfFeesRs}      ${feesPaidRs}    ${selfFeesPaidRs} 
        # Should Be Equal      ${totalChargesDeducted}  ${emiRs}          ${feesPaidRs}             
        Should Be Equal      ${principalXl}          ${principalOutRs}  ${selfPrincipalOutRs}
        IF  ${netParDisAmountRs}==0
            Should Be Equal   ${parFeesRs}     ${parFeesPaidRs}    ${parPrincipalOutRs}    ${0}
        END
     END
     IF  ${i}!=0
        #  Taking principal & Interest here bz in i=oth place no Principal & Interest
        ${dpdBucketRs}           Get Value From Json    ${jsonData}              repaymentSchedule.periods[${i}].dpdBucket
        ${dpdBucketRs}           Get From List          ${dpdBucketRs}           0
        ${principalDueRs}        Get From List          ${principalDueRs}        0
        ${selfPrincipalDueRs}    Get From List          ${selfPrincipalDueRs}    0
        ${parPrincipalDueRs}     Get From List          ${parPrincipalDueRs}     0  
        ${interestRs}            Get From List          ${interestRs}            0
        ${selfInterestRs}        Get From List          ${selfInterestRs}        0
        ${parInterestRs}         Get From List          ${parInterestRs}         0
        ${dueDateYr}      Get From List     ${dueDate}    0
        ${dueDateYr}      Get From List     ${dueDateYr}  0
        ${daysInYear}     Run Keyword If    ${dueDateYr}==2020 or ${dueDateYr}==2024   Set Variable     ${366}    ELSE   Set Variable   ${365}
        ${daysInYear}     Run Keyword If    '${partner}' == 'Seeds'  Set Variable  ${365}   ELSE   Set Variable   ${daysInYear}
        ${fromDate}       Get From List     ${fromDate}    0
        ${fromMonth}      Run Keyword If    ${fromDate[1]} != "0" and ${fromDate[1]} < 10    Set Variable    0${fromDate[1]}    ELSE    Set Variable    ${fromDate[1]}
        ${fromDay}        Run Keyword If    ${fromDate[2]} != "0" and ${fromDate[2]} < 10    Set Variable    0${fromDate[2]}    ELSE    Set Variable    ${fromDate[2]}
        ${fromDate}       Convert Date      ${fromDate[0]}-${fromMonth}-${fromDay}   result_format=%Y-%m-%d
        ${dueDate}        Get From List     ${dueDate}    0
        ${month}          Run Keyword If    ${dueDate[1]} != "0" and ${dueDate[1]} < 10    Set Variable    0${dueDate[1]}    ELSE    Set Variable    ${dueDate[1]}
        ${day}            Run Keyword If    ${dueDate[2]} != "0" and ${dueDate[2]} < 10    Set Variable    0${dueDate[2]}    ELSE    Set Variable    ${dueDate[2]}
        ${year}           Set Variable      ${dueDate[0]} 
        #  ${dueDate1}   Convert Date      ${year}-${month}-${day}    result_format=%d %B %Y
        ${dueDate1}       Convert Date      ${year}-${month}-${day}    result_format=%Y-%m-%d
        #  ${list}          Create List
        Log Many    ${dueDate1}    ${fromDate}
        ${diffDate}    Subtract Date From Date   ${dueDate1}  ${fromDate}    verbose
        IF  '${diffDate}' == '1 day'
          ${diffDate}    Get Substring             ${diffDate}    0    -4
        ELSE
          ${diffDate}    Get Substring             ${diffDate}    0    -5
        END
        Append To List     ${dueDateList}    ${dueDate1}
        # Append To List     ${RSList}         ${dueDate1}  ${emiRs}  ${principalRs}  ${interestRs}  ${principalOutRs}  ${dpdRs}
        ${emiRs}             Evaluate          int(${emiRs})    
        ${dpdRs}    Run Keyword If    ${dpdRs}!=[]        Get From List     ${dpdRs}    0
        IF  ${dpdRs}!=None and ${dpdRs}!=0
            ${dpdDiff}    Subtract Date From Date   ${currDate}  ${dueDate1}    verbose
            IF  '${dpdDiff}' == '1 day'
              ${dpdDiff}    Get Substring           ${dpdDiff}    0    -4
            ELSE
              ${dpdDiff}    Get Substring           ${dpdDiff}    0    -5
            END
            ${dpdDiff}    Convert To Integer    ${dpdDiff}
            Should Be Equal    ${dpdRs}    ${dpdDiff}
            Set Test Variable     ${dpdDiff}
            Set Test Variable     ${dpdBucketRs}
            Comment   NPA and DPD bucket test
            Run Keyword    NPA and DPD Bucket calcualtion
        END
        # ${dpdRs}      Run Keyword And Ignore Error      Get From List     ${dpdRs}    0
        ${lastEmi}    Evaluate    ${tenure}-1
        IF  ${lastEmi} != ${i}
          ${int_12}=    Evaluate    (${interestXl}/12)/100
          ${emi}=       Evaluate    ${principalXl}*((${int_12}*((1+${int_12})**${noOfRepayRs}))/(((1+${int_12})**${noOfRepayRs})-1))
          ${emi}=       Evaluate    math.ceil(${emi})
          ${emi}        Run Keyword If   '${partner}' == 'Seeds'  Evaluate   math.ceil(${emi}/100.0)*100  ELSE  Set Variable  ${emi}
          Should Be Equal   ${emi}  ${emiRs}   ${selfEmiRs}
        END
        IF  ${i}==1
          ${intCal}        Evaluate    ${principalXl}*(${interestXl}/100)*(${diffDate}/${daysInYear})
          ${integer_part}    Evaluate    int(${intCal})
          ${decimal_part}    Evaluate    "${intCal}".split(".")[1]
          ${truncated_value}    Set Variable    ${integer_part}.${decimal_part[0:2]}
          IF  '${partner}' == 'Seeds'
            ${twoDecimal}    Set Variable    ${decimal_part[0:1]}
            ${intCal}        Run Keyword If  int('${twoDecimal}') >= 5   Evaluate  ${integer_part}+${1}   ELSE  Set Variable  ${integer_part}
          ELSE
            ${intCal}    Evaluate    math.ceil(${truncated_value})
          END
          # ${intCal}    Run Keyword If  '${partner}' == 'Seeds'  Evaluate  ${decimal_part} >= 50  Evaluate  (${intCal}+${1})  ELSE  Set Variable  ${intCal}
          Should Be Equal      ${intCal}        ${interestRs}        ${selfInterestRs}
          ${principalDue}      Evaluate         ${emi}-${intCal}
          Should Be Equal      ${principalDue}  ${principalDueRs}       ${selfPrincipalDueRs}
          ${balanceLoan}   Evaluate    ${principalXl}-${principalDue} 
          Should Be Equal          ${balanceLoan}    ${principalOutRs}  ${selfPrincipalOutRs}
        END
        IF  ${i}!=1 and ${i}!=${lastEmi}    
          ${intCal}          Evaluate    ${balanceLoan}*(${interestXl}/100)*(${diffDate}/${daysInYear})
          ${integer_part}    Evaluate    int(${intCal})
          ${decimal_part}    Evaluate    "${intCal}".split(".")[1]
          ${truncated_value}    Set Variable    ${integer_part}.${decimal_part[0:2]}
          # ${intCal}    Evaluate    math.ceil(${truncated_value})
          IF  '${partner}' == 'Seeds'
            ${twoDecimal}    Set Variable    ${decimal_part[0:1]}
            ${intCal}        Run Keyword If  int('${twoDecimal}') >= 5   Evaluate  ${integer_part}+${1}   ELSE  Set Variable  ${integer_part}
          ELSE
            ${intCal}    Evaluate    math.ceil(${truncated_value})
          END
          Should Be Equal    ${intCal}       ${interestRs}        ${selfInterestRs}
          ${principalDue}    Evaluate        ${emi}-${intCal}
          ${principalDue}    Evaluate        math.ceil(${principalDue})
          Should Be Equal    ${principalDue}    ${principalDueRs}       ${selfPrincipalDueRs}
          ${balanceLoan}     Evaluate         ${balanceLoan} - ${principalDue}
          Should Be Equal    ${balanceLoan}    ${principalOutRs}    ${selfPrincipalOutRs}
        END
        IF  ${lastEmi} == ${i}
          ${principalLast}   Set Variable   ${balanceLoan}
          ${intCal}          Evaluate       ${principalLast}*(${interestXl}/100)*(${diffDate}/${daysInYear})
          ${integer_part}    Evaluate       int(${intCal})
          ${decimal_part}    Evaluate       "${intCal}".split(".")[1]
          ${truncated_value}    Set Variable    ${integer_part}.${decimal_part[0:2]}
          IF  '${partner}' == 'Seeds'
            ${twoDecimal}    Set Variable    ${decimal_part[0:1]}
            ${intCal}        Run Keyword If  int('${twoDecimal}') >= 5   Evaluate  ${integer_part}+${1}   ELSE  Set Variable  ${integer_part}
          ELSE
            ${intCal}    Evaluate    math.ceil(${truncated_value})
          END
          Should Be Equal    ${intCal}       ${interestRs}        ${selfInterestRs}
          ${principalDue}    Set Variable    ${principalLast}
          Should Be Equal    ${principalDue}  ${principalDueRs}       ${selfPrincipalDueRs}
          ${int_12}          Evaluate       (${interestXl}/12)/100
          ${emi}             Evaluate       ${principalLast} + ${intCal}
          # ${selfEmiRs}    Get From List     ${selfEmiRs}  0
          Should Be Equal   ${emi}          ${emiRs}      ${selfEmiRs}
        END
        IF  ${netParDisAmountRs}==0
          Should Be Equal   ${parEmiRs}  ${parPrincipalDueRs}  ${parInterestRs}  ${parPrincipalOutRs}    ${0}
        END
     END     
    #  Append To List     ${list}   ${dueDate1}  ${emiRs}  ${principalRs}  ${interestRs}  ${principalOutRs}  ${dpdRs}
    #  Set Test Message   ${list}
    END
    Comment   In RS Due date test of all tenures
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
    ${1stDueDateRs}    Get From List     ${1stDueRS}       2
    ${1stDueMonthRs}   Get From List     ${1stDueRS}       1
    ${1stDueyearRs}    Get From List     ${1stDueRS}       0
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
    ${DueDateRs}    Get From List     ${DueRS}       2
    ${DueMonthRs}   Get From List     ${DueRS}       1
    ${DueyearRs}    Get From List     ${DueRS}       0
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

NPA and DPD Bucket calcualtion
  IF  0 < ${dpdDiff} <= 30
   ${dpdBucket}    Set Variable  SMA_0
  ELSE IF  30 < ${dpdDiff} <= 60
   ${dpdBucket}    Set Variable  SMA_1
  ELSE IF  6 < ${dpdDiff} <= 90
   ${dpdBucket}    Set Variable  SMA_2
  ELSE IF  90 < ${dpdDiff}
   ${dpdBucket}    Set Variable  NPA
  END
  Should Be Equal    ${dpdBucket}    ${dpdBucketRs}


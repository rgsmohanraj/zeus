*** Settings ***
Resource    ../keywords/common.robot
Resource    ../testcases/11__getExcelTemp.robot
Library    17__gstReport.py

*** Variables ***
${processFeesGSTMethod}    1    # 1-Inclusive  2-Exclusive
${insChargesGSTMethod}     1

*** Keywords ***
Download GST report
    [Documentation]    Download the GST report
    Set Selenium Speed    0.5s
    Click Element    xpath://a[@href='#/reports']
    Sleep    1
    Click Element    xpath://td[text()=' GST Report ']
    Sleep    1
    Click Element    xpath://*[@class='mat-input-element mat-form-field-autofill-control mat-datepicker-input ng-tns-c93-37 ng-untouched ng-pristine ng-invalid cdk-text-field-autofill-monitored']
    Click Element    xpath://button[@cdkarialive='polite']
    Click Element    xpath://div[text()=' 2020 ']
    Click Element    xpath://div[text()=' JAN ']
    Click Element    xpath://div[text()=' 1 ']
    Click Element    xpath://*[@class='mat-input-element mat-form-field-autofill-control mat-datepicker-input ng-tns-c93-38 ng-untouched ng-pristine ng-invalid cdk-text-field-autofill-monitored']
    ${currDay}   Get Time	return day
    ${currDay}   Run Keyword If    "${currDay[0]}" == "0"    Evaluate    "${currDay[1:]}"    ELSE    Set Variable    ${currDay}
    Click Element    xpath://div[text()=' ${currDay} ']
    Click Element    xpath://*[@icon="cogs"]
    Sleep  3s
    Log    GST report downloaded

Download GST report using API
  ${currDay}     Get Current Date     result_format=%d
  ${currMonth}   Get Current Date     result_format=%B
  create session     getGSTReport     ${baseUrl.QA}
  ${header}     Create Dictionary    Authorization=Basic YWRtaW46cGFzc3dvcmQ=     Fineract-Platform-TenantId=default  Content-Type=application/json
  ${Rurl}       Set Variable         /lms/api/v1/runreports/GST%20Report?R_loanChargeCreatedStartDate=05%20January%202022&R_loanChargeCreatedEndDate=${currDay}%20${currMonth}%202024&locale=en&dateFormat=dd%20MMMM%20yyyy&exportCSV=true
  ${response}   GET On Session       getGSTReport      ${Rurl}     headers=${header}
  ${currDay}       Get Time  
  ${currDay}       Replace String      ${currDay}        :    ${EMPTY}
  Create Binary File   ${CURDIR}//testData//GST_Report_${currDay}.csv      ${response.content}

Check in GST report 
    [Documentation]    Check in GST report values with bulk loan excel values
    ${gstReportPath}    11__getRecentFile.Get Most Recent Csv File
    Set Global Variable   ${gstReportPath}
    ${failed_loops}    Create List
    # GST Report constant column values
    ${Type}            Set Variable      Taxable Supply
    ${billState}       Set Variable      Tamil Nadu   
    ${billCountry}     Set Variable      India
    ${HSNcode}         Set Variable      ${997113}
    ${subject}         Set Variable      ${EMPTY}
    ${stateCode}       Create Dictionary     Jammu and Kashmir=${1}   Himachal Pradesh=${2}  Punjab=${3}   Chandigarh=${4}   Uttarakhand=${5}  Haryana=${6}  Delhi=${7}  Rajasthan=${8}   Uttar pradesh=${9}  Bihar=${10}   Sikkim=${11}   Arunachal pradesh=${12}  Nagaland=${13}  Manipur=${14}  Mizoram=${15}  Tripura=${16}  Meghalaya=${17}  Assam=${18}  West Bengal=${19}  Jharkhand=${20}  Odisha=${21}  Chattisgarh=${22}  Madhya pradesh=${23}  Gujarat=${24}  Dadra and Nagar Haveli and Daman and Diu=${26}  Maharashtra=${27}  Andhra Pradesh=${28}  Karnataka=${29}  Goa=${30}  Lakshadweep=${31}  Kerala=${32}  Tamil Nadu=${33}  Puducherry=${34}  Andaman and Nicobar Islands=${35}   Telangana=${36}  Ladakh=${38}
    #From Bulk loan upload excel
    FOR    ${index}    IN RANGE    ${startLoanId}      ${endLoanId}
      ${rowNo}         Evaluate          (${index}-${startLoanId}+2)
      Open Workbook   ${bulkLoanFile}
      ${exId}         Read From Cell    B${rowNo}
      ${assetCls}     Read From Cell    E${rowNo}
      ${firstName}    Read From Cell    F${rowNo}
      ${middleName}   Read From Cell    G${rowNo}
      ${lastName}     Read From Cell    H${rowNo}
      IF  '${middleName}'=='None'
        ${clientName}   Set Variable      ${firstName} ${lastName}
      ELSE
        ${clientName}   Set Variable      ${firstName} ${middleName} ${lastName} 
      END
      ${clientState}    Read From Cell    V${rowNo}
      ${stateCode1}     Set Variable      ${stateCode}[${clientState}]
      ${disburseDate}   Read From Cell    AG${rowNo}
      ${disburseDate}   Convert Date      ${disburseDate}    result_format=%d-%m-%Y
      ${processFees}    Read From Cell    AM${rowNo}
    # If processFees & Insurance charges empty ,while computing its getting failed so set to zero
      IF  '${processFees}' == 'None'
        ${processFees}    Set Variable    0
      END
      ${processFees}            Evaluate          float(${processFees})
      ${processFeeAmountEx}     Set Variable      ${processFees}
      IF  ${processFeesGSTMethod} == ${1}
        IF  '${partner}' == 'Seeds'
         ${processFeeAmountEx}    Evaluate          round(${processFees}*${100}/${118},0) 
        ELSE
         ${processFeeAmountEx}    Evaluate          round(${processFees}*${100}/${118},2)
        END    
        ${processFeeGSTAmount}      Evaluate          ${processFees}-${processFeeAmountEx} 
      ELSE IF   ${processFeesGSTMethod} == ${2}
          ${processFeeGSTAmount}    Evaluate          round(${processFees}*${18}/${100},2)
          ${processFees}            Evaluate          round(${processFeeAmountEx}+${processFeeGSTAmount},2)
      END
      ${insCharges}             Read From Cell    AN${rowNo}
      IF  '${insCharges}' == 'None'
        ${insCharges}    Set Variable    0
      END
      ${insCharges}             Evaluate          float(${insCharges})
      ${insChargesAmountEx}     Set Variable      ${insCharges}
      IF  ${insChargesGSTMethod} == 1
        IF  '${partner}' == 'Seeds'
         ${insChargesAmountEx}       Evaluate          round(${insCharges}*${100}/${118},0)
        ELSE
         ${insChargesAmountEx}       Evaluate          round(${insCharges}*${100}/${118},2)
        END
        ${insChargesGSTAmount}    Evaluate          ${insCharges}-${insChargesAmountEx} 
      ELSE IF   ${insChargesGSTMethod} == ${2}
         ${insChargesGSTAmount}    Evaluate          round(${insCharges}*${18}/${100},2)
         ${insCharges}             Evaluate          round(${insChargesAmountEx}+${insChargesGSTAmount},2)
      END
       ${totalChargesDeducted}   Evaluate          round(${processFeeAmountEx}+${insChargesAmountEx},2)
       ${totalGSTDeducted}       Evaluate          round(${processFeeGSTAmount}+${insChargesGSTAmount},2)
      Close Workbook
      IF  ${stateCode1} == ${33}    #Tamilnadu GST state code=33
        ${CGSTpf1}   Evaluate   ${processFees} - ${processFeeAmountEx}
       IF  '${partner}' == 'Seeds'
        ${CGSTpf}    Evaluate   round(${processFeeAmountEx}*(9/100),0)
        ${SGSTpf}    Evaluate   round(${CGSTpf1} - ${CGSTpf},0)
        ${CGSTic1}   Evaluate   ${insCharges} - ${insChargesAmountEx}
        ${CGSTic}    Evaluate   round(${insChargesAmountEx}*(9/100),0)
        ${SGSTic}    Evaluate   round(${CGSTic1} - ${CGSTic},0)
       ELSE
        ${CGSTpf}    Evaluate   round(${processFeeAmountEx}*(9/100),2)
        ${SGSTpf}    Evaluate   round(${CGSTpf1} - ${CGSTpf},2)
        ${CGSTic1}   Evaluate   ${insCharges} - ${insChargesAmountEx}
        ${CGSTic}    Evaluate   round(${insChargesAmountEx}*(9/100),2)
        ${SGSTic}    Evaluate   round(${CGSTic1} - ${CGSTic},2)
       END
        ${IGSTpf}    Set Variable    0
        ${IGSTic}    Set Variable    0
      ELSE
        ${IGSTpf}    Evaluate   round(${processFees} - ${processFeeAmountEx},2)
        ${IGSTic}    Evaluate   round(${insCharges} - ${insChargesAmountEx},2)
        ${CGSTpf}    Set Variable    0
        ${SGSTpf}    Set Variable    0
        ${CGSTic}    Set Variable    0
        ${SGSTic}    Set Variable    0
      END
      # If both fees & charges are 0 
      IF  ${processFees} or ${insCharges} != 0
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
        ${feesName}        Get Value From Json    ${jsonData}    charges[0].name
        ${feesAmount}      Get Value From Json    ${jsonData}    charges[0].amount
        ${feesType}        Get Value From Json    ${jsonData}    charges[0].chargeTimeType.value         #Disbursement
        ${feesCalType}     Get Value From Json    ${jsonData}    charges[0].chargeCalculationType.value  #Flat
        ${chargeName}      Get Value From Json    ${jsonData}    charges[1].name
        ${chargeAmount}    Get Value From Json    ${jsonData}    charges[1].amount
        ${chargeType}      Get Value From Json    ${jsonData}    charges[1].chargeTimeType.value
        ${chargeCalType}   Get Value From Json    ${jsonData}    charges[1].chargeCalculationType.value
        ${result}          17__gstReport.Search Csv By LoanAccountNumber    ${gstReportPath}   ${exId}   ${loanAccNo}  ${clientName}  ${Type}  ${assetCls}   ${partner}   ${index}  ${stateCode1}  ${processFees}   ${processFeeGSTAmount}  ${insCharges}  ${insChargesAmountEx}  ${totalChargesDeducted}   ${totalGSTDeducted}   ${disburseDate}  ${billState}  ${billCountry}  ${CGSTpf}  ${CGSTic}  ${SGSTpf}  ${SGSTic}  ${IGSTpf}  ${IGSTic}  ${HSNcode}  ${subject}
        Run Keyword If    '${result}' != 'PASS'    Run Keyword And Continue On Failure     Log   Loop number ${index} failed!
        Run Keyword If    '${result}' != 'PASS'    Append To List    ${failed_loops}    ${index}       
      END
    END
    Run Keyword If    '${failed_loops}' != []    Set Test Message    The following loops failed: ${failed_loops}
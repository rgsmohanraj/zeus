*** Settings ***
Resource    ../keywords/common.robot
Resource    ../PageObjects/01__loginPage.robot
Resource    ../testcases/11__getExcelTemp.robot
Library     ../PageObjects/11__testDateXl.py

*** Keywords ***
Bulk loan date insert to excel
    [Documentation]   Date insert to excel using python code
    ${loanCount1}    Evaluate       int(${loanCount}+${1})
    ${loanCount2}    Evaluate       int(${loanCount1}/2)
    ${loanCount3}    Evaluate       int(${loanCount1} - ${loanCount2})
    Date Creation Xl    ${loanCount1}

Download loan temp
    [Documentation]   Download bulk loan template for partner and product
    Set Selenium Speed  0.3s
    Sleep    1s
    Click Element    xpath://a[@href='#/organization/bulk-import']
    Click Element    xpath://h4[text()='Bulk Loan ']
    Sleep    1s
    Click Element    xpath://mat-select[@formcontrolname='partnerId']
    Click Element    xpath://span[contains(text(),'${partner}')]
    Click Element    xpath://mat-select[@formcontrolname='productId']
    Click Element    xpath:(//span[contains(text(),'${product}')])[2]
    Click Element    xpath://*[@class='fa fa-download']
    Sleep    5s
    log  ${partner} partner and ${product} product bulk loan template downloaded

Bulk Loan Download using API
  ${productId}    Get From Dictionary    ${productId}    key=${product}
  ${partnerId}    Get From Dictionary    ${partnerId}    key=${product}
  create session     getBulkLoanTemp     ${baseUrl.QA}
  ${header}     Create Dictionary    Authorization=Basic YWRtaW46cGFzc3dvcmQ=     Fineract-Platform-TenantId=default  Content-Type=application/json
  ${Rurl}       Set Variable         /lms/api/v1/loans/uploadtemplate?partnerId=${partnerId}&productId=${productId} 
  ${response}   GET On Session       getBulkLoanTemp      ${Rurl}     headers=${header}
  ${currDay}       Get Time  
  ${currDay}       Replace String      ${currDay}        :    ${EMPTY}
  Create Binary File    ${CURDIR}//testData//${partner}_Bulk_Loan_${currDay}.xlsx      ${response.content}
  ${bulkLoanFile}    Set Variable    ${CURDIR}//testData//${partner}_Bulk_Loan_${currDay}.xlsx
  Set Global Variable    ${bulkLoanFile}

Create a Bulk loan using product
    [Documentation]  Bulk loan creation using product Api and Charge Api details
    ${productId}    Get From Dictionary    ${productId}    key=${product}
    create session     getProduct     ${baseUrl.QA}
    ${header}     Create Dictionary    Authorization=Basic YWRtaW46cGFzc3dvcmQ=     Fineract-Platform-TenantId=default  Content-Type=application/json
    ${Rurl}       Set Variable         /lms/api/v1/loanproducts/${productId}
    ${response}   GET On Session       getProduct      ${Rurl}     headers=${header} 
    ${jsonData}   Convert String To Json        ${response.content}
    ${productNameRs}         Get Value From Json    ${jsonData}    name
    ${minPrincipal}          Get Value From Json    ${jsonData}    minPrincipal
    ${maxPrincipal}          Get Value From Json    ${jsonData}    maxPrincipal
    ${minRepayPeriod}        Get Value From Json    ${jsonData}    minNumberOfRepayments
    ${maxRepayPeriod}        Get Value From Json    ${jsonData}    maxNumberOfRepayments
    ${minInterest}           Get Value From Json    ${jsonData}    minInterestRatePerPeriod
    ${maxInterest}           Get Value From Json    ${jsonData}    maxInterestRatePerPeriod
    ${productNameRs}         Get From List          ${productNameRs}    0
    ${minPrincipal}          Get From List          ${minPrincipal}     0
    ${maxPrincipal}          Get From List          ${maxPrincipal}     0
    ${minRepayPeriod}        Get From List          ${minRepayPeriod}   0
    ${minRepayPeriodSet}     Set Variable           ${minRepayPeriod}
    ${maxRepayPeriod}        Get From List          ${maxRepayPeriod}   0
    ${minInterest}           Get From List          ${minInterest}      0
    ${minInterestSet}        Set Variable           ${minInterest}
    ${maxInterest}           Get From List          ${maxInterest}      0
    ${chargeNames}           Create List
    ${chargeIds}             Create List
    ${chargeTypes}           Create List
    ${gstTypes}              Create List
    ${minCharges}            Create List
    ${maxCharges}            Create List
    ${chargeRounds}          Create List
    ${chargeGstRounds}       Create List
    ${chargeCount}    Get From Dictionary    ${chargeCount}    key=${product}
    FOR    ${i}    IN RANGE    0    ${chargeCount}
      Log    ${jsonData}
      ${chargeName}      Get Value From Json    ${jsonData}      colendingFees[${i}].name
      ${chargeName}      Get From List          ${chargeName}    0
      Append To List     ${chargeNames}         ${chargeName}
      ${chargeId}        Get Value From Json    ${jsonData}      colendingFees[${i}].chargeId
      ${chargeId}        Get From List          ${chargeId}      0
      Append To List     ${chargeIds}           ${chargeId}
      create session     getCharge       ${baseUrl.QA}
      ${header}     Create Dictionary    Authorization=Basic YWRtaW46cGFzc3dvcmQ=     Fineract-Platform-TenantId=default  Content-Type=application/json
      ${Rurl}       Set Variable         /lms/api/v1/charges/${chargeIds[${i}]}
      ${response}   GET On Session       getCharge      ${Rurl}     headers=${header} 
      ${chargeData}   Convert String To Json        ${response.content}
      Log    Charge Name: ${chargeNames[${i}]}
      ${chargeType}       Get Value From Json    ${chargeData}    chargeCalculationType.value
      ${chargeType}       Get From List          ${chargeType}    0
      Append To List      ${chargeTypes}         ${chargeType}
      ${gstType}          Get Value From Json    ${chargeData}    gstSelected.value
      ${gstType}          Get From List          ${gstType}       0
      Append To List      ${gstTypes}            ${gstType}
      ${minCharge}        Get Value From Json    ${chargeData}    minChargeAmount
      ${minCharge}        Get From List          ${minCharge}     0
      Append To List      ${minCharges}          ${minCharge}
      ${maxCharge}        Get Value From Json    ${chargeData}    maxChargeAmount
      ${maxCharge}        Get From List          ${maxCharge}     0
      Append To List      ${maxCharges}          ${maxCharge}
      ${chargeRound}      Get Value From Json    ${chargeData}    chargeRoundingMode
      ${chargeRound}      Get From List          ${chargeRound}   0
      Append To List      ${chargeRounds}        ${chargeRound}
    END
   Process Recent Excel File
   Set Global Variable   ${file_path}
   Open Workbook  ${file_path}
   ${extId}         Generate Random String     3    [UPPER]
   Set Global Variable    ${extId}
   ${middleName}    Generate Random String     2    [UPPER]
   ${lastName}      Generate Random String     2    [UPPER]
   ${chargesXlList}    Create List
   ${charge1Xl}    Read From Cell     AM${1}
   ${charge2Xl}    Read From Cell     AN${1}
   ${charge3Xl}    Read From Cell     AO${1}
   ${charge4Xl}    Read From Cell     AP${1}
   ${charge5Xl}    Read From Cell     AQ${1}
   Append To List    ${chargesXlList}    ${charge1Xl}  ${charge2Xl}  ${charge3Xl}  ${charge4Xl}  ${charge5Xl}
   Comment   XL charge order & API Charge orders are diff so loop throughing to find the specific charge Index&min,max value
   FOR   ${chargeXlName}  IN  @{chargesXlList}
        FOR    ${index}    ${chargeApiName}    IN ENUMERATE   @{chargeNames}
            IF   '${chargeXlName}' == '${chargeApiName}' 
                Log    ${chargeXlName}
                Log    ${chargeApiName}
             IF  '${charge1Xl}' != 'None' and '${charge1Xl}' == '${chargeApiName}'
              Log    ${chargeApiName}
              ${minFees1}      Set Variable       ${minCharges[${index}]}
              ${minFees1Set}   Set Variable       ${minFees1}
              ${maxFees1}      Set Variable       ${maxCharges[${index}]}
             END
             IF  '${charge2Xl}' != 'None' and '${charge2Xl}' == '${chargeApiName}'
              ${minFees2}      Set Variable       ${minCharges[${index}]}
              ${minFees2Set}   Set Variable       ${minFees2}
              ${maxFees2}      Set Variable       ${maxCharges[${index}]}
             END
             IF  '${charge3Xl}' != 'None' and '${charge3Xl}' == '${chargeApiName}'
              ${minFees3}      Set Variable       ${minCharges[${index}]}
              ${minFees3Set}   Set Variable       ${minFees3}
              ${maxFees3}      Set Variable       ${maxCharges[${index}]}
             END
             IF  '${charge4Xl}' != 'None' and '${charge4Xl}' == '${chargeApiName}'
              ${minFees4}      Set Variable       ${minCharges[${index}]}
              ${minFees4Set}   Set Variable       ${minFees4}
              ${maxFees4}      Set Variable       ${maxCharges[${index}]}
             END
             IF  '${charge5Xl}' != 'None' and '${charge5Xl}' == '${chargeApiName}'
              ${minFees5}      Set Variable       ${minCharges[${index}]}
              ${minFees5Set}   Set Variable       ${minFees5}
              ${maxFees5}      Set Variable       ${maxCharges[${index}]}
             END
             BREAK
            END
        END
      IF  '${chargeXlName}' == 'None'    Exit For Loop
   END
   FOR    ${index}    IN RANGE    1    ${loanCount}
    Write To Cell    A${index+1}    ${index}
    Write To Cell    B${index+1}    ${extId}${index}
    Write To Cell    C${index+1}    Vivriti Capital Limited
    Write To Cell    D${index+1}    PERSON
    # ${assetClass}    Create List    BNPL - Expenses  BNPL - Asset Purchase  PL - ST  MFI  Education  Pledge  Gold - Savings  Property-Buy  Property-Renovate  Two Wheeler - New  Two Wheeler - Old  Commercial vehicle - New  Four Wheeler - New  Four Wheeler - Old  Commercial vehicle - Used  Leasing - Furniture  Leasing - Equipment  Leasing - Product  Leasing - CV  CEQ - excavators  CEQ - Bulldozers  Car  Machinery  Gold  LRD - Lease rental discounting  Stock  Cash + FD + Mutual Funds
    # ${assetClass}    random.choice   ${assetClass}
    Write To Cell    E${index+1}    MFI
    ${firstName}     FakerLibrary.First Name
    Write To Cell    F${index+1}    ${firstName}
    Write To Cell    G${index+1}    ${middleName}
    Write To Cell    H${index+1}    ${lastName} 
    Write To Cell    I${index+1}    ${38}
    ${gender}        Create List    Male    Female   Transgender
    ${gender}        random.choice  ${gender}
    Write To Cell    J${index+1}    ${gender}
    ${aadharNum}     FakerLibrary.Numerify    text=###########
    ${pan1}          Generate Random String   5    [UPPER]
    ${pan2}          FakerLibrary.Numerify    text=####
    Write To Cell    L${index+1}    ${pan1}${pan2}B    
    Write To Cell    M${index+1}    7${aadharNum} 
    Write To Cell    N${index+1}    ZAP0766072
    Write To Cell    O${index+1}    N8369845
    Write To Cell    P${index+1}    DB5620130123456
    ${address}       FakerLibrary.Address
    Write To Cell    Q${index+1}    ${address}
    Write To Cell    R${index+1}    ${816125}
    Write To Cell    T${index+1}    KEYAN@gmail.com
    ${mobNum}        FakerLibrary.Numerify    text=######### 
    Write To Cell    S${index+1}    7${mobNum}
    Write To Cell    U${index+1}    Chennai
    ${state}         Create List    Jammu and Kashmir  Himachal Pradesh  Punjab  Chandigarh  Uttarakhand  Haryana  Delhi  Rajasthan  Uttar pradesh  Bihar  Sikkim  Arunachal pradesh  Nagaland  Manipur  Mizoram  Tripura  Meghalaya  Assam  West Bengal  Jharkhand  Odisha  Chattisgarh  Madhya pradesh  Gujarat  Dadra and Nagar Haveli and Daman and Diu  Maharashtra  Andhra Pradesh  Karnataka  Lakshadweep  Kerala  Tamil Nadu  Puducherry  Andaman and Nicobar Islands  Telangana  Ladakh
    ${state}         random.choice  ${state}
    Write To Cell    V${index+1}    ${state}
    Write To Cell    Y${index+1}    ${firstName}
    Write To Cell    Z${index+1}    11005600003855
    ${accType}       Create List     SBA - Savings Account  CAA - Current Account
    ${accType}       random.choice   ${accType}
    Write To Cell    AA${index+1}    ${accType}
    Write To Cell    AB${index+1}    FDRL0001001
    Write To Cell    AC${index+1}    600019159
    Write To Cell    AD${index+1}    FDRLINBBIBD
    Write To Cell    AE${index+1}    Arcot road,kodambakam
    Comment    If the Pricipal,fees,loan term and Interest reaches max again it will start from min value
    ${principal}=    Evaluate    (${minPrincipal} + (${index} * 1000)) % (${maxPrincipal} - ${minPrincipal}) + ${minPrincipal}
    Write To Cell    AH${index+1}     ${principal}
    ${minRepayPeriod}    Evaluate        ${minRepayPeriod}+${1}
    ${minRepayPeriod}    Run Keyword If    ${minRepayPeriod} > ${maxRepayPeriod}     Set Variable   ${minRepayPeriodSet}  ELSE   Set Variable   ${minRepayPeriod}
    Write To Cell    AI${index+1}     ${minRepayPeriod} 
    ${minInterest}     Evaluate        ${minInterest}+${0.5}
    ${minInterest}    Run Keyword If    ${minInterest} > ${maxInterest}     Set Variable   ${minInterestSet}  ELSE   Set Variable  ${minInterest}  
    Write To Cell    AL${index+1}     ${minInterest}
    Comment          Increment the fee value by 100
    ${minFees1}      Evaluate    ${minFees1} + 100
    Comment          Check if the fee value has reached the maximum if its set minfees value
    ${minFees1}    Run Keyword If    ${minFees1} > ${maxFees1}     Set Variable   ${minFees1Set}  ELSE   Set Variable  ${minFees1}  
    Write To Cell    AM${index+1}     ${minFees1}
    ${minFees2}    Evaluate    ${minFees2} + 100
    ${minFees2}    Run Keyword If    ${minFees2} > ${maxFees2}     Set Variable   ${minFees2Set}  ELSE   Set Variable  ${minFees2}
    Write To Cell    AN${index+1}     ${minFees2}
    IF  '${charge3Xl}' != 'None'
      ${minFees3}    Evaluate    ${minFees3} + 100
      ${minFees3}    Run Keyword If    ${minFees3} > ${maxFees3}     Set Variable   ${minFees3Set}  ELSE   Set Variable  ${minFees3}    
      Write To Cell    AO${index+1}     ${minFees3}
    END
    IF  '${charge4Xl}' != 'None'
      ${minFees4}    Evaluate    ${minFees4} + 100
      ${minFees4}    Run Keyword If    ${minFees4} > ${maxFees4}     Set Variable   ${minFees4Set}  ELSE   Set Variable  ${minFees4}
      Write To Cell    AP${index+1}     ${minFees4}
    END
    IF  '${charge5Xl}' != 'None'
      ${minFees5}    Evaluate    ${minFees5} + 100
      ${minFees5}    Run Keyword If    ${minFees5} > ${maxFees5}     Set Variable   ${minFees5Set}  ELSE   Set Variable  ${minFees5}  
      Write To Cell    AQ${index+1}     ${minFees5}
    END
   END
  Write To Cell    AM2     ${0}
  Write To Cell    AN2     ${0}
  Write To Cell    AM3     ${0}
   Save
   Close Workbook
    
Upload the created excel loan
   [Documentation]    Bulk loan upload in UI
   Set Selenium Speed  0.3s
   Process Recent Excel File
   Set Global Variable   ${file_path}
  #  01__loginPage.Open Browser and login to Zeus     ${userName}        ${password}
   Click Element    xpath://a[@href='#/organization/bulk-import']
   Click Element    xpath://h4[text()='Bulk Loan ']
   Sleep    2s
   Click Element    xpath://mat-select[@formcontrolname='partnerId']
   Click Element    xpath://span[contains(text(),'${partner}')]
   Click Element    xpath://mat-select[@formcontrolname='productId']
   Click Element    xpath:(//span[contains(text(),'${product}')])[2]
   Sleep    2s
   Choose File      xpath://input[@type='file']    ${file_path}
   Sleep    1s
   Click Element    xpath://i[@class='fa fa-upload']
   Sleep    10s
   Scroll Element Into View    xpath://span[contains(text(),'Download')]
   Click Element               xpath://span[contains(text(),'Download')]
   Capture Page Screenshot

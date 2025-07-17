*** Settings ***
Resource    ../keywords/common.robot
Resource    ../PageObjects/01__loginPage.robot
Resource    ../testcases/11__getExcelTemp.robot

*** Variables ***
${dir}=        D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads
${loopCount}=  2

*** Keywords ***
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
    Click Element    xpath://span[contains(text(),'${product}')]
    Click Element    xpath://*[@class='fa fa-download']
    Sleep    5s
    log  ${partner} partner and ${product} product bulk loan template downloaded

Create a Bulk loan
   [Documentation]  Bulk loan creation using product details
   Click Element    xpath:(//fa-icon[@class='ng-fa-icon mr-05'])[8]
   Sleep    1s
   Click Element    xpath://button[text()='Products']
   Sleep    1s
   Click Element    xpath://h4[text()='Loan Products']
   Sleep    1s
   Click Element    xpath://span[text()='10']
   Sleep    1s
   Click Element    xpath://span[text()=' 50 ']
   Sleep    1s
   Click Element    xpath://td[text()=' ${product} ']
   Sleep    1s
   Click Element    xpath://fa-icon[@icon='edit']
   Sleep    2s
   Click Element    xpath://*[text()='TERMS']
   Sleep    1s
   # TODO: Getting the Principal,Loan Term,interest rate from Product
   ${minPrincipal}=      Get Value    xpath://input[@formcontrolname='minPrincipal']
   ${maxPrincipal}=      Get Value    xpath://input[@formcontrolname='maxPrincipal']
   ${minRepayPeriod}=    Get value    xpath://input[@formcontrolname='minNumberOfRepayments']
   ${maxRepayPeriod}=    Get value    xpath://input[@formcontrolname='maxNumberOfRepayments']
   ${minInterest}=       Get value    xpath://input[@formcontrolname='minInterestRatePerPeriod']
   ${minInterestSet}     Set Variable     ${minInterest}
   ${maxInterest}=       Get value    xpath://input[@formcontrolname='maxInterestRatePerPeriod']
   Click Element         xpath://*[text()='LOAN SIGNATURE']
   Sleep    1s
   IF  '${partner}' == 'NOCPL'
     ${feesCount}    Set Variable     2
   ELSE
     ${feesCount}    Set Variable     5
   END
   ${loopFees}       Set Variable     1
   FOR  ${feesNum}  IN RANGE    1     ${feesCount}
     ${feesName}=    Get Text    xpath:(//mat-select[@formcontrolname='colendingFees'])[${feesNum}]
    #  ${feesStatus2}=    Run Keyword And Return Status    Element Should Be Visible    xpath:(//mat-select[@formcontrolname='colendingFees'])[2]
    #  ${feesName2}=    Run Keyword If    '${feesStatus2}' == 'True'   Get Text    xpath:(//mat-select[@formcontrolname='colendingFees'])[2]
     Set Global Variable   ${feesName}${feesNum}     ${feesName}
   END
   FOR  ${i}  ${f}  IN ENUMERATE    ${feesName{feesNum}}
     Click Element    xpath:(//fa-icon[@class='ng-fa-icon mr-05'])[8]
     Sleep    1s
     Click Element    xpath://button[text()='Products']
     Sleep    1s
     Click Element    xpath://h4[text()='Fee & Charges']
     Sleep    2s
     Log    ${i}
     Click Element    xpath://td[contains(text(),' ${f} ')]
     Sleep    1s
     Click Element    xpath://fa-icon[@icon='edit']
     Sleep   1s
     ${minFees1}=    Get Value    xpath://input[@formcontrolname='minChargeAmount']
     ${minFees1Set}   Set Variable    ${minFees1}
     ${maxFees1}=    Get Value    xpath://input[@formcontrolname='maxChargeAmount']
   END
   
#    ${feesName1}=    Get Text    xpath:(//mat-select[@formcontrolname='colendingFees'])[1]
#    ${feesStatus2}=    Run Keyword And Return Status    Element Should Be Visible    xpath:(//mat-select[@formcontrolname='colendingFees'])[2]
#    ${feesName2}=    Run Keyword If    '${feesStatus2}' == 'True'   Get Text    xpath:(//mat-select[@formcontrolname='colendingFees'])[2]
#    Set Global Variable    ${feesName2}
#    ${chargeName1}=    Get Text   xpath:(//mat-select[@formcontrolname='colendingCharge'])[1]
#    ${chargeStatus2}=    Run Keyword And Return Status    Element Should Be Visible    xpath:(//mat-select[@formcontrolname='colendingCharge'])[2]
#    ${chargeName2}=    Run Keyword If    '${chargeStatus2}' == 'True'    Get Text    xpath:(//mat-select[@formcontrolname='colendingCharge'])[2]
#    Set Global Variable    ${chargeName2}
#    Click Element    xpath:(//fa-icon[@class='ng-fa-icon mr-05'])[8]
#    Sleep    1s
#    Click Element    xpath://button[text()='Products']
#    Sleep    1s
#    Click Element    xpath://h4[text()='Fee & Charges']
#    Sleep    2s
#    Click Element    xpath://td[contains(text(),' ${feesName{feesNum}} ')]
#    Sleep    1s
#    Click Element    xpath://fa-icon[@icon='edit']
#    Sleep   1s
#    ${minFees1}=    Get Value    xpath://input[@formcontrolname='minChargeAmount']
#    ${minFees1Set}   Set Variable    ${minFees1}
#    ${maxFees1}=    Get Value    xpath://input[@formcontrolname='maxChargeAmount']
#    Click Element    xpath://a[text()='Charges']
#    Sleep    2s
#    Click Element    xpath://td[contains(text(),' ${chargeName1} ')]
#    Sleep    1s
#    Click Element    xpath://fa-icon[@icon='edit']
#    Sleep   2s
#    ${minCharge1}=    Get Value    xpath://input[@formcontrolname='minChargeAmount']
#    ${minCharge1Set}    Set Variable    ${minCharge1}
#    ${maxCharge1}=    Get Value    xpath://input[@formcontrolname='maxChargeAmount']
#    Run Keyword If    '${feesStatus2}' == 'True'     Add one more fees
#    Run Keyword If    '${chargeStatus2}' == 'True'   Add one more charge
    #    ${minOdCharge}=   Set Variable    ${1}              #Overdue charge min & max
    #    ${maxOdCharge}=   Set Variable    ${10}
   Process Recent Excel File
   Set Global Variable   ${file_path}
   Open Workbook  ${file_path}
   ${extId}         Generate Random String     3    [UPPER]
   ${middleName}    Generate Random String     2    [UPPER]
   ${lastName}      Generate Random String     2    [UPPER]
   FOR    ${index}    IN RANGE    1    ${loopCount}
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
    ${gender}    Create List        Male    Female
    ${gender}    random.choice      ${gender}
    Write To Cell    J${index+1}    ${gender}
    ${aadharNum}=    FakerLibrary.Numerify    text=###########
    ${pan1}          Generate Random String     5    [UPPER]
    ${pan2}=         FakerLibrary.Numerify    text=####
    Write To Cell    L${index+1}    ${pan1}${pan2}B    
    Write To Cell    M${index+1}    7${aadharNum} 
    Write To Cell    N${index+1}    ZAP0766072
    Write To Cell    O${index+1}    N8369845
    Write To Cell    P${index+1}    DB56 20130123456
    ${address}=      FakerLibrary.Address
    Write To Cell    Q${index+1}    ${address}
    Write To Cell    R${index+1}    ${816125}
    Write To Cell    T${index+1}    KEYAN@gmail.com
    ${mobNum}=       FakerLibrary.Numerify    text=######### 
    Write To Cell    S${index+1}    7${mobNum}
    Write To Cell    U${index+1}    Chennai
    ${state}    Create List    Jammu and Kashmir  Himachal Pradesh  Punjab  Chandigarh  Uttarakhand  Haryana  Delhi  Rajasthan  Uttar pradesh  Bihar  Sikkim  Arunachal pradesh  Nagaland  Manipur  Mizoram  Tripura  Meghalaya  Assam  West Bengal  Jharkhand  Odisha  Chattisgarh  Madhya pradesh  Gujarat  Dadra and Nagar Haveli and Daman and Diu  Maharashtra  Andhra Pradesh  Karnataka  Lakshadweep  Kerala  Tamil Nadu  Puducherry  Andaman and Nicobar Islands  Telangana  Ladakh
    ${state}    random.choice       ${state}
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
   #  If the Pricipal,fees,loan term and Interest reaches max again it will start from min value
    ${principal}=    Evaluate    (${minPrincipal} + (${index} * 1000)) % (${maxPrincipal} - ${minPrincipal}) + ${minPrincipal}
    Write To Cell    AH${index+1}     ${principal}
    ${repayPeriod}=    Evaluate    (${minRepayPeriod} + (${index} * 100)) % (${maxRepayPeriod} - ${minRepayPeriod}) + ${minRepayPeriod}
    Write To Cell    AI${index+1}     ${repayPeriod}
    # ${repayPeriod}    Create List     ${12}  ${14}  ${16}  ${18}  ${20}  ${22}  ${24}  ${30}  ${34}  
    # ${repayPeriod}    random.Choice    ${repayPeriod}
    # Write To Cell     AI${index+1}     ${repayPeriod}
    ${minInterest}     Evaluate        ${minInterest}+${0.5}
    IF  ${minInterest} >= ${maxInterest}
        ${minInterest}    Set Variable    ${minInterestSet}
    END
    Write To Cell    AL${index+1}     ${minInterest}
   # Increment the fee value by 100
    ${minFees1}=    Evaluate    ${minFees1} + 100
   # Check if the fee value has reached the maximum if its set minfees value
    IF  ${minFees1} >= ${maxFees1}
        ${minFees1}    Set Variable    ${minFees1Set}
    END  
    Write To Cell    AM${index+1}     ${minFees1}
    IF  '${feesStatus2}' == 'True'
        ${minFees2}     Evaluate    ${minFees2} + 1
        Run Keyword If    ${minFees2} >= ${maxFees2}     Set Variable   ${minFees2}    
        Write To Cell    AO${index+1}     ${minFees2}
    END
    ${minCharge1}=    Evaluate    ${minCharge1} + 10
    IF  ${minCharge1} >= ${maxCharge1}
        ${minCharge1}    Set Variable    ${minCharge1Set}
    END 
    Write To Cell    AN${index+1}     ${minCharge1}
    IF  '${chargeStatus2}' == 'True'
        ${mincharge2}    Evaluate    ${mincharge2} + 1
        Run Keyword If    ${mincharge2} >= ${maxcharge2}    Set Variable    ${mincharge2}    ${mincharge2}
        Write To Cell    AP${index+1}     ${mincharge2}
    END
    # ${odCharge}=    Evaluate    (${minOdCharge} + (${index} * 1000)) % (${maxOdCharge} - ${minOdCharge}) + ${minOdCharge}
    # Write To Cell    AO${index+1}     ${odCharge}
   END
  Write To Cell    AM2     ${0}
  Write To Cell    AN2     ${0}
  Write To Cell    AM3     ${0}
   Save
   Close Workbook
   Sleep    3s

Add one more fees
   Click Element    xpath://a[text()='Charges']
   Sleep    1s
   Click Element    xpath://td[contains(text(),' ${feesName2} ')]
   Sleep    1s
   Click Element    xpath://fa-icon[@icon='edit']
   Sleep   1s
   ${minFees2}=    Get Value    xpath://input[@formcontrolname='minChargeAmount']
   Set Global Variable    ${minFees2}
   ${maxFees2}=    Get Value    xpath://input[@formcontrolname='maxChargeAmount']
   Set Global Variable    ${maxFees2}

Add one more charge
   Click Element    xpath://a[text()='Charges']
   Sleep    1s
   Click Element    xpath://td[contains(text(),' ${chargeName2} ')]
   Sleep    1s
   Click Element    xpath://fa-icon[@icon='edit']
   Sleep   1s
   ${minCharge2}=    Get Value    xpath://input[@formcontrolname='minChargeAmount']
   Set Global Variable    ${minCharge2}
   ${maxCharge2}=    Get Value    xpath://input[@formcontrolname='maxChargeAmount']
   Set Global Variable    ${maxCharge2}
   
Upload the created excel loan
   [Documentation]    Bulk loan upload
   Set Selenium Speed  0.3s
   Process Recent Excel File
   Set Global Variable   ${file_path}
   01__loginPage.Open Browser and login to Zeus     ${userName}        ${password}
   Click Element    xpath://a[@href='#/organization/bulk-import']
   Click Element    xpath://h4[text()='Bulk Loan ']
   Sleep    2s
   Click Element    xpath://mat-select[@formcontrolname='partnerId']
   Click Element    xpath://span[contains(text(),'${partner}')]
   Click Element    xpath://mat-select[@formcontrolname='productId']
   Click Element    xpath://span[contains(text(),'${product}')]
   Sleep    2s
   Execute JavaScript    document.querySelector('input[type="file"]').style.display = 'block'
   Sleep    2s
   Choose File      xpath://input[@type='file']    ${file_path}    
   Click Element    xpath://i[@class='fa fa-upload']
#    Sleep    70s
#    Scroll Element Into View    xpath://span[text()=' Download ']
   Capture Page Screenshot
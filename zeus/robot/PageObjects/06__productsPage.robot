*** Settings ***
Resource           ../keywords/common.robot
Resource           ../PageObjects/04__partnerPage.robot

*** Variables ***
${ProductName}=        Regress Loan 26
${shortName}=          RL26
${desc}=               ok
${exId}=               104

${decDigit}=           0
${minPrincipal}=       50000
${defPrincipal}=       100000
${maxPrincipal}=       200000
${minNOP}=             10
${defNOP}=             12
${maxNOP}=             24
${minInterest}=        3
${defInterest}=        5
${maxInterest}=        8
${repayEvery}=         5
${minDay}=             15

${Amortization}=               Equal installments 
${interestMethod}=             Declining Balance
${interestCalPeriod}=          Same as repayment period
${brokenInterestStrategy}=     Nobroken
${daysInYearType}=             Actual
${daysInMonthType}=            Actual
${selfPrincipalShare}=         90
${selfFeesShare}=              90
${selfPenaltyShare}=           90
${selfOverpaidShare}=          90
${clientInterest}=             5
${selfInterest}=               3
${partnerPrincipalShare}=      10
${partnerFeesShare}=           10
${partnerPenaltyShare}=        10
${partnerOverpaidShare}=       10
${partnerInterest}=            2
${partner}=                    ${getpartnerName}

*** Keywords ***
Create new product
    [Documentation]    Create a new Product in Zeus
    Set Selenium Speed    0.4 seconds
    [Arguments]      ${ProductName}  ${shortName}  ${desc}  ${decDigit}  ${minPrincipal}  ${defPrincipal}  ${maxPrincipal}  ${minNOP}  ${defNOP}  ${maxNOP}  ${minInterest}  ${defInterest}  ${maxInterest}  ${repayEvery}  ${minDay}  ${Amortization}  ${interestMethod}  ${interestCalPeriod}  ${brokenInterestStrategy}  ${daysInYearType}  ${daysInMonthType}  ${selfPrincipalShare}  ${selfFeesShare}  ${selfPenaltyShare}  ${selfOverpaidShare}  ${clientInterest}  ${selfInterest}  ${partnerPrincipalShare}  ${partnerFeesShare}  ${partnerPenaltyShare}  ${partnerOverpaidShare}  ${partnerInterest}    ${partner}
    Click Element    xpath:(//fa-icon[@class='ng-fa-icon mr-05'])[5]
    # Sleep    2
    Click Element    xpath://button[text()='Products']
    # Sleep    2
    Click Element    xpath://h4[text()='Loan Products']
    Sleep    1
    Click Element    xpath://span[contains(text(),'Create Loan Product ')]
    Sleep    1
    Input Text       xpath://input[@formcontrolname='name']        ${ProductName}
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='shortName']    ${shortName}
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='classId']
    # Sleep    2
    Click Element    xpath://span[text()=' Asset ']
    # Sleep    2
    Click Element    xpath://mat-select[@formcontrolname='typeId']
    # Sleep    1
    Click Element    xpath://span[text()=' Co-Lending ']
    # Sleep    2
    Click Element    xpath://input[@formcontrolname='startDate']
    # Sleep    1
    Click Element    xpath://div[text()=' 1 ']
    # Sleep    2
    Click Element    xpath://input[@formcontrolname='closeDate']
    # Sleep    2
    Click Element    xpath://span[text()='FEB 2023']
    # Sleep    2
    Click Element    xpath://div[text()=' 2024 ']
    # Sleep    2
    Click Element    xpath://div[text()=' JAN ']
    # Sleep    2
    Click Element    xpath://div[text()=' 2 ']
    # Sleep    2
    Input Text       xpath://textarea[@formcontrolname='description']    ${desc}
    # Sleep    2    
    Click Element    xpath:(//button[@type='submit'])[1]

    # Currency page
    Sleep    2
    Click Element    xpath://mat-select[@formcontrolname='currencyCode']
    # Sleep    1
    Click Element    xpath://span[text()=' Indian Rupee ']
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='digitsAfterDecimal']        ${decDigit}
    Input Text       xpath://input[@formcontrolname='installmentAmountInMultiplesOf']    1
    # Sleep    2
    Click Element    xpath:(//button[@type='submit'])[2]
    Sleep    2
    
    #Terms
    Input Text    xpath://input[@formcontrolname='minPrincipal']    ${minPrincipal}
    # Sleep    1
    Input Text    xpath://input[@formcontrolname='principal']       ${defPrincipal}
    # Sleep    1
    Input Text    xpath://input[@formcontrolname='maxPrincipal']    ${maxPrincipal}
    # Sleep    2
    Input Text    xpath://input[@formcontrolname='minNumberOfRepayments']    ${minNOP}
    # Sleep    1
    Input Text    xpath://input[@formcontrolname='numberOfRepayments']       ${defNOP}
    # Sleep    1
    Input Text    xpath://input[@formcontrolname='maxNumberOfRepayments']    ${maxNOP}
    # Sleep    1
    Input Text    xpath://input[@formcontrolname='minInterestRatePerPeriod']    ${minInterest}
    # Sleep    1
    Input Text    xpath://input[@formcontrolname='interestRatePerPeriod']       ${defInterest}
    # Sleep    1
    Input Text    xpath://input[@formcontrolname='maxInterestRatePerPeriod']    ${maxInterest}
    # Sleep    1
    Input Text    xpath://input[@formcontrolname='repaymentEvery']            ${repayEvery}
    # Sleep    1
    Input Text    xpath://input[@formcontrolname='minimumDaysBetweenDisbursalAndFirstRepayment']    ${minDay}
    # Sleep    1
    Click Element    xpath://span[text()=' Select Accepted Dates ']
    # Sleep    2
    Click Element    xpath://mat-select[@formcontrolname='acceptedDateType']
    # Sleep    1
    Click Element    xpath://span[text()=' Range ']
    # Sleep    1
    Input Text    xpath://input[@formcontrolname='acceptedStartDate']    1
    # Sleep    1
    Input Text    xpath://input[@formcontrolname='acceptedEndDate']    5
    # Sleep    1
    Click Element    xpath://span[text()=' Apply Prepaid Locking Period ']
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='prepayLockingPeriod']    5
    # Sleep    1
    Click Element    xpath://span[text()=' Apply Foreclosure Locking Period ']
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='foreclosureLockingPeriod']    5
    # Sleep    1
    Click Element    xpath:(//button[@type='submit'])[3]
    Sleep    2

    # Settings
    # Click Element    xpath://div[text()='SETTINGS']
    # Sleep    3
    Click Element    xpath://mat-select[@formcontrolname='amortizationType']
    # Sleep    1
    Click Element    xpath://span[text()=' ${Amortization} ']
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='interestType']
    # Sleep    1
    Click Element    xpath://span[text()=' ${interestMethod} ']
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='interestCalculationPeriodType']
    # Sleep    1
    Click Element    xpath://span[text()=' ${interestCalPeriod} ']
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='brokenInterestCalculationPeriod']
    # Sleep    1
    Click Element    xpath://span[text()=' Actual ']
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='brokenInterestStrategy']
    # Sleep    1
    Click Element    xpath://span[contains(text(),'${brokenInterestStrategy} ')]
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='repaymentStrategyForNpaId']
    # Sleep    1
    Click Element    xpath://span[contains(text(),'Strategy1')]
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='loanForeclosureStrategy']
    # Sleep    1
    Click Element    xpath://span[contains(text(),'Loan Strategy1')]
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='daysInYearType']
    Sleep    1
    Click Element    xpath://span[contains(text(),' ${daysInYearType} ')]
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='daysInMonthType']
    # Sleep    2
    Click Element    xpath://span[contains(text(),' ${daysInMonthType} ')]
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='brokenInterestDaysInYears']
    Sleep    1
    Click Element    xpath://mat-option[@value='Actual']
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='brokenInterestDaysInMonth']
    # Sleep    1
    Click Element    xpath://mat-option[@value='Actual']
    # Sleep    1
    Click Element    xpath://span[text()=' Use Days In month 30 for loan Provisioning ']
    # Sleep    1
    Click Element    xpath://span[text()=' Divide by thirty for partial period ']
    # Sleep    1
    Click Element    xpath://span[text()=' Enable Co-Lending Loan ']
    # Sleep    1
    Click Element    xpath://span[text()=' By Percentage Split ']
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='selfPrincipalShare']    ${selfPrincipalShare}
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='selfFeeShare']    ${selfFeesShare}
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='selfPenaltyShare']    ${selfPenaltyShare}
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='selfOverpaidShares']    ${selfOverpaidShare}
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='interestRate']    ${clientInterest}
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='selfInterestRate']    ${selfInterest}
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='partnerId']
    # Sleep    1
    Click Element    xpath://span[text()=' ${partner} ']
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='partnerPrincipalShare']    ${partnerPrincipalShare}
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='partnerFeeShare']    ${partnerFeesShare}
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='partnerPenaltyShare']    ${partnerPenaltyShare}
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='partnerOverpaidShare']    ${partnerOverpaidShare}
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='partnerInterestRate']    ${partnerInterest}
    # Sleep    1
    Click Element    xpath://span[text()=' Enable Charge Wise Bifarcation ']
    # Sleep    1
    Click Element    xpath://mat-select[@formcontrolname='colendingCharge']
    # Sleep    1
    Click Element    xpath://span[text()=' ServiceProcessingfee ']
    # Sleep    1
    Input Text       xpath://input[@formcontrolname='partnerCharge']    100
    # Sleep    1
    Click Element    xpath:(//button[@type='submit'])[4]
    Sleep    2

    # Charges
    # Click Element    xpath:(//mat-select[@role='combobox'])[19]
    # Sleep    1
    # Click Element    xpath://span[text()=' Processing Fee ']
    # Sleep    1
    # Click Element    xpath:(//span[contains(text(),'Add ')])[1]
    # Sleep    1
    Click Element    xpath:(//button[@type='submit'])[5]
    Sleep    2

Verify valid product onboarding
    Set Selenium Speed    0.4 seconds
    [Documentation]    verify valid product onboarding
    Click Element    xpath:(//button[@type='submit'])[6]
    Sleep    1
    # Pause Execution
    Click Element    xpath://span[text()=' Submit ']
    Sleep    2    
    Scroll Element Into View    xpath://h2[text()='${ProductName}']
    Element Should Be Visible   xpath://h2[text()='${ProductName}']
    ${productStatus}=   Run Keyword And Return Status   Element Should Be Visible  xpath://div[text()=' Loan product may only have one charge of each type.` ']
    # Wait Until Keyword Succeeds    '${status}==True'    timeout=5s    Element Should Be Visible  xpath://div[text()=' Loan product may only have one charge of each type.` ']
    Run Keyword If    '${productStatus}' == 'True'     Check alredy product onboarded    ELSE     Log     New Product onboarded into Zeus
    # Sleep   5

Invalid product onboarding
    [Documentation]    Invalid product onboarding
    Set Selenium Speed    0.4 seconds
    Click Element    xpath:(//button[@type='submit'])[6]
    # Sleep    3
    Click Element    xpath://span[text()=' Submit ']
    # Sleep    4
    Element Should Be Visible       xpath:(//span[text()='Create'])[2]
    Element Should Not Be Visible   xpath://h2[text()='${ProductName}']
    Log     New Product Not onboarded into Zeus
    # Sleep   5

Check alredy product onboarded
    [Documentation]    Check alredy product onboarded
    Capture Page Screenshot
    Set Selenium Speed    0.4 seconds
    Click Element    xpath:(//fa-icon[@class='ng-fa-icon mr-05'])[5]
    # Sleep    2
    Click Element    xpath://button[text()='Products']
    # Sleep    2
    Click Element    xpath://h4[text()='Loan Products']
    Sleep    2
    Click Element    xpath://mat-select[@aria-label='Items per page:']
    # Sleep    1
    Click Element    xpath://span[text()=' 100 ']
    Sleep    2
     Input Text    xpath:/html/body/mifosx-web-app/mifosx-shell/mat-sidenav-container/mat-sidenav-content/mifosx-content/mifosx-offices/div[2]/div[1]/mat-form-field/div/div[1]/div/input    ${ProductName}
    Scroll Element Into View    xpath://td[text()=' ${ProductName} ']
    Click Element               xpath://td[text()=' ${ProductName} ']
    Sleep   2
    Scroll Element Into View    xpath://h2[text()='${ProductName}']
    Capture Page Screenshot
    Log    Already this product onboarded into Zeus    level=ERROR

Create new product and verify
    [Arguments]    ${ProductName}  ${shortName}  ${desc}  ${decDigit}  ${minPrincipal}  ${defPrincipal}  ${maxPrincipal}  ${minNOP}  ${defNOP}  ${maxNOP}  ${minInterest}  ${defInterest}  ${maxInterest}  ${repayEvery}  ${minDay}  ${Amortization}  ${interestMethod}  ${interestCalPeriod}  ${brokenInterestStrategy}  ${daysInYearType}  ${daysInMonthType}  ${selfPrincipalShare}  ${selfFeesShare}  ${selfPenaltyShare}  ${selfOverpaidShare}  ${clientInterest}  ${selfInterest}  ${partnerPrincipalShare}  ${partnerFeesShare}  ${partnerPenaltyShare}  ${partnerOverpaidShare}  ${partnerInterest}    ${partner}    
    Set Selenium Speed    0.2 seconds
    Create new product    ${ProductName}  ${shortName}  ${desc}  ${decDigit}  ${minPrincipal}  ${defPrincipal}  ${maxPrincipal}  ${minNOP}  ${defNOP}  ${maxNOP}  ${minInterest}  ${defInterest}  ${maxInterest}  ${repayEvery}  ${minDay}  ${Amortization}  ${interestMethod}  ${interestCalPeriod}  ${brokenInterestStrategy}  ${daysInYearType}  ${daysInMonthType}  ${selfPrincipalShare}  ${selfFeesShare}  ${selfPenaltyShare}  ${selfOverpaidShare}  ${clientInterest}  ${selfInterest}  ${partnerPrincipalShare}  ${partnerFeesShare}  ${partnerPenaltyShare}  ${partnerOverpaidShare}  ${partnerInterest}    ${partner}
    Verify valid product onboarding
    Capture Page Screenshot
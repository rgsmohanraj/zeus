*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot

*** Keywords ***
TC_RS_partner
    ${pos_partner}=  Set Variable    ${p_partner}

    ${int_partner_sum}=       Evaluate    0
    ${p_partner_sum}=         Evaluate    0
    ${emi_partner_sum}=       Evaluate    0
    ${ots_partner_sum}=       Evaluate    0

    Sleep    1

    Click Element    xpath://a[contains(text(),'Repayment Schedule')]
    Click Element    xpath://div[contains(text(),'Partner')]

    # Disbursement line in Partner RS
    Sleep    2
    ${pos_in_disb_partner_value}=    Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-balanceOfLoan mat-column-balanceOfLoan ng-star-inserted']
    ${pos_in_disb_partner_value}=    Custom Modify Number    ${pos_in_disb_partner_value}
    Should Be Equal As Numbers    ${pos_in_disb_partner_value}    ${pos_partner}
    ${emi_in_disb_partner}=    Set Variable    ${charge_partner_RS}
    ${fees_in_disb_partner}=    Set Variable    ${charge_partner_RS}
    ${emi_in_disb_partner_value}=     Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-due mat-column-due ng-star-inserted']
    ${fees_in_disb_partner_value}=    Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-fees mat-column-fees ng-star-inserted']
    ${emi_in_disb_partner_value}=     Custom Modify Number    ${emi_in_disb_partner_value}
    ${fees_in_disb_partner_value}=    Custom Modify Number    ${fees_in_disb_partner_value}
    Should Be Equal As Numbers    ${emi_in_disb_partner}     ${emi_in_disb_partner_value}
    Should Be Equal As Numbers    ${fees_in_disb_partner}    ${fees_in_disb_partner_value}

        IF  ${result_disbursed}==True
            ${paid_in_disb_partner}=    Set Variable    ${charge_partner_RS}
            ${paid_in_disb_partner_value}=    Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-paid mat-column-paid ng-star-inserted']
            ${paid_in_disb_partner_value}=    Custom Modify Number    ${paid_in_disb_partner_value}
            Should Be Equal As Numbers    ${paid_in_disb_partner_value}    ${paid_in_disb_partner}
            ${ots_in_disb_partner}=    Set Variable    0
        ELSE IF    ${result_not_disbursed}==True
            ${paid_in_disb_partner}=    Set Variable    0
            ${ots_in_disb_partner}=    Set Variable    ${charge_partner_RS}
            ${ots_in_disb_partner_value}=    Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-outstanding mat-column-outstanding ng-star-inserted']
            ${ots_in_disb_partner_value}=    Custom Modify Number    ${ots_in_disb_partner_value}
            Should Be Equal As Numbers    ${ots_in_disb_partner_value}    ${ots_in_disb_partner_value}
        END
        Log To Console    \n Partner - Disbursement Line - Due-${emi_in_disb_partner}, Principal O/S-${pos_partner}, Fees-${fees_in_disb_partner}, Paid- ${paid_in_disb_partner}, Outstanding - ${ots_in_disb_partner}

        Write To Cell    AR21   ${fees_in_disb_partner}
        Write To Cell    AQ21   ${paid_in_disb_partner}
        Write To Cell    AT21   ${ots_in_disb_partner}

    FOR  ${i}  IN RANGE    1    ${${tenure}+${1}}
        Write To Cell    AJ${cc}    ${i}          #s.no
        Write To Cell    AK${cc}    ${days_list}[${i}]        #days
        Write To Cell    AL${cc}    ${date_list}[${i}]        #Date

        #DPD Calculation
        IF  ${dpd_list}[${i}]>0
            IF  ${result_disbursed}==True
                Sleep    0.3
                ${dpd_partner_value}=    Get Text    xpath:(//td[@class='mat-cell cdk-cell cdk-column-dpd mat-column-dpd ng-star-inserted'])[${${i}+${1}}]
                ${dpd_partner_value}=    Custom Modify Number    ${dpd_partner_value}
                Should Be Equal As Numbers    ${dpd_partner_value}    ${dpd_list}[${i}] 
                Write To Cell    AS${cc}    ${dpd_list}[${i}]
            END
        END

        # Partner RS Calculation
        ${int_partner_mon}=    Evaluate    ${int_list_cl}[${i}]-${int_list_self}[${i}]
        ${p_partner_mon}=      Evaluate    ${p_list_cl}[${i}]-${p_list_self}[${i}]
        ${emi_partner_mon}=    Evaluate    ${int_partner_mon}+${p_partner_mon}
        ${pos_partner_mon}=    Evaluate    ${pos_partner}-${p_partner_mon}
        ${ots_partner_mon}=    Set Variable    ${emi_partner_mon}
        Write To Cell    AP${cc}    ${int_partner_mon}
        Write To Cell    AO${cc}    ${p_partner_mon}
        Write To Cell    AN${cc}    ${emi_partner_mon}
        Write To Cell    AM${cc}    ${pos_partner_mon}
        Write To Cell    AT${cc}    ${ots_partner_mon}
        # Partner RS Sum Calculation
        ${int_partner_sum}=    Evaluate    ${int_partner_sum}+${int_partner_mon}
        ${p_partner_sum}=      Evaluate    ${p_partner_sum}+${p_partner_mon}
        ${emi_partner_sum}=    Evaluate    ${emi_partner_sum}+${emi_partner_mon}
        ${ots_partner_sum}=    Evaluate    ${ots_partner_sum}+${ots_partner_mon}

        #RS_partner_validation
        ${s.no_partner_value}=          Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-number mat-column-number ng-star-inserted'])[${${i}+${1}}]
        ${days_partner_value}=          Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-days mat-column-days ng-star-inserted'])[${${i}+${1}}]
        ${int_partner_mon_value}=       Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-interest mat-column-interest ng-star-inserted'])[${${i}+${1}}]
        ${p_partner_mon_value}=         Get Text    xpath:(//*[@class='mat-cell cdk-cell check cdk-column-principalDue mat-column-principalDue ng-star-inserted'])[${${i}+${1}}]
        ${emi_partner_month_value}=     Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-due mat-column-due ng-star-inserted'])[${${i}+${1}}]
        ${pos_partner_value}=           Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-balanceOfLoan mat-column-balanceOfLoan ng-star-inserted'])[${${i}+${1}}]
        ${ots_partner_mon_value}=       Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-outstanding mat-column-outstanding ng-star-inserted'])[${${i}+${1}}]    
        # ${dpd_value}=         Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-dpd mat-column-dpd ng-star-inserted'])[${${i}+${1}}]    

        ${int_partner_mon_value}=        Custom Modify Number    ${int_partner_mon_value}
        ${p_partner_mon_value}=          Custom Modify Number    ${p_partner_mon_value}
        ${emi_partner_month_value}=      Custom Modify Number    ${emi_partner_month_value}
        ${pos_partner_value}=            Custom Modify Number    ${pos_partner_value}
        ${ots_partner_mon_value}=        Custom Modify Number    ${ots_partner_mon_value}

        Should Be Equal As Numbers    ${s.no_partner_value}            ${i}
        Should Be Equal As Numbers    ${days_partner_value}            ${days_list}[${i}]
        Should Be Equal As Numbers    ${int_partner_mon_value}         ${int_partner_mon}
        Should Be Equal As Numbers    ${p_partner_mon_value}           ${p_partner_mon}
        Should Be Equal As Numbers    ${emi_partner_month_value}       ${emi_partner_mon}
        Should Be Equal As Numbers    ${pos_partner_value}             ${pos_partner}
        Should Be Equal As Numbers    ${ots_partner_mon_value}         ${ots_partner_mon}

        Log To Console    \n ${client_name} - Partner - S.No-${i}, Days-${days_list}[${i}], Due-${emi_partner_mon}, PrincipalDue-${p_partner_mon}, Interest-${int_partner_mon}, Principal O/S-${pos_partner}, Outstanding-${ots_partner_mon}

        #next line
        ${cc}=    Evaluate    ${cc}+1            #s.no for next line
    END

    ${emi_partner_sum+c}=    Evaluate    ${emi_partner_sum}+${charge_partner_RS}
    ${ots_partner_sum+c}=    Evaluate    ${ots_partner_sum}+${ots_in_disb_partner}
    ${total_partner_fees}=    Set Variable    ${fees_in_disb_partner}
    ${total_partner_paid}=    Set Variable    ${paid_in_disb_partner}
    Log    \nTotal partner Interest:${int_partner_sum}
    Log    \nTotal partner Principal:${p_partner_sum}
    Log    \nTotal partner EMI_withoutCharge:${emi_partner_sum+c}

    #total lines in RS_excel
    Write To Cell    AP${${tc}+${n}}    ${int_partner_sum}
    Write To Cell    AO${${tc}+${n}}    ${p_partner_sum}   
    Write To Cell    AN${${tc}+${n}}    ${emi_partner_sum+c}
    Write To Cell    AT${${tc}+${n}}    ${ots_partner_sum+c}
    Write To Cell    AR${${tc}+${n}}    ${total_partner_fees}
    Write To Cell    AQ${${tc}+${n}}    ${total_partner_paid}

    # total line in RS --> partner
    IF  ${result_disbursed}==True
        ${total_partner_paid_value}=    Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-paid mat-column-paid ng-star-inserted']
        ${total_partner_paid_value}=    Custom Modify Number        ${total_partner_paid_value}
        Should Be Equal As Numbers    ${total_partner_paid_value}    ${total_partner_paid}
    END
    
    ${total_partner_emi_value}=     Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-due mat-column-due ng-star-inserted']
    ${total_partner_p_value}=       Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell check cdk-column-principalDue mat-column-principalDue ng-star-inserted']
    ${total_partner_int_value}=     Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-interest mat-column-interest ng-star-inserted']
    ${total_partner_fees_value}=    Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-fees mat-column-fees ng-star-inserted']
    ${total_partner_ots_value}=     Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-outstanding mat-column-outstanding ng-star-inserted']
    ${total_partner_emi_value}=     Custom Modify Number        ${total_partner_emi_value}
    ${total_partner_p_value}=       Custom Modify Number        ${total_partner_p_value}
    ${total_partner_int_value}=     Custom Modify Number        ${total_partner_int_value}
    ${total_partner_fees_value}=    Custom Modify Number        ${total_partner_fees_value}
    ${total_partner_ots_value}=     Custom Modify Number        ${total_partner_ots_value}

    Should Be Equal As Numbers    ${total_partner_emi_value}     ${emi_partner_sum+c}
    Should Be Equal As Numbers    ${total_partner_p_value}       ${p_partner_sum}
    Should Be Equal As Numbers    ${total_partner_int_value}     ${int_partner_sum}
    Should Be Equal As Numbers    ${total_partner_fees_value}    ${total_partner_fees}
    Should Be Equal As Numbers    ${total_partner_ots_value}     ${ots_partner_sum+c}
    
    Log To Console    \n Partner Total: Due-${emi_partner_sum+c}, PrincipalDue-${p_partner_sum}, Interest-${int_partner_sum}, Fees-${total_partner_fees}, Paid-${total_partner_paid}, Outstanding-${ots_partner_sum+c}

    Log To Console    \n RS - Partner validated

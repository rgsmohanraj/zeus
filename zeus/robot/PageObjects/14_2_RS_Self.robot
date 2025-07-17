*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/14_2_RS_Client.robot

*** Keywords ***
TC_RS_self

    ${p_self}=       Evaluate    ${p}*${principal_self_split%}/100

    ${int_self_sum}=       Evaluate    0
    ${p_self_sum}=         Evaluate    0
    ${emi_self_sum}=       Evaluate    0
    ${ots_self_sum}=       Evaluate    0

    Sleep    1

    Click Element    xpath://a[contains(text(),'Repayment Schedule')]
    Click Element    xpath://div[contains(text(),'Self')]

    ${pos_self}=           Set Variable    ${p_self}

        # Disbursement line in Self RS
        Sleep    2
        ${pos_in_disb_self_value}=    Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-balanceOfLoan mat-column-balanceOfLoan ng-star-inserted']
        ${pos_in_disb_self_value}=    Custom Modify Number    ${pos_in_disb_self_value}
        Log    \n pos_self: ${pos_self}
        Should Be Equal As Numbers    ${pos_in_disb_self_value}    ${pos_self}

        IF  ${result_disbursed}==True
            ${paid_in_disb_self}=    Set Variable    ${charge_self_RS}
            ${paid_in_disb_self_value}=    Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-paid mat-column-paid ng-star-inserted']
            ${paid_in_disb_self_value}=    Custom Modify Number    ${paid_in_disb_self_value}
            Should Be Equal As Numbers    ${paid_in_disb_self_value}    ${paid_in_disb_self}
            ${ots_in_disb_self}=    Set Variable    0
        ELSE IF    ${result_not_disbursed}==True
            ${paid_in_disb_self}=    Set Variable    0
            ${ots_in_disb_self}=    Set Variable    ${charge_self_RS}
            ${ots_in_disb_self_value}=    Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-outstanding mat-column-outstanding ng-star-inserted']
            ${ots_in_disb_self_value}=    Custom Modify Number    ${ots_in_disb_self_value}
            Should Be Equal As Numbers    ${ots_in_disb_self_value}    ${ots_in_disb_self_value}
        END

        ${emi_in_disb_self}=    Set Variable    ${charge_self_RS}
        ${fees_in_disb_self}=    Set Variable    ${charge_self_RS}
        ${emi_in_disb_self_value}=     Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-due mat-column-due ng-star-inserted']
        ${fees_in_disb_self_value}=    Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-fees mat-column-fees ng-star-inserted']
        ${emi_in_disb_self_value}=     Custom Modify Number    ${emi_in_disb_self_value}
        ${fees_in_disb_self_value}=    Custom Modify Number    ${fees_in_disb_self_value}
        Should Be Equal As Numbers    ${emi_in_disb_self}     ${emi_in_disb_self_value}
        Should Be Equal As Numbers    ${fees_in_disb_self}    ${fees_in_disb_self_value}

        Log To Console    \n Self Disbursement Line - Due-${emi_in_disb_self}, Principal O/S-${pos_self}, Fees-${fees_in_disb_self}, Paid- ${paid_in_disb_self}, Outstanding - ${ots_in_disb_self}
    
        Write To Cell    AA21   ${fees_in_disb_self}
        Write To Cell    Z21    ${paid_in_disb_self}
        Write To Cell    AC21   ${ots_in_disb_self}

    ${int_list_self}=    Create List    0
    ${p_list_self}=      Create List    0

    Click Element    xpath://div[contains(text(),'Self')]

    FOR  ${i}  IN RANGE    1    ${${tenure}+${1}}
        # S.No
        Write To Cell    S${cc}    ${i}          #s.no

        #Days
        Write To Cell    T${cc}    ${days_list}[${i}]        #days

        #Date
        ${date}=    Convert Date    ${date}    result_format=%Y-%m-%d
        Write To Cell    U${cc}    ${date_list}[${i}]        #Date

        #DPD Calculation
        IF  ${dpd_list}[${i}]>0
            IF  ${result_disbursed}==True
                Sleep    0.3
                ${dpd_self_value}=    Get Text    xpath:(//td[@class='mat-cell cdk-cell cdk-column-dpd mat-column-dpd ng-star-inserted'])[${${i}+${1}}]
                ${dpd_self_value}=    Custom Modify Number    ${dpd_self_value}
                Should Be Equal As Numbers    ${dpd_self_value}    ${dpd_list}[${i}]
                Write To Cell    AB${cc}    ${dpd_list}[${i}]
            END
        END

        #self_RS-Interest_calculation
        ${int_self_mon}=    Evaluate    ${int_list_cl}[${i}]*(${principal_self_split%}/100)*(${int_self_split%}/100)
        ${int_self_mon}    Evaluate    math.ceil(${int_self_mon})
        Append To List    ${int_list_self}    ${int_self_mon}
        Write To Cell    Y${cc}    ${int_self_mon}

        #Self RS Principal Calculation
        ${p_self_mon}=    Evaluate    ${p_list_cl}[${i}]*${principal_self_split%}/100
        ${p_self_mon}=    Evaluate    math.ceil(${p_self_mon})
        Append To List    ${p_list_self}    ${p_self_mon}
        Write To Cell    X${cc}    ${p_self_mon}

        #Self RS EMI Calculation
        ${emi_self_month}=    Evaluate    ${p_self_mon}+${int_self_mon}
        Write To Cell    W${cc}    ${emi_self_month}

        #Self RS Principal O/S Calculation
        ${pos_self}=    Evaluate    ${pos_self}-${p_self_mon}
        Write To Cell    V${cc}    ${pos_self}

        # Self RS Outstanding Calculation
        ${ots_self_mon}=    Set Variable    ${emi_self_month}
        Write To Cell    AC${cc}    ${ots_self_mon}

        #Self RS Sum Calculation
        ${int_self_sum}=    Evaluate    ${int_self_sum}+${int_self_mon}
        ${p_self_sum}=      Evaluate    ${p_self_sum}+${p_self_mon}
        ${emi_self_sum}=    Evaluate    ${emi_self_sum}+${emi_self_month}
        ${ots_self_sum}=    Evaluate    ${ots_self_sum}+${ots_self_mon}

        #RS_self_validation
        ${s.no_self_value}=          Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-number mat-column-number ng-star-inserted'])[${${i}+${1}}]
        ${days_self_value}=          Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-days mat-column-days ng-star-inserted'])[${${i}+${1}}]
        ${int_self_mon_value}=       Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-interest mat-column-interest ng-star-inserted'])[${${i}+${1}}]
        ${p_self_mon_value}=         Get Text    xpath:(//*[@class='mat-cell cdk-cell check cdk-column-principalDue mat-column-principalDue ng-star-inserted'])[${${i}+${1}}]
        ${emi_self_month_value}=     Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-due mat-column-due ng-star-inserted'])[${${i}+${1}}]
        ${pos_self_value}=           Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-balanceOfLoan mat-column-balanceOfLoan ng-star-inserted'])[${${i}+${1}}]
        ${ots_self_mon_value}=       Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-outstanding mat-column-outstanding ng-star-inserted'])[${${i}+${1}}]    
        # ${dpd_value}=         Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-dpd mat-column-dpd ng-star-inserted'])[${${i}+${1}}]    

        ${int_self_mon_value}=        Custom Modify Number    ${int_self_mon_value}
        ${p_self_mon_value}=          Custom Modify Number    ${p_self_mon_value}
        ${emi_self_month_value}=      Custom Modify Number    ${emi_self_month_value}
        ${pos_self_value}=            Custom Modify Number    ${pos_self_value}
        ${ots_self_mon_value}=        Custom Modify Number    ${ots_self_mon_value}

        Should Be Equal As Numbers    ${s.no_self_value}            ${i}
        Should Be Equal As Numbers    ${days_self_value}            ${days_list}[${i}]
        Should Be Equal As Numbers    ${int_self_mon_value}         ${int_self_mon}
        Should Be Equal As Numbers    ${p_self_mon_value}           ${p_self_mon}
        Should Be Equal As Numbers    ${emi_self_month_value}       ${emi_self_month}
        Should Be Equal As Numbers    ${pos_self_value}             ${pos_self}
        Should Be Equal As Numbers    ${ots_self_mon_value}         ${ots_self_mon}

        Log To Console    \n ${client_name} - Self- S.No-${i} Days-${days_list}[${i}], Due-${emi_self_month}, PrincipalDue-${p_self_mon}, Interest-${int_self_mon}, Principal O/S-${pos_self}, Outstanding-${ots_self_mon}

        #next line
        ${cc}=    Evaluate    ${cc}+1            #s.no for next line
    END

    ${emi_self_sum+c}=    Evaluate    ${emi_self_sum}+${charge_self_RS}
    ${ots_self_sum+c}=    Evaluate    ${ots_self_sum}+${ots_in_disb_self}
    ${total_self_paid}=    Set Variable    ${paid_in_disb_self}
    ${total_self_fees}=    Set Variable    ${fees_in_disb_self}
    Log    \nTotal Self Interest:${int_self_sum}
    Log    \nTotal Self Principal:${p_self_sum}
    Log    \nTotal Self EMI_withoutCharge:${emi_self_sum+c}

    #total lines in RS_excel
    Write To Cell    Y${${tc}+${n}}    ${int_self_sum}
    Write To Cell    X${${tc}+${n}}    ${p_self_sum}   
    Write To Cell    W${${tc}+${n}}    ${emi_self_sum+c}
    Write To Cell    Z${${tc}+${n}}    ${total_self_paid}
    Write To Cell    AA${${tc}+${n}}    ${total_self_fees}
    Write To Cell    AC${${tc}+${n}}    ${ots_self_sum+c}

    IF  ${result_disbursed}==True
        ${total_self_paid_value}=    Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-paid mat-column-paid ng-star-inserted']
        ${total_self_paid_value}=    Custom Modify Number       ${total_self_paid_value}
        Should Be Equal As Numbers    ${total_self_paid_value}    ${total_self_paid}
    END

    # total line in RS --> Self
    ${total_self_emi_value}=     Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-due mat-column-due ng-star-inserted']
    ${total_self_p_value}=       Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell check cdk-column-principalDue mat-column-principalDue ng-star-inserted']
    ${total_self_int_value}=     Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-interest mat-column-interest ng-star-inserted']
    ${total_self_fees_value}=    Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-fees mat-column-fees ng-star-inserted']
    ${total_self_ots_value}=     Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-outstanding mat-column-outstanding ng-star-inserted']
    ${total_self_emi_value}=     Custom Modify Number       ${total_self_emi_value}
    ${total_self_p_value}=       Custom Modify Number       ${total_self_p_value}
    ${total_self_int_value}=     Custom Modify Number       ${total_self_int_value}
    ${total_self_fees_value}=    Custom Modify Number       ${total_self_fees_value}
    ${total_self_ots_value}=     Custom Modify Number       ${total_self_ots_value}

    Should Be Equal As Numbers    ${total_self_emi_value}     ${emi_self_sum+c}
    Should Be Equal As Numbers    ${total_self_p_value}       ${p_self_sum}
    Should Be Equal As Numbers    ${total_self_int_value}     ${int_self_sum}
    Should Be Equal As Numbers    ${total_self_fees_value}    ${total_self_fees}
    Should Be Equal As Numbers    ${total_self_ots_value}     ${ots_self_sum+c}
    
    Log To Console    \n Self Total: Due-${emi_self_sum+c}, PrincipalDue-${p_self_sum}, Interest-${int_self_sum}, Fees-${total_self_fees}, Paid-${total_self_paid}, Outstanding-${ots_self_sum+c}

    Log To Console    \n RS_Self validated

    Set Test Variable    ${int_list_self}
    Set Test Variable    ${p_list_self}
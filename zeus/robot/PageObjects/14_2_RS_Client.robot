
*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/14_1_RS_Charge.robot

*** Keywords ***
RS_Client
    ${date}=    Convert Date    ${date}    result_format=%Y-%m-%d
    ${pv_date}=    Convert Date    ${Disbursement date}    result_format=%Y-%m-%d

    Write To Cell    D${pc}    ${Disbursement date}        #disb-date

	#emi_calculation
    ${int_12}=    Evaluate    (${int}/12)/100
    ${emi}=       Evaluate    ${p}*((${int_12}*((1+${int_12})**${n}))/(((1+${int_12})**${n})-1))
    ${emi}=       Evaluate    math.ceil(${emi})
    Write To Cell       B6    ${emi}
	
	${overdue_count}=      Evaluate    0
    ${p_overdue}=          Evaluate    0
    ${int_overdue}=        Evaluate    0

    ${int_sum}=            Evaluate    0
    ${p_sum}=              Evaluate    0
    ${emi_sum}=            Evaluate    0
    ${ots_sum}=            Evaluate    0

    Click Element          xpath://a[contains(text(),'Repayment Schedule')]
    Click Element          xpath://div[contains(text(),'Client')]
    #disbursement line in RS_Client
    ${pos_in_disb}=        Set Variable    ${pos}
    ${emi_in_disb}=        Set Variable    ${charge_RS}
    ${fees_in_disb}=       Set Variable    ${charge_RS}

    IF  ${result_disbursed}==True
        ${paid_in_disb}=    Set Variable    ${charge_RS}
        ${paid_in_disb_value}=    Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-paid mat-column-paid ng-star-inserted']
        ${paid_in_disb_value}=    Custom Modify Number    ${paid_in_disb_value}
        Should Be Equal As Numbers    ${paid_in_disb}    ${paid_in_disb_value}
        ${ots_in_disb}=    Set Variable    0
    ELSE IF    ${result_not_disbursed}==True
        ${paid_in_disb}=    Set Variable    0
        ${ots_in_disb}=    Set Variable    ${charge_RS}
        ${ots_in_disb_value}=    Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-outstanding mat-column-outstanding ng-star-inserted']
        ${ots_in_disb_value}=    Custom Modify Number    ${ots_in_disb_value}
        Should Be Equal As Numbers    ${ots_in_disb}    ${ots_in_disb_value}
    END
    ${emi_in_disb_value}=     Get Text         xpath://*[@class='mat-cell cdk-cell cdk-column-due mat-column-due ng-star-inserted']
    ${fees_in_disb_value}=    Get Text         xpath://*[@class='mat-cell cdk-cell cdk-column-fees mat-column-fees ng-star-inserted']
    ${emi_in_disb_value}=     Custom Modify Number    ${emi_in_disb_value}
    ${fees_in_disb_value}=    Custom Modify Number    ${fees_in_disb_value}
    Should Be Equal As Numbers    ${emi_in_disb}     ${emi_in_disb_value}
    Should Be Equal As Numbers    ${fees_in_disb}    ${fees_in_disb_value}
    ${pos_in_disb_value}=         Get Text    xpath://*[@class='mat-cell cdk-cell cdk-column-balanceOfLoan mat-column-balanceOfLoan ng-star-inserted']
    ${pos_in_disb_value}=         Custom Modify Number    ${pos_in_disb_value}
    Should Be Equal As Numbers    ${pos_in_disb}    ${pos_in_disb_value}

    Write To Cell    I21    ${fees_in_disb}
    Write To Cell    K21    ${paid_in_disb}
    Write To Cell    L21    ${ots_in_disb}

    ${date}=    Convert Date    ${date}    result_format=%Y-%m-%d
    ${date_list}=    Create List    ${pv_date}
    ${amount_disb}=    Evaluate    round(${charge_1}*100/118,2)-${p}
    ${amount_list}=    Create List    ${amount_disb}

    ${dpd_list}=       Create List    0
    ${int_list_cl}=    Create List    0
    ${p_list_cl}=      Create List    0
    ${days_list}=      Create List    0

    Log To Console    \n Client Disbursement Line --> Due-${emi_in_disb}, Principal O/S-${pos_in_disb}, Fees- ${fees_in_disb}, Paid-${paid_in_disb}, Outstanding-${ots_in_disb}
    FOR  ${i}  IN RANGE    1    ${${tenure}+${1}}
        # S.No
        Write To Cell    B${cc}    ${i}          #s.no

        #Days
        Log    ${date}
        Log    ${pv_date}
        ${Days}=    Subtract Date From Date    ${date}    ${pv_date}
        ${Days}=    Evaluate    ${Days}/86400
        ${Days}=    Convert To Number    ${Days}    ${roundup}
        Append To List    ${days_list}    ${Days}
        Write To Cell    C${cc}    ${days_list}[${i}]        #days

        #Date
        ${date}=    Convert Date    ${date}    result_format=%Y-%m-%d
        Write To Cell    D${cc}    ${date}        #Date
        Append To List    ${date_list}    ${date}

        ${pv_date}=    Set Variable    ${date}            #store the date
        Log    \n RES: date-item: ${date}                #[string]
        Log    \n RES: pv-date-item: ${pv_date}          #[string]

        ${modified_date}=    Convert Date    ${date}    %d %B %Y
        Log    \n RES: ${modified_date}
        ${date_value}=        Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-date mat-column-date ng-star-inserted'])[${${i}+${1}}]
        Should Be Equal    ${modified_date}    ${date_value}

        #xirr
        

        # ${modified_date}=    Custom Date Conversion    ${date}
        Log    \n RES: Modified-date: ${modified_date}    

        #interest_convention_365_or_366
        ${date}=    Convert Date    ${date}    datetime
        ${year}=    Set Variable    ${date.year}
        IF  ${year}%4 == 0
            ${years_day}=    Set Variable    366
        ELSE
            ${years_day}=    Set Variable    365
        END

        #int_of the month_calculation
        ${int_mon}=    Evaluate    ${pos}*(${int}/100)*(${days_list}[${i}]/${years_day})    #interest of the month
        ${int_mon}    Evaluate    math.ceil(${int_mon})
        Append To List    ${int_list_cl}    ${int_mon}
        Write To Cell    H${cc}    ${int_mon}

        #emi of the month
        IF  ${i}<${n}
            ${emi_month}=    Set Variable    ${emi}
            Write To Cell    F${cc}    ${emi}        #emi_for all line
        END

        #principal of the month calculation
        IF  ${i}==${n}
            ${p_mon}=    Set Variable    ${pos}
        ELSE
            ${p_mon}=    Evaluate    ${emi}-${int_mon}
        END
        Append To List    ${p_list_cl}    ${p_mon}
        Write To Cell    G${cc}    ${p_mon}     

        IF  ${i}==${n}                                            #emi for last month
            ${emi_month}=    Evaluate    ${p_mon}+${int_mon}    
            Write To Cell    F${cc}    ${emi}
        END
        
        #emi for last month
        IF  ${i}==${n}
            ${emi_month}=    Evaluate    ${p_mon}+${int_mon}
            Write To Cell    F${cc}    ${emi_month}        #emi_for last month
        END

        Append To List    ${amount_list}    ${emi_month}

        #principal o/s calculation
        ${pos}=    Evaluate    ${pos}-${p_mon}
        Write To Cell    E${cc}    ${pos}

        # Outstanding calculation
        ${ots_mon}=    Set Variable    ${emi_month}
        Write To Cell    L${cc}    ${ots_mon}

        #calculation for sum
        ${int_sum}=    Evaluate    ${int_sum}+${int_mon}
        ${p_sum}=    Evaluate    ${p_sum}+${p_mon}
        ${emi_sum}=    Evaluate    ${emi_sum}+${emi_month}
        ${ots_sum}=    Evaluate    ${ots_sum}+${ots_mon}

        #DPD Calculation
        ${current_date}=    Get Current Date
        ${dpd}=    Subtract Date From Date    ${current_date}    ${date}    
        ${dpd}=    Evaluate    ${dpd}//86400
        Append To List    ${dpd_list}    ${dpd}

        IF  ${dpd_list}[${i}]>0
            IF  ${result_disbursed}==True
                ${dpd_value}=    Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-dpd mat-column-dpd ng-star-inserted'])[${${i}+${1}}]
                ${dpd_value}=    Custom Modify Number    ${dpd_value}
                Should Be Equal As Numbers    ${dpd_value}    ${dpd_list}[${i}]   
                Write To Cell    J${cc}    ${dpd_list}[${i}]
                ${overdue_count}=    Evaluate    ${overdue_count}+${i}
                ${p_overdue}=        Evaluate    ${p_overdue}+${p_mon}
                ${int_overdue}=      Evaluate    ${int_overdue}+${int_mon}
            END
        END

        #RS_validation
        ${s.no_value}=        Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-number mat-column-number ng-star-inserted'])[${${i}+${1}}]
        ${days_value}=        Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-days mat-column-days ng-star-inserted'])[${${i}+${1}}]
        ${int_mon_value}=     Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-interest mat-column-interest ng-star-inserted'])[${${i}+${1}}]
        ${p_mon_value}=       Get Text    xpath:(//*[@class='mat-cell cdk-cell check cdk-column-principalDue mat-column-principalDue ng-star-inserted'])[${${i}+${1}}]
        ${emi_month_value}=   Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-due mat-column-due ng-star-inserted'])[${${i}+${1}}]
        ${pos_value}=         Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-balanceOfLoan mat-column-balanceOfLoan ng-star-inserted'])[${${i}+${1}}]
        ${ots_mon_value}=     Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-outstanding mat-column-outstanding ng-star-inserted'])[${${i}+${1}}]    
        # ${dpd_value}=         Get Text    xpath:(//*[@class='mat-cell cdk-cell cdk-column-dpd mat-column-dpd ng-star-inserted'])[${${i}+${1}}]    

        ${int_mon_value}=        Custom Modify Number    ${int_mon_value}
        ${p_mon_value}=          Custom Modify Number    ${p_mon_value}
        ${emi_month_value}=      Custom Modify Number    ${emi_month_value}
        ${pos_value}=            Custom Modify Number    ${pos_value}
        ${ots_mon_value}=        Custom Modify Number    ${ots_mon_value}
      
        Should Be Equal As Numbers    ${s.no_value}         ${i}
        Should Be Equal As Numbers    ${days_value}         ${days_list}[${i}]
        Should Be Equal As Numbers    ${int_mon_value}      ${int_mon}
        Should Be Equal As Numbers    ${p_mon_value}        ${p_mon}
        Should Be Equal As Numbers    ${emi_month_value}    ${emi_month}
        Should Be Equal As Numbers    ${pos_value}          ${pos}
        Should Be Equal As Numbers    ${ots_mon_value}      ${ots_mon}
        
        Log To Console    \n ${client_name} - Client - S.No-${i}, Days-${days_list}[${i}], Due-${emi_month}, PrincipalDue-${p_mon}, Interest-${int_mon}, Principal O/S-${pos}, Outstanding-${ots_mon}

        #nextDate--Line
        ${date}=    Convert Date    ${date}    datetime
        ${month}=    Evaluate    ${date.month}+1
        IF  ${month}==13
            ${month}=    Set Variable    1
            ${year}=    Evaluate    ${date.year}+1
        ELSE
            ${year}=    Set Variable    ${date.year}
        END
        IF  ${month}<10
            ${month}=    Set Variable    0${month}
        END
        ${date}=    Convert Date    ${year}-${month}-${date.day}    date_format=%Y-%m-%d
        ${date}=    Convert Date    ${date}    result_format=%Y-%m-%d
        
        #next line
        ${cc}=    Evaluate    ${cc}+1            #s.no for next line
    END

    ${emi_sum+c}=    Evaluate    ${emi_sum}+${emi_in_disb}
    ${ots_sum+c}=    Evaluate    ${ots_sum}+${ots_in_disb}
    ${total_client_fess}=    Set Variable    ${fees_in_disb}
    ${total_client_paid}=    Set Variable    ${paid_in_disb}

    #total lines in RS_excel
    Write To Cell    H${${tc}+${n}}    ${int_sum}
    Write To Cell    G${${tc}+${n}}    ${p_sum}   
    Write To Cell    F${${tc}+${n}}    ${emi_sum+c}
    Write To Cell    L${${tc}+${n}}    ${ots_sum+c}
    Write To Cell    I${${tc}+${n}}    ${total_client_fess}
    Write To Cell    K${${tc}+${n}}    ${total_client_paid}

    # total line in RS --> Client
    ${total_client_emi_value}=     Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-due mat-column-due ng-star-inserted']
    ${total_client_p_value}=       Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell check cdk-column-principalDue mat-column-principalDue ng-star-inserted']
    ${total_client_int_value}=     Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-interest mat-column-interest ng-star-inserted']
    ${total_client_fees_value}=    Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-fees mat-column-fees ng-star-inserted']
    ${total_client_paid_value}=    Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-paid mat-column-paid ng-star-inserted']
    ${total_client_ots_value}=     Get Text    xpath://td[@class='mat-footer-cell cdk-footer-cell cdk-column-outstanding mat-column-outstanding ng-star-inserted']
    
    ${total_client_emi_value}=    Custom Modify Number    ${total_client_emi_value}
    ${total_client_p_value}=      Custom Modify Number    ${total_client_p_value}
    ${total_client_int_value}=    Custom Modify Number    ${total_client_int_value}
    ${total_client_fees_value}=   Custom Modify Number    ${total_client_fees_value}
    ${total_client_paid_value}=   Custom Modify Number    ${total_client_paid_value}
    ${total_client_ots_value}=    Custom Modify Number    ${total_client_ots_value}

    Should Be Equal As Numbers    ${total_client_emi_value}     ${emi_sum+c}
    Should Be Equal As Numbers    ${total_client_p_value}       ${p_sum}
    Should Be Equal As Numbers    ${total_client_int_value}     ${int_sum}
    Should Be Equal As Numbers    ${total_client_fees_value}    ${total_client_fess}
    Should Be Equal As Numbers    ${total_client_ots_value}     ${ots_sum+c}
    Should Be Equal As Numbers    ${total_client_paid_value}    ${total_client_paid}
    
    Log To Console    \n Client Total: Due-${emi_sum+c}, PrincipalDue-${p_sum}, Interest-${int_sum}, Fees-${total_client_fess}, Paid-${total_client_paid}, Outstanding-${ots_sum+c}

    Log    \nRES: Overdue Count: ${overdue_count}
    Log    \nRES: Principal Overdue: ${p_overdue}
    Log    \nRES: Interest Overdue: ${int_overdue}
    Log To Console    \n RS_Client Validated

    Set Test Variable    ${int_sum}
    Set Test Variable    ${p_sum}
    Set Test Variable    ${ots_sum}
    Set Test Variable    ${p_overdue}
    Set Test Variable    ${int_overdue}
  
    Set Test Variable    ${days_list}
    Set Test Variable    ${date_list}
    Set Test Variable    ${int_list_cl}
    Set Test Variable    ${p_list_cl}
    Set Test Variable    ${dpd_list}

    #xirr
    ${xirr}=    Custom Xirr    ${date_list}    ${amount_list}
    ${xirr}=    Evaluate    round(${xirr}*100,2)
    Set Test Variable    ${xirr}
    Should Be Equal As Numbers     ${XIRR_value}               ${xirr}

TC_excel_namings
    Write To Cell    A2    Principal
    Write To Cell    A3    Interest
    Write To Cell    A4    Tenure
    Write To Cell    A5    Roundoff
    Write To Cell    A6    EMI
    Write To Cell    A14    Current Date
    ${currentDate}=    Get Current Date
    ${currentDate}=    Convert Date    ${currentDate}    result_format=%d-%m-%Y
    Write To Cell    B14    ${currentDate}
    Write To Cell    B19    S.No
    Write To Cell    C19    Days
    Write To Cell    D19    Date
    Write To Cell    E19    Principal o/s
    Write To Cell    F19    EMI
    Write To Cell    G19    Principal
    Write To Cell    H19    Interest
    Write To Cell    I19    Fees
    Write To Cell    J19    DPD
    Write To Cell    K19    Paid
    Write To Cell    L19    Outstanding
    Write To Cell    M19    Cashflow
    #self
    Write To Cell    S19    S.No
    Write To Cell    T19    Days
    Write To Cell    U19    Date
    Write To Cell    V19    Principal o/s
    Write To Cell    W19    EMI
    Write To Cell    X19    Principal
    Write To Cell    Y19    Interest
    Write To Cell    AA19    Fees
    Write To Cell    AB19    DPD
    Write To Cell    Z19     Paid
    Write To Cell    AC19    Outstanding
    Write To Cell    AD19    Cashflow
    # partner
    Write To Cell    AJ19    S.No
    Write To Cell    AK19    Days
    Write To Cell    AL19    Date
    Write To Cell    AM19    Principal o/s
    Write To Cell    AN19    EMI
    Write To Cell    AO19    Principal
    Write To Cell    AP19    Interest
    Write To Cell    AR19    Fees
    Write To Cell    AS19    DPD
    Write To Cell    AQ19    Paid
    Write To Cell    AT19    Outstanding
    Write To Cell    AU19    Cashflow  

    Write To Cell    B5    ${roundup}
    Save

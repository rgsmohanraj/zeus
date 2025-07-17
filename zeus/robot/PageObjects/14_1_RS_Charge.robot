*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Library            ../PageObjects/CustomLibrary.py

*** Variables ***
@{state_vcpl_list}     Tamil Nadu    Maharashtra

*** Keywords ***
TC_Charge_Details
    TC_Excel_Charge_Namings
    ${total_charge_sum}                    Evaluate    0
    ${total_self_fee_sum_basic}            Evaluate    0
    ${total_partner_fee_sum_basic}         Evaluate    0
    ${total_fee_amount_sum_basic}          Evaluate    0
    ${total_gst_sum}                       Evaluate    0
    ${result_state_gst_type}=    Run Keyword And Return Status    List Should Contain Value    ${state_vcpl_list}    ${state}
    FOR  ${i}  IN RANGE    1    ${charge_count}+${1}
        ${total_fee}=     Set Variable    ${Charge_${i}}
        # ${fee_amount}=    Evaluate    (math.ceil((${total_fee}*100/118)*100))/100    # roundup 2 decimal
        ${fee_amount}=    Evaluate    round(${total_fee}*100/118,2)                    # round 2 decimal
        ${gst}=           Evaluate    ${total_fee}-${fee_amount}
        ${gst}=           Evaluate    round(${gst},2)
        IF  ${result_state_gst_type}==False
            ${igst}=        Set Variable    ${gst}
            ${cgst}=        Evaluate    0
            ${sgst}=        Evaluate    0
        ELSE
            ${igst}=        Evaluate    0
            ${cgst}=        Evaluate    ${gst}/2
            ${cgst}=        Convert To Number    ${cgst}    3
            ${cgst}=        Convert To String    ${cgst}
            ${result_roundoff}=    Run Keyword And Return Status    Should End With    ${cgst}    5
            ${cgst}=        Convert To Number    ${cgst}
            IF  ${result_roundoff}==True
                ${cgst}=    Evaluate    math.ceil(${cgst}*100)/100
            ELSE
                ${cgst}=    Convert To Number    ${cgst}    2    
            END
            ${sgst}=        Evaluate    ${gst}-${cgst}
            ${sgst}=        Convert To Number    ${sgst}    2
        END
        ${self_fee}=        Evaluate    ${fee_amount}*${charge_${i}_split%}/100
        ${self_fee}=        Convert To Number    ${self_fee}    2
        ${partner_fee}=     Evaluate    ${fee_amount}*(100-${charge_${i}_split%})/100
        ${partner_fee}=     Convert To Number    ${partner_fee}    2

        Log    \nRES:i_value:${i}
        Log    \nRES:Fee_Amount:${fee_amount}
        Log    \nRES:IGST:${igst}
        Log    \nRES:CGST:${cgst}
        Log    \nRES:SGST:${sgst}
        Log    \nRES:Total_Charge_${i}:${total_fee}
        Log    \nRES:Self_fee_amount:${self_fee}
        Log    \nRES:Partner_fee_amount:${partner_fee}

        ${line}=         Evaluate    ${i}+3
        Write To Cell    I${line}    ${self_fee}
        Write To Cell    J${line}    ${partner_fee}
        Write To Cell    K${line}    ${fee_amount}
        Write To Cell    L${line}    ${igst}
        Write To Cell    M${line}    ${cgst}
        Write To Cell    N${line}    ${sgst}
        Write To Cell    O${line}    ${total_fee}
        
        ${total_charge_sum}=                Evaluate    ${total_fee}+${total_charge_sum}
        ${total_self_fee_sum_basic}=        Evaluate    ${total_self_fee_sum_basic}+${self_fee}
        ${total_partner_fee_sum_basic}=     Evaluate    ${total_partner_fee_sum_basic}+${partner_fee}
        ${total_fee_amount_sum_basic}=      Evaluate    ${total_fee_amount_sum_basic}+${fee_amount}
        ${total_gst_sum}=                   Evaluate    ${total_gst_sum}+${gst}

        # RS_xpath
        Click Element    xpath://a[contains(text(),'Disbursement Summary')]
        IF  ${charge_${i}}!=0
            # ${self_fee_value}=       Get Text    xpath://td[contains(text(),'${charge_${i}_name}')]/following::td[1]
            # ${partner_fee_value}=    Get Text    xpath://td[contains(text(),'${charge_${i}_name}')]/following::td[2]
            ${fee_amount_value}=     Get Text    xpath://td[contains(text(),'${charge_${i}_name}')]/following::td[1]
            ${igst_value}=           Get Text    xpath://td[contains(text(),'${charge_${i}_name}')]/following::td[2]
            ${cgst_value}=           Get Text    xpath://td[contains(text(),'${charge_${i}_name}')]/following::td[3]
            ${sgst_value}=           Get Text    xpath://td[contains(text(),'${charge_${i}_name}')]/following::td[4]
            ${total_value}=          Get Text    xpath://td[contains(text(),'${charge_${i}_name}')]/following::td[5]

            Log    \nRES: Charge Name with Details - ${charge_${i}_name}
            # Log    \nRES: Self fee value: ${self_fee_value}
            # Log    \nRES: Partner fee value: ${partner_fee_value}
            Log    \nRES: Fee Amount Value: ${fee_amount_value}
            Log    \nRES: IGST value: ${igst_value}
            Log    \nRES: CGST value: ${cgst_value}
            Log    \nRESL SGST Value: ${sgst_value}
            Log    \nRES: Total Value: ${total_value}

            # Should Be Equal As Numbers    ${self_fee}        ${self_fee_value}
            # Should Be Equal As Numbers    ${partner_fee}     ${partner_fee_value}
            Should Be Equal As Numbers    ${fee_amount}      ${fee_amount_value}
            Should Be Equal As Numbers    ${igst}            ${igst_value}
            Should Be Equal As Numbers    ${cgst}            ${cgst_value}
            Should Be Equal As Numbers    ${sgst}            ${sgst_value}
            Should Be Equal As Numbers    ${total_fee}       ${total_value}
        END
        Log To Console    \n Charge_${i} validated
    END

    Write To Cell    I6    ${total_self_fee_sum_basic}
    Write To Cell    J6    ${total_partner_fee_sum_basic}
    Write To Cell    K6    ${total_fee_amount_sum_basic}
    Write To Cell    O6    ${total_charge_sum}
    
    ${total_charge_without_gst}=    Evaluate    ${total_charge_sum}-${total_gst_sum}

    ${grand_total_value}=    Get Text    xpath://b[text()='Grand Total']/following::td[5]
    Log    \nRES Grand Total Value: ${grand_total_value}
    Should Be Equal As Numbers    ${total_charge_sum}    ${grand_total_value}
    Log    Grand total fee validated

    #net_disbursement
    ${net_disb}=    Evaluate    ${principal}-${total_charge_sum}
    Log To Console    \nNet_Disbursement_Amount:${net_disb}
    Write To Cell    F13    ${net_disb}

    #net_self_disbursement
    ${principal_self_amount}=    Evaluate    ${principal}*${principal_self_split%}/100
    ${net_self_disb}=    Evaluate    ${principal_self_amount}-(${total_self_fee_sum_basic}+(${gst_split%}*${total_gst_sum}/100))
    Log To Console    \nNet Self Disbursement Amount: ${net_self_disb}
    Write To Cell    F14    ${net_self_disb}

    #net partner_disbursement
    IF  ${principal_self_split%}==100
        ${net_partner_disb}=    Set Variable    0
        Log To Console    \nNet Partner Disbursement Amount: ${net_partner_disb}
        Write To Cell    F15    ${net_partner_disb}
    END

    # RS_validation for Net values
    IF  ${result_disbursed}==True
        ${net_disbursement_value}=    Get Text    xpath://b[contains(text(),'Net Disbursement Amount')]/following::span
        ${self_net_value}=            Get Text    xpath://b[contains(text(),'Self Net Disbursement Amount')]/following::span
        ${partner_net_value}=         Get Text    xpath://b[contains(text(),'Partner Net Disbursement Amount')]/following::span

        ${net_disbursement_value}=    Custom Modify Number    ${net_disbursement_value}
        ${self_net_value}=            Custom Modify Number    ${self_net_value}
        ${partner_net_value}=         Custom Modify Number    ${partner_net_value}

        Should Be Equal As Numbers    ${net_disbursement_value}    ${net_disb}
        Should Be Equal As Numbers    ${self_net_value}            ${net_self_disb}
        Should Be Equal As Numbers    ${partner_net_value}         ${net_partner_disb}

        Log    \nRES: Net Disbursement Value: ${net_disbursement_value}
        Log    \nRES: Self_Net_Disbursement_value: ${self_net_value}
        # Log    \nRES: Partner_Net_Disbursement_value: ${partner_fee_value}
    ELSE
        ${net_disbursal_value_not_disb}=            Custom Modify Number    ${net_disbursal_value_not_disb}
        ${net_self_disbursal_value_not_disb}=       Custom Modify Number    ${net_self_disbursal_value_not_disb}
        ${net_partner_disbursal_value_not_disb}=    Custom Modify Number    ${net_partner_disbursal_value_not_disb}
        Should Be Equal As Numbers    ${net_disbursal_value_not_disb}            ${net_disb}
        Should Be Equal As Numbers    ${net_self_disbursal_value_not_disb}       ${net_self_disb}
        Should Be Equal As Numbers    ${net_partner_disbursal_value_not_disb}    ${net_partner_disb}
    END

    Set Test Variable    ${net_disb}
    Set Test Variable    ${net_self_disb}
    Set Test Variable    ${net_partner_disb}


    Log    Net Disbursement for Client, Self, Partner validated

    ${total_charge_with_gst}=    Set Variable    ${total_charge_sum}
    ${charge_and_net_results}=    Create List    ${total_charge_with_gst}    ${total_charge_without_gst}    ${total_self_fee_sum_basic}    ${total_partner_fee_sum_basic}    ${net_disb}
    
    ${charge_RS}=            Set Variable    ${total_charge_without_gst}
    ${charge_self_RS}=       Set Variable    ${total_self_fee_sum_basic}
    ${charge_partner_RS}=    Set Variable    ${total_partner_fee_sum_basic}

    Set Test Variable    ${charge_RS}
    Set Test Variable    ${charge_self_RS}
    Set Test Variable    ${charge_partner_RS}
    Set Test Variable    ${total_charge_with_gst}
    Set Test Variable    ${total_charge_without_gst}

    Log To Console    \n Fees and Charge details & net disbursement validated
    # [Return]    ${charge_and_net_results}

TC_Excel_Charge_Namings
    Write To Cell    E1     Charge Details
    Write To Cell    E7     Loan amount
    Write To Cell    E8     Loan Balance
    Write To Cell    E9     Amount Paid
    Write To Cell    E10    Proposed Amount
    Write To Cell    E11    Approved Amount
    Write To Cell    E12    Disburse Amount
    Write To Cell    E13    Net Disbursement Amount
    Write To Cell    E14    Self Net Disbursement Amount
    Write To Cell    E15    Partner Net Disbrusement Amount
    
    Write To Cell    H2    Fee List
    Write To Cell    I3    Self Fee Amount
    Write To Cell    J3    Partner Fee Amount
    Write To Cell    K3    Fee Amount
    Write To Cell    L3    IGST
    Write To Cell    M3    CGST
    Write To Cell    N3    SGST
    Write To Cell    O3    Total
    Write To Cell    H4    Processing fee
    Write To Cell    H5    Documentation charge
    
    Write To Cell    I9    Original
    Write To Cell    J9    Paid
    Write To Cell    K9    Waived
    Write To Cell    L9    WrittenOff
    Write To Cell    M9    Outstanding
    Write To Cell    N9    Overdue
    Write To Cell    H10    Principal
    Write To Cell    H11    Interest
    Write To Cell    H12    Fees
    Write To Cell    H13    Penalties
    Write To Cell    H14    Broken period Interest
    Write To Cell    H15    Total
    Write To Cell    H6     Grand Total

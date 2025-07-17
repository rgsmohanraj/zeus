*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/14_1_RS_Charge.robot

*** Keywords ***
TC_General_Loan_details_validation
    IF  ${result_disbursed}==True
        Should Be Equal As Numbers    ${loan_amount_value}        ${principal}
        Should Be Equal As Numbers    ${loan_balance_value}       ${outstanding_total_value}
        Should Be Equal As Numbers    ${amount_paid_value}        ${total_paid_without_gst}
        Should Be Equal As Numbers    ${proposed_amount_value}    ${principal}
        Should Be Equal As Numbers    ${approved_amount_value}    ${principal}
        Should Be Equal As Numbers    ${disburse_amount_value}    ${net_disb}
        Write To Cell    F7    ${principal}
        Write To Cell    F8    ${outstanding_total_value}
        Write To Cell    F9    ${total_paid_without_gst}
        Write To Cell    F10   ${principal}
        Write To Cell    F11   ${principal}
        Write To Cell    F12   ${net_disb}
        
    ELSE
        ${proposed_amount_value_not_disb}=        Custom Modify Number    ${proposed_amount_value_not_disb}
        ${approved_amount_value_not_disb}=        Custom Modify Number    ${approved_amount_value_not_disb}
        ${net_disbursal_value_not_disb}=          Custom Modify Number    ${net_disbursal_value_not_disb}    
        ${net_self_disbursal_value_not_disb}=     Custom Modify Number    ${net_self_disbursal_value_not_disb}    
        ${net_partner_disbursal_value_not_disb}=  Custom Modify Number    ${net_partner_disbursal_value_not_disb}    
        Should Be Equal As Numbers    ${proposed_amount_value_not_disb}          ${principal}
        Should Be Equal As Numbers    ${approved_amount_value_not_disb}          ${principal}
        Should Be Equal As Numbers    ${net_disbursal_value_not_disb}            ${net_disb}
        Should Be Equal As Numbers    ${net_self_disbursal_value_not_disb}       ${net_self_disb}
        Should Be Equal As Numbers    ${net_partner_disbursal_value_not_disb}    ${net_partner_disb}
        Write To Cell    F10   ${principal}
        Write To Cell    F11   ${principal}  
    END
    
TC_Loan_summary
    IF  ${result_disbursed}==True

        ${total_original}=        Evaluate        ${p_sum}+${int_sum}+${total_charge_with_gst}
        ${total_paid_with_gst}=   Set Variable    ${total_charge_with_gst}
        ${total_paid_without_gst}=   Set Variable    ${total_charge_without_gst}
        ${total_outstanding}=     Evaluate        ${int_sum}+${p_sum}
        ${total_overdue}=         Evaluate        ${p_overdue}+${int_overdue}
        Should Be Equal As Numbers           ${ots_sum}      ${total_outstanding}
    
        # Excel output
        #original
        Write To Cell    I10    ${p_sum}               #original principal
        Write To Cell    I11    ${int_sum}             #original interest
        Write To Cell    I12    ${total_charge_with_gst}                      #original fees
        Write To Cell    I13    0                      #original penalties
        Write To Cell    I14    0                      #original broken period interest
        Write To Cell    I15    ${total_original}      #original total
        #paid
        Write To Cell    J10    ${0}                   #principal
        Write To Cell    J11    ${0}                   #interest
        Write To Cell    J12    ${total_charge_with_gst}   #fees
        Write To Cell    J13    ${0}                   #penalties
        Write To Cell    J14    ${0}                   #broken period interest
        Write To Cell    J15    ${total_charge_with_gst}          #total
        #outstanding
        Write To Cell    M10    ${p_sum}               #principal
        Write To Cell    M11    ${int_sum}             #interest
        Write To Cell    M12    ${0}                   #fees
        Write To Cell    M13    ${0}                   #penalties
        Write To Cell    M14    ${0}                   #broken period interest
        Write To Cell    M15    ${total_outstanding}   #total    
        #overdue
        Write To Cell    N10    ${p_overdue}           #principal
        Write To Cell    N11    ${int_overdue}         #interest
        Write To Cell    N12    ${0}                   #fees
        Write To Cell    N13    ${0}                   #penalties
        Write To Cell    N14    ${0}                   #broken period interest
        Write To Cell    N15    ${total_overdue}       #total

        Click Element    xpath://a[contains(text(),'Disbursement Summary')]

        # RS_Loan Summary
        ${original_principal_value}=        Get Text    xpath://td[contains(text(),'Principal')]/following::td[1]
        ${paid_principal_value}=            Get Text    xpath://td[contains(text(),'Principal')]/following::td[2]
        ${outstanding_principal_value}=     Get Text    xpath://td[contains(text(),'Principal')]/following::td[5]
        ${overdue_principal_value}=         Get Text    xpath://td[contains(text(),'Principal')]/following::td[6]

        ${original_interest_value}=         Get Text    xpath://td[contains(text(),'Interest')]/following::td[1]
        ${paid_interest_value}=             Get Text    xpath://td[contains(text(),'Interest')]/following::td[2]
        ${outstanding_interest_value}=      Get Text    xpath://td[contains(text(),'Interest')]/following::td[5]
        ${overdue_interest_value}=          Get Text    xpath://td[contains(text(),'Interest')]/following::td[6]

        ${original_fees_value}=             Get Text    xpath://td[contains(text(),'Fees')]/following::td[1]
        ${paid_fees_value}=                 Get Text    xpath://td[contains(text(),'Fees')]/following::td[2]
        ${outstanding_fees_value}=          Get Text    xpath://td[contains(text(),'Fees')]/following::td[5]
        ${overdue_fees_value}=              Get Text    xpath://td[contains(text(),'Fees')]/following::td[6]

        ${original_total_value}=            Get Text    xpath://td[contains(text(),'Total')]/following::td[1]
        ${paid_total_value}=                Get Text    xpath://td[contains(text(),'Total')]/following::td[2]
        ${outstanding_total_value}=         Get Text    xpath://td[contains(text(),'Total')]/following::td[5]
        ${overdue_total_value}=             Get Text    xpath://td[contains(text(),'Total')]/following::td[6]
    
        #original
        ${original_principal_value}=    Custom Modify Number    ${original_principal_value}
        ${original_interest_value}=     Custom Modify Number    ${original_interest_value}
        ${original_fees_value}=         Custom Modify Number    ${original_fees_value}
        ${original_total_value}=        Custom Modify Number    ${original_total_value}
    
        Should Be Equal As Numbers    ${original_principal_value}       ${p_sum}
        Should Be Equal As Numbers    ${original_interest_value}        ${int_sum}
        Should Be Equal As Numbers    ${original_fees_value}            ${total_charge_with_gst}
        Should Be Equal As Numbers    ${original_total_value}           ${total_original}

        #paid
        ${paid_principal_value}=    Custom Modify Number    ${paid_principal_value}
        ${paid_interest_value}=     Custom Modify Number    ${paid_interest_value}
        ${paid_fees_value}=         Custom Modify Number    ${paid_fees_value}
        ${paid_total_value}=        Custom Modify Number    ${paid_total_value}

        Should Be Equal As Numbers    ${paid_principal_value}       ${0}
        Should Be Equal As Numbers    ${paid_interest_value}        ${0}
        Should Be Equal As Numbers    ${paid_fees_value}            ${total_charge_with_gst}
        Should Be Equal As Numbers    ${paid_total_value}           ${total_paid_with_gst}

        # outstanding
        ${outstanding_principal_value}=    Custom Modify Number    ${outstanding_principal_value}
        ${outstanding_interest_value}=     Custom Modify Number    ${outstanding_interest_value}
        ${outstanding_fees_value}=         Custom Modify Number    ${outstanding_fees_value}
        ${outstanding_total_value}=        Custom Modify Number    ${outstanding_total_value}

        Should Be Equal As Numbers    ${outstanding_principal_value}       ${p_sum}
        Should Be Equal As Numbers    ${outstanding_interest_value}        ${int_sum}
        Should Be Equal As Numbers    ${outstanding_fees_value}            ${0}
        Should Be Equal As Numbers    ${outstanding_total_value}           ${total_outstanding}

        # overdue
        ${overdue_principal_value}=    Custom Modify Number    ${overdue_principal_value}
        ${overdue_interest_value}=     Custom Modify Number    ${overdue_interest_value}
        ${overdue_fees_value}=         Custom Modify Number    ${overdue_fees_value}
        ${overdue_total_value}=        Custom Modify Number    ${overdue_total_value}

        Should Be Equal As Numbers    ${overdue_principal_value}       ${p_overdue}
        Should Be Equal As Numbers    ${overdue_interest_value}        ${int_overdue}
        Should Be Equal As Numbers    ${overdue_fees_value}            ${0}
        Should Be Equal As Numbers    ${overdue_total_value}           ${total_overdue}

        Set Test Variable    ${original_total_value}
        Set Test Variable    ${paid_total_value}
        Set Test Variable    ${outstanding_total_value}
        Set Test Variable    ${overdue_total_value}
        Set Test Variable    ${total_paid_with_gst}
        Set Test Variable    ${total_paid_without_gst}

    END
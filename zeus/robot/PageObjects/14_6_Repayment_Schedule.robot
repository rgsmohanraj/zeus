*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/14_1_RS_Charge.robot
Resource           ../PageObjects/14_2_RS_Client.robot
Resource           ../PageObjects/14_2_RS_Self.robot
Resource           ../PageObjects/14_4_RS_Partner.robot
Resource           ../PageObjects/14_5_RS_Loan_Summary.robot

*** Variables ***
${loan_name}=                NOCPL TESTING
${principal_self_split%}     100
${int_self_split%}           100
${charge_1_split%}           100
${charge_2_split%}           45
${gst_split%}                100

${charge_count}              2
${roundup}                   0
${cc}                        22
${pc}                        21
${tc}                        22

*** Keywords ***
# *** Test Cases ***
TC_Repayement_Schedule_validations
    Click Element    xpath://a[@href='#/clients']
    Sleep    0.2
    Click Element    xpath://mat-select[@aria-label='Items per page:']
    Sleep    0.2
    Click Element    xpath://span[text()=' 100 ']
    Sleep    0.2
    ${result}=    Run Keyword And Return Status   Element Should Be Visible    xpath://tr/td[contains(text(),'${client_name}')]/following::td[contains(text(),'${mobile_no}')]
    ${i}=    Set Variable    1
    Sleep    1
    WHILE  ${result}==False
        Click Element    xpath://button[@aria-label='Next page']
        Sleep    1
        ${result}=    Run Keyword And Return Status   Element Should Be Visible    xpath://tr/td[contains(text(),'${client_name}')]/following::td[contains(text(),'${mobile_no}')]
        ${i}=    Evaluate    ${i}+1
        IF    ${i}==50    BREAK
    END
    Sleep    2
    Click Element    xpath://tr/td[contains(text(),'${client_name}')]/following::td[contains(text(),'${mobile_no}')]
    Create Workbook    ${CURDIR}\\${client_name}-${external_id}.xlsx
    Switch Workbook    ${CURDIR}\\${client_name}-${external_id}.xlsx
    Create Sheet    ${client_name}
    Switch Sheet    ${client_name}
    Sleep    2
    ${loan_amount_value}=        Get Text    xpath://*[contains(text(),'${loan_name}')]/following-sibling::td[1]
    ${loan_balance_value}=       Get Text    xpath://td[contains(text(),'${loan_name}')]/following-sibling::td[2]
    ${amount_paid_value}=        Get Text    xpath://td[contains(text(),'${loan_name}')]/following-sibling::td[3]
    Set Test Variable    ${loan_amount_value}
    Set Test Variable    ${loan_balance_value}
    Set Test Variable    ${amount_paid_value}

    Click Element                xpath://td[contains(text(),'${loan_name}')]
    Sleep    1
    ${result_not_disbursed}=    Run Keyword And Return Status    Page Should Contain Element    xpath://span[contains(text(),'Disburse ')]
    Sleep    1
    ${result_disbursed}=    Run Keyword And Return Status    Page Should Contain Element    xpath://span[contains(text(),'Undo Disbursal')]
    Set Test Variable    ${result_disbursed}
    Set Test Variable    ${result_not_disbursed}
    #external_id_validation
    Sleep    1
    ${external_id_value}=    Get Text    xpath:(//td[contains(text(),'External Id')]/following::td)[1]
    Should Be Equal    ${external_id}    ${external_id_value}

    IF  ${result_disbursed}==True
        Log To Console    \n Case Disbursed
        Wait Until Page Contains Element    xpath://td[contains(text(),'Proposed Amount')]
        ${proposed_amount_value}=    Get Text    xpath://td[contains(text(),'Proposed Amount')]/following::td
        ${approved_amount_value}=    Get Text    xpath://td[contains(text(),'Approved Amount')]/following::td
        ${disburse_amount_value}=    Get Text    xpath://td[contains(text(),'Disburse Amount')]/following::td  
        Set Test Variable    ${proposed_amount_value}
        Set Test Variable    ${approved_amount_value}
        Set Test Variable    ${disburse_amount_value}
        Write To Cell    E2    Disbursement Completed
    ELSE IF  ${result_not_disbursed}==True
        Log To Console    \n Case not disbursed
        Wait Until Element Is Visible    xpath://*[contains(text(),'Disbursement Summary')]
        Click Element    xpath://a[contains(text(),'Disbursement Summary')]
        Sleep    0.2
        # Loan Purpose for not disbursed cases
        ${proposed_amount_value_not_disb}=            Get Text    xpath://span[contains(text(),'Proposed Amount')]/following::span
        ${approved_amount_value_not_disb}=            Get Text    xpath://span[contains(text(),'Approved Amount')]/following::span
        ${net_disbursal_value_not_disb}=              Get Text    xpath://span[contains(text(),'Net Disbursal Amount')]/following::span[1]
        ${net_self_disbursal_value_not_disb}=         Get Text    xpath://span[contains(text(),'Net Self Disbursal Amount')]/following::span[1]
        ${net_partner_disbursal_value_not_disb}=      Get Text    xpath://span[contains(text(),'Net Partner Disbursal Amount')]/following::span[1]
        Set Test Variable    ${proposed_amount_value_not_disb}
        Set Test Variable    ${approved_amount_value_not_disb}
        Set Test Variable    ${net_disbursal_value_not_disb}
        Set Test Variable    ${net_self_disbursal_value_not_disb}
        Set Test Variable    ${net_partner_disbursal_value_not_disb}
    ELSE
        Log To Console    \n Could not find if the case is disbursed
        Write To Cell    E2    Could not find if the case is disbursed
    END

    Sleep    0.3 seconds
    Click Element    xpath://a[contains(text(),'Disbursement Summary')]
    Sleep    0.5 

    14_1_RS_Charge.TC_Charge_Details
    Click Element    xpath://*[contains(text(),'Account Details')]
    ${XIRR_value}=    Get Text    //span[contains(text(),'XIRR Value')]/following::span
    Set Test Variable    ${XIRR_value}
    Sleep    1
    Click Element    xpath://*[contains(text(),'Repayment Schedule')]

    14_2_RS_Client.TC_excel_namings
    14_1_RS_Charge.TC_Excel_Charge_Namings
    ${date}=    Convert Date    ${date}    result_format=%Y-%m-%d
    ${pv_date}=    Convert Date    ${Disbursement date}    result_format=%Y-%m-%d
    
    Sleep    0.2
    ${p}=            Set Variable    ${principal}
    ${p_self}=       Evaluate    ${p}*${principal_self_split%}/100
    ${p_partner}=    Evaluate    ${p}*(100-${principal_self_split%})/100

    Set Test Variable    ${p}
    Set Test Variable    ${p_self}
    Set Test Variable    ${p_partner}

    ${int}=          Set Variable    ${interest}
    ${n}=            Set Variable    ${tenure}

    ${pos}=          Set Variable    ${p}
    ${pos_self}=     Set Variable    ${p_self}
    ${pos_partner}=  Set Variable    ${p_partner}

    Set Test Variable    ${int}
    Set Test Variable    ${n}
    Set Test Variable    ${pos}
    # Set Test Variable    ${pos_self}
    Set Test Variable    ${pos_partner}
    
    Write To Cell    E21    ${pos}
    Write To Cell    V21    ${pos_self}
    Write To Cell    AM21   ${pos_partner}
    Write To Cell    F21    ${charge_RS}
    Write To Cell    B2     ${p}
    Write To Cell    B3     ${int}
    Write To Cell    B4     ${n}

    Sleep    1

    14_2_RS_Client.RS_Client
    14_2_RS_Self.TC_RS_self
    14_4_RS_Partner.TC_RS_partner

    14_5_RS_Loan_Summary.TC_Loan_summary
    14_5_RS_Loan_Summary.TC_General_Loan_details_validation
    
    Save
    Close Workbook

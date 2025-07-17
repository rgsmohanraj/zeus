*** Settings ***  
Resource           ../keywords/common.robot
Resource           ../PageObjects/14_6_Repayment_Schedule.robot

*** Test Cases ***
TC_excel_read_datas_for_client_details
    01__loginPage.Open Browser and login to Zeus    ${userName}        ${password}
    Open Workbook    ${CURDIR}\\Bulk_Upload.xlsx
    ${i}=        Set Variable    1
    ${s_no}=     Set Variable    0
    ${i}=    Evaluate    ${i}+${1}
    WHILE  ${s_no}!=None
        ${i}=       Set Variable      ${i}
        ${s_no}=    Read From Cell    A${i}
        Log To Console    \nRES: S.No: ${s_no}
        ${client_first_name}=        Read From Cell    F${i}
        ${client_middle_name}=       Read From Cell    G${i}
        ${client_last_name}=         Read From Cell    H${i}
        Set Test Variable    ${client_middle_name}
        IF  "${client_middle_name}"=="None"
            ${client_name}=          Catenate          ${client_first_name} ${client_last_name}
            Log To Console    \n fsa: ${client_name}
        ELSE
            ${client_name}=          Catenate          ${client_first_name} ${client_middle_name} ${client_last_name}
        END
        ${mobile_no}=                Read From Cell    S${i}
        ${principal}=                Read From Cell    AH${i}
        ${tenure}=                   Read From Cell    AI${i}
        ${disbursement_date}=        Read From Cell    AG${i}
        ${first_repayment_date}=     Read From Cell    AJ${i}
        ${disbursement_date}=        Convert Date      ${disbursement_date}       result_format=%Y-%m-%d
        ${first_repayment_date}=     Convert Date      ${first_repayment_date}    result_format=%Y-%m-%d
        ${interest}=                 Read From Cell    AL${i}
        ${charge_1_name}=            Read From Cell    AM1            # processing fee
        ${charge_2_name}=            Read From Cell    AN1            # documentation (or) Insurance
        ${charge_1}=                 Read From Cell    AM${i}
        ${charge_2}=                 Read From Cell    AN${i}
        ${state}=                    Read From Cell    V${i}
        ${external_id}               Read From Cell    B${i}
        Set Test Variable            ${client_name}
        Set Test Variable            ${mobile_no}
        Set Test Variable            ${principal}
        Set Test Variable            ${tenure}
        Set Test Variable            ${interest}
        Set Test Variable            ${disbursement_date}
        Set Test Variable            ${first_repayment_date}
        Set Test Variable            ${charge_1}
        Set Test Variable            ${charge_2}
        Set Test Variable            ${charge_1_name}
        Set Test Variable            ${charge_2_name}
        Set Test Variable            ${state}
        Set Test Variable            ${external_id}

        Log To Console            \n External ID: ${external_id}
        Log To Console            \n Client Name: ${client_name}
        Log To Console            \n Mobile Number: ${mobile_no}
        Log To Console            \n Principal: ${principal}
        Log To Console            \n Tenure: ${tenure}
        Log To Console            \n Interest: ${interest}
        Log To Console            \n Disbursement Date: ${disbursement_date}
        Log To Console            \n First Repayment Date: ${first_repayment_date}
        Log To Console            \n Charge 1: ${charge_1}
        Log To Console            \n Charge 2: ${charge_2}
        Log to Console            \n State: ${state}

        ${date}=    Set Variable    ${first_repayment_date}
        Set Test Variable    ${date}
        #other codes
        14_6_Repayment_Schedule.TC_Repayement_Schedule_validations
        ${i}=        Evaluate    ${i}+${1}
        ${i}=        Set Variable    ${i}
        ${s_no}=     Read From Cell    A${i}
        Log To Console    \n next serial number: ${s_no}
    END
    Log To Console    \n Case Disbursed: ${result_disbursed}
    Log               \n Case Disbursed: ${result_disbursed}
    Save
    Close Workbook
    Close Browser

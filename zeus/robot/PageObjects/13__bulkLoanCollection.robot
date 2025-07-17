*** Settings ***
Resource           ../keywords/common.robot
Resource    ../testcases/11__getExcelTemp.robot

*** Variables ***
${bulkLoanPath}=            D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads//1.xlsx
${collectionFilePath}=      D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads//coll.xlsx
${resultCSV}=               D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads//2.xlsx
${collLoop}=                10
${SUCCESS}=                 SUCCESS
${counter}=                 ${2}
${transDate1}=              01-03-2020

*** Keywords ***
Download Bulk collection template
    [Documentation]   Download bulk collection template for partner and product
    Set Selenium Speed  0.3s
    Click Element    xpath://a[@href='#/organization/bulk-import']
    Click Element    xpath://h4[text()='Bulk Collections ']
    Click Element    xpath:(//*[contains(text(),'Download ')])[1]
    Log    Bulk collection template downloded

Collection write to excel
    [Documentation]    Write to recently downloaded file 
   Process Recent Excel File
   Set Global Variable   ${file_path}
   Open Workbook  ${file_path}
   
Write to collection excel using loan details and response excel
    [Documentation]    Write collection details in collection template
    # Read the value from response file & write it to collection excel
    FOR    ${row}    IN RANGE    1    ${collLoop}
        Open Workbook    ${resultCSV}
        ${loanStatus}=    Read From Cell  E${row+1} 
        ${externalId}    Read From Cell   C${row+1}
        ${loanId}        Read From Cell   D${row+1}
        Close Workbook
        IF  '${loanStatus}' == '${SUCCESS}'
            Open Workbook    ${collectionFilePath}
            Write To Cell    B${row+1}    ${loanId}
            Write To Cell    C${row+1}    ${externalId}
            Save
            Close Workbook
            Open Workbook    ${bulkLoanPath}
            # Read the value from bulk loan Excel file
            ${principal}    Read From Cell    AH${row+1}
            ${Interest}     Read From Cell    AL${row+1}
            ${int_12}    Evaluate    (${Interest} / 12)/100
            ${loanTenure}   Read From Cell    AI${row+1}
            ${transDate}    Read From Cell    AJ${row+1}    
            Close Workbook
            # Write the value to the collection Excel file
            Open Workbook    ${collectionFilePath}
            Write To Cell    A${row+1}    ${row}
            Write To Cell    D${row+1}    ${1}
            Write To Cell    F${row+1}    ${transDate}   number_format=dd-mm-yyyy 
            Write To Cell    G${row+1}    ${10}
            Write To Cell    J${row+1}    NACH
            ${emi}=       Evaluate    round(${principal}*((${int_12}*((1+${int_12})**${loanTenure}))/(((1+${int_12})**${loanTenure})-1)),2)
            ${emi}=       Evaluate    math.ceil(${emi})
            Write To Cell    E${row+1}       ${emi}
            Save
            Close Workbook
        END
    END
    ${nextLoop}=    Evaluate    ${collLoop}+${1}
    Open Workbook    ${bulkLoanPath}
    # Read the loan tenure from bulk loan Excel file
    ${principal}    Read From Cell    AH${counter}
    ${Interest}     Read From Cell    AL${counter}
    ${int_12}       Evaluate    (${Interest} / 12)/100
    ${loanTenure}   Read From Cell    AI${counter}
    ${fullTenure}   Evaluate    ${nextLoop}+${loanTenure}-${1}
    Close Workbook
    Open Workbook    ${collectionFilePath}
    ${loanAccNo}=    Read From Cell    B${counter}
    ${exId}=         Read From Cell    C${counter}
    ${instNum}=      Read From Cell    D${counter}
    ${transDate}=    Read From Cell    F${counter}
    ${refNum}=       Read From Cell    G${counter}
    ${repayMode}=    Read From Cell    J${counter}
    Close Workbook
    FOR    ${row}    IN RANGE    ${nextLoop}    ${fullTenure}
    #   FOR    ${r}    IN RANGE    1    ${collLoop}
        Open Workbook    ${collectionFilePath}
        Write To Cell    A${row}       ${row - ${1}}
        Write To Cell    B${row}       ${loanAccNo}
        Write To Cell    C${row}       ${exId}
        Write To Cell    D${row}       ${counter}
        ${transDate1}=    Set Variable    ${transDate1}
        ${day}    ${month}    ${year}    Split String    ${transDate1}    -
        ${month}    Run Keyword If    "${month[0]}" == "0"    Evaluate    "${month[1:]}"    ELSE    Set Variable    ${month}
        ${incrementedMonth}    Evaluate    int(${month}) + 1
        ${newMonth}    Set Variable If    ${incrementedMonth} <= 12    ${incrementedMonth}    1
        ${newMonth}    Convert To String    ${newMonth}
        IF  "${newMonth[0]}" != "0" and ${newMonth} != 10 and ${newMonth} != 11 and ${newMonth} != 12
           ${newMonth}  Run Keyword If  ${newMonth} !=0  Set Variable    0${newMonth}
        ELSE
           ${newMonth}  Set Variable    ${newMonth}
        END
        # ${newMonth}    Run Keyword If   "${month[0]}" != "10" and "${month[0]}" != "11" and    Set Variable    ${newMonth}=0${newMonth}   
        ${incrementedYear}    Evaluate    int(${year}) + 1
        ${newYear}    Set Variable If   ${incrementedMonth} > 12    ${incrementedYear}    ${year}   
        ${transDate1}   Set Variable    ${day}-${newMonth}-${newYear}
        # ${transDate}=    Evaluate    datetime.datetime.strptime("${transDate}", "%Y-%m-%d %H:%M:%S") + datetime.timedelta(days=31)
        Write To Cell    F${row}       ${transDate1}        number_format=dd-mm-yyyy
        Write To Cell    G${row}       ${refNum}
        Write To Cell    J${row}       ${repayMode}
        ${emi}=       Evaluate    round(${principal}*((${int_12}*((1+${int_12})**${loanTenure}))/(((1+${int_12})**${loanTenure})-1)),2)
        ${emi}=       Evaluate    math.ceil(${emi})
        Write To Cell    E${row}       ${emi}
        ${counter}=      Evaluate    ${counter}+${1}
        Save
        Close Workbook
    #   END
    END


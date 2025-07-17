*** Settings ***
Resource    ../resources/imports.robot

*** Variables ***
&{url}             QA=https://zeus-qa.vivriticapital.com/zeus/#/login     UAT=http://10.100.10.116:4200/zeus/#/login
&{baseUrl}         QA=http://10.100.10.158:8443                           UAT=http://10.100.10.116:8443
${dir}             D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads
# ${bulkLoanFile}     D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads//2024-01-29 174154.xlsx
${bulkLoanFile}    D://Zeus//zeus//robot//PageObjects//testData//NOCPL_Bulk_Loan_2024-02-16 114305.xlsx
${browserName}     headlesschrome
${takeValue}       rs
${expCode}         200
${loanCount}       4
${partner}         NOCPL
${product}         NOCPL
${startLoanId}     1315
${endLoanId}       1318
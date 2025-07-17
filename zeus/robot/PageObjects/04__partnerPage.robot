*** Settings ***
Resource           ../keywords/common.robot
Resource           ../PageObjects/01__loginPage.robot


*** Variables ***
${partnerLimit}=      800000
${productName}=       Test
${panCard}=           YRUFH8239U
${cinNumber}=         L17110MH1973PLC019786
${address1}=          Prestige Zackriya
${address2}=          Thousand Lights
${pinCode}=           647288
${keyPerson}=         Test
${gstNum}=            18AABCU9603R1ZM
${approvedLimit}      8000000
${BeneficiaryName}    Vendata Limited
${BeneficiaryAcc}     182383839828
${IfscCode}           SBIN0005943
${getpartnerName}

#Partner_Details
*** Keywords ***
Create New Partner
    [Documentation]      Create a new partner
    Set Selenium Speed   0.4 seconds
    Click Element        xpath://a[text()=' Partner ']
    Click Element        xpath://button[@class='mat-focus-indicator mat-raised-button mat-button-base mat-primary ng-star-inserted']
    ${partnerName}=      FakerLibrary.first_name
    Input Text           xpath://input[@formcontrolname='partnerName']                    ${partnerName}   
    Click Element        xpath://mat-select[@formcontrolname='source']
    Click Element        xpath://span[text()=' Direct ']        
    Input Text           xpath://input[@formcontrolname='panCard']                        ${panCard}
    Input Text           xpath://input[@formcontrolname='cinNumber']                      ${cinNumber}
    Input Text           xpath://input[@formcontrolname='address1']                       ${address1}
    Input Text           xpath://input[@formcontrolname='address2']                       ${address2}
    Click Element        xpath://mat-select[@formcontrolname='city']
    Click Element        xpath://span[text()=' Chennai ']
    Click Element        xpath://mat-select[@formcontrolname='state']       
    Click Element        xpath://span[text()=' Tamil Nadu ']
    Input Text           xpath://input[@formcontrolname='pincode']                        ${pinCode}
    Click Element        xpath://mat-select[@formcontrolname='country']
    Click Element        xpath://span[text()=' India ']
    Click Element        xpath://mat-select[@formcontrolname='constitution']
    Click Element        xpath://span[text()=' Private Limited ']
    Input Text           xpath://input[@formcontrolname='keyPersons']                     ${keyPerson}
    Click Element        xpath://mat-select[@formcontrolname='industry']
    Click Element        xpath://span[text()=' Other financial service activities ']
    Click Element        xpath://mat-select[@formcontrolname='sector']
    Click Element        xpath://span[text()=' Financial leasing ']
    Click Element        xpath://mat-select[@formcontrolname='subSector']
    Click Element        xpath://span[text()=' Financial leasing ']
    Input Text           xpath://input[@formcontrolname='gstNumber']                      ${gstNum}
    Click Element        xpath://mat-select[@formcontrolname='gstRegistration']
    Click Element        xpath://span[text()=' Normal Taxpayer ']
    Click Element        xpath:(//button[@type='submit'])[1]

    Click Element        xpath://mat-select[@formcontrolname='partnerType']
    Click Element        xpath://span[text()=' Digital Lending ']
    Input Text           xpath://input[@formcontrolname='approvedLimit']                  ${approvedLimit}
    Click Element        xpath:(//button[@type='submit'])[2]

    Input Text           xpath://input[@formcontrolname='beneficiaryName']                ${BeneficiaryName}
    Input Text           xpath://input[@formcontrolname='beneficiaryAccountNumber']       ${BeneficiaryAcc}
    Input Text           xpath://input[@formcontrolname='ifscCode']                       ${IfscCode} 
    Click Element        xpath:(//button[@type='submit'])[3]
    Click Element        xpath://span[text()=' Submit ']   
    Log                  Partner Created




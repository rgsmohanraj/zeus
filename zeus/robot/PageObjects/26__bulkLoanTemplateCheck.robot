*** Settings ***
Resource    ../keywords/common.robot
Resource    ../testcases/11__getExcelTemp.robot

*** Keywords ***
Bulk Loan template test
  [Documentation]    Check the Bulk loan excel template file
  ${bulkLoanFile}    11__getRecentFile.Get Most Recent Excel File
  Open Workbook   ${bulkLoanFile}
  ${SnoXl}          Read From Cell    A${1}
  Should Be Equal   ${SnoXl}          S.NO*
  ${exIdXl}         Read From Cell    B${1}
  Should Be Equal   ${exIdXl}         External Id*
  ${entityXl}       Read From Cell    C${1}
  Should Be Equal   ${entityXl}       Entity*
  ${appTypeXl}      Read From Cell    D${1}
  Should Be Equal   ${appTypeXl}      Applicant Type*
  ${assetClsXl}     Read From Cell    E${1}
  Should Be Equal   ${assetClsXl}     Asset Class
  ${firstNameXl}    Read From Cell    F${1}
  Should Be Equal   ${firstNameXl}    First Name*
  ${middleNameXl}   Read From Cell    G${1}
  Should Be Equal   ${middleNameXl}   Middle Name
  ${lastNameXl}     Read From Cell    H${1}
  Should Be Equal   ${lastNameXl}     Last Name*
  ${ageXl}          Read From Cell    I${1}
  Should Be Equal   ${ageXl}          Age*
  ${genderXl}       Read From Cell    J${1}
  Should Be Equal   ${genderXl}       Gender*
  ${dobXl}          Read From Cell    K${1}
  Should Be Equal   ${dobXl}          DOB*
  IF  '${product}' == 'Seeds'
   ${panXl}          Read From Cell    L${1}
   Should Be Equal   ${panXl}          PAN*
   ${aadharXl}       Read From Cell    M${1}
   Should Be Equal   ${aadharXl}       Aadhaar
  ELSE
   ${panXl}          Read From Cell    L${1}
   Should Be Equal   ${panXl}          PAN
   ${aadharXl}       Read From Cell    M${1}
   Should Be Equal   ${aadharXl}       Aadhaar*
  END
  ${voterIdXl}      Read From Cell    N${1}
  Should Be Equal   ${voterIdXl}      Voter ID
  ${passportNoXl}   Read From Cell    O${1}
  Should Be Equal   ${passportNoXl}   Passport Number
  ${dlXl}           Read From Cell    P${1}
  Should Be Equal   ${dlXl}           Driving License
  ${addressXl}      Read From Cell    Q${1}
  Should Be Equal   ${addressXl}      Address*
  ${pincodeXl}      Read From Cell    R${1}
  Should Be Equal   ${pincodeXl}      Pincode*
  ${mobNoXl}        Read From Cell    S${1}
  Should Be Equal   ${mobNoXl}        Mobile Number*
  ${emailXl}        Read From Cell    T${1}
  Should Be Equal   ${emailXl}        Email Address
  ${cityXl}         Read From Cell    U${1}
  Should Be Equal   ${cityXl}         City*
  ${stateXl}        Read From Cell    V${1}
  Should Be Equal   ${stateXl}        State*
  ${submittedOnXl}  Read From Cell    W${1}
  Should Be Equal   ${submittedOnXl}  Submitted On*
  ${actDateXl}      Read From Cell    X${1}
  Should Be Equal   ${actDateXl}      Activation Date*
  ${benNameXl}      Read From Cell    Y${1}
  Should Be Equal   ${benNameXl}      Beneficiary Name*
  ${benAccNoXl}     Read From Cell    Z${1}
  Should Be Equal   ${benAccNoXl}     Beneficiary Account No*
  ${accTypeXl}      Read From Cell    AA${1}
  Should Be Equal   ${accTypeXl}      Account Type*
  ${ifscXl}         Read From Cell    AB${1}
  Should Be Equal   ${ifscXl}         IFSC*
  ${micrXl}         Read From Cell    AC${1}
  Should Be Equal   ${micrXl}         MICR Code
  ${swiftCodeXl}    Read From Cell    AD${1}
  Should Be Equal   ${swiftCodeXl}    Swift Code
  ${branchXl}       Read From Cell    AE${1}
  Should Be Equal   ${branchXl}       Branch
  ${loanSubOnXl}    Read From Cell    AF${1}
  Should Be Equal   ${loanSubOnXl}    Loan Submitted On*
  ${disDateXl}      Read From Cell    AG${1}
  Should Be Equal   ${disDateXl}      Disbursement Date*
  ${principalXl}    Read From Cell    AH${1}
  Should Be Equal   ${principalXl}    Principal*
  ${loanTermXl}     Read From Cell    AI${1}
  Should Be Equal   ${loanTermXl}     Loan Term*
  ${1stRepayOnXl}   Read From Cell    AJ${1}
  Should Be Equal   ${1stRepayOnXl}   First Repayment On*
  ${dueDayXl}       Read From Cell    AK${1}
  Should Be Equal   ${dueDayXl}       Due Day*
  ${interestXl}     Read From Cell    AL${1}
  Should Be Equal   ${interestXl}     Interest Rate*
  IF  '${product}' == 'NOCPL'
   ${charge1Xl}      Read From Cell    AM${1}
   Should Be Equal   ${charge1Xl}      NOCPL Processing fee    
   ${charge2Xl}      Read From Cell    AN${1}
   Should Be Equal   ${charge2Xl}      NOCPL Insurance Charge
  ELSE IF   '${product}' == 'Dvara'
   ${charge1Xl}      Read From Cell    AM${1}
   Should Be Equal   ${charge1Xl}      Dvara Processing Fee    
   ${charge2Xl}      Read From Cell    AN${1}
   Should Be Equal   ${charge2Xl}      Dvara Insurance Charge
  ELSE IF   '${product}' == 'Navdhan'
   ${charge1Xl}      Read From Cell    AM${1}
   Should Be Equal   ${charge1Xl}      Navdhan Processing Fees    
   ${charge2Xl}      Read From Cell    AN${1}
   Should Be Equal   ${charge2Xl}      Navdhan Insurance Charges - Life Cover
  ELSE IF   '${product}' == 'Seeds'
   ${charge1Xl}      Read From Cell    AM${1}
   Should Be Equal   ${charge1Xl}      Seeds Processing Fees    
   ${charge2Xl}      Read From Cell    AN${1}
   Should Be Equal   ${charge2Xl}      Seeds Insurance Charges - Life Cover
   ${charge3Xl}      Read From Cell    AO${1}
   Should Be Equal   ${charge3Xl}      Seeds Stamp Duty Charge
   ${charge4Xl}      Read From Cell    AP${1}
   Should Be Equal   ${charge4Xl}      Seeds Insurance Charges - Hospi Cash
   ${charge5Xl}      Read From Cell    AQ${1}
   Should Be Equal   ${charge5Xl}      Seeds Foreclosure Charges
  END
  Close Workbook
  Log    Partner:${partner} and Product:${product} bulk loan excel template checked successfully
  

  
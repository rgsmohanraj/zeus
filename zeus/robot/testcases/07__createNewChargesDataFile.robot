*** Settings *** 
Resource           ../keywords/common.robot    
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/07__chargesPage.robot
Test Template      Create New Charges
Test Teardown      InValid charges creation
Suite Setup        Open Browser and login to Zeus    ${userName}    ${password}

*** Test Cases ***                feesName               chargeTimeType       chargeCalculationType        Amount
TC_CH_02_Invalid_Amount         w House Loan Fees         Disbursement                Flat                !@000
TC_CH_03_Invalid Fees name      0!@ome Loan Fees          Disbursement                 % Amount           10
# TC_CH_04_Create_Charge          VI Twowheel Loan Fees   Disbursement      % Loan Amount + Interest      5
# TC_CH_05_Create_Charge          VI Gold Loan Fees       Disbursement            % Interest              7
# TC_CH_06_Invalid_Amount         I Gold Loan Fees        Disbursement            % Interest              10@1!?
# TC_CH_07_Invalid_Amount         I Gold Loan Fees        Disbursement            % Interest              7abv120

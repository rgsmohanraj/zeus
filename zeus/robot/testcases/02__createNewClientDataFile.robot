*** Settings ***
Documentation    Negative test for new client creation
Resource            ../keywords/common.robot
Resource            ../PageObjects/01__loginPage.robot
Resource            ../PageObjects/02__clientPage.robot
Suite Setup         Open Browser and login to Zeus       ${userName}        ${password}    
Test Template       Create New Client
Test Teardown       InValid Client Onboarding  


*** Test Cases ***                                first_name    last_name    external_id    mobile_num     mail_id
# TC_CL_02_Incorrect first name                       @rumugam      muruga        94         90974677543    vil@gmail.com
# TC_CL_03_Missing first name                        ${EMPTY}      Tech.         80          80974677543    ar@gmail.com
TC_CL_04_Incorrect last name                         Prem         Kali(!)        94          89074677541    ar@gmail.com
# TC_CL_05_Incorrect external Id                        Kali        Info           Two         80074677523    kl@gmail.com
# TC_CL_06_Incorrect mobile number                       Len        Tech           84          80974677       ll@gmail.com
# TC_CL_07_Incorrect mailId                              Abi        gal            85          81234627523    vbl!gmail.com
# TC_CL_08_Missing first name                            ${EMPTY}   Kra            86          81234627523    vbl@gmail.com
# TC_CL_09_Missing Last name                             Dinesh    ${EMPTY}        87          77234627523    din@gmail.com
# TC_CL_10_Missing External id                           Saranya    Jayapal     ${EMPTY}       66234627523    ss@gmail.com
# TC_CL_11_Missing mobile number                         Nagaraj    Ganesan        88          ${EMPTY}       NG@gmail.com
# TC_CL_12_Missing mailId                                Priya      Kannan         89          8172837837    ${EMPTY}
# TC_CL_13_Missing mobile num&Mail id                    Pavithra   Ramesh         90          ${EMPTY}      ${EMPTY}
# TC_CL_14_Missing first name&Mail id                    ${EMPTY}   Kanan          91          9172837837    ${EMPTY}
# TC_CL_15_Missing Last name&Mail id                      Jaya      ${EMPTY}       92          9102837837    ${EMPTY}
# TC_CL_16_Missing External id&Mail id                    Ram       Babu         ${EMPTY}      7102837837    ${EMPTY}
# TC_CL_17_Missing External id&Mob number                 Ravi      Babu         ${EMPTY}       ${EMPTY}     Ram@gmail.com
# TC_CL_18_Missing External id&Last name                 Ajith     ${EMPTY}      ${EMPTY}      99934627523    AK@gmail.com
# TC_CL_19_Missing External id&Invalid Last name         Ajith      Kum!@*       ${EMPTY}      93934627523    AKm@gmail.com
# TC_CL_20_Missing External id&Invalid First name        Chel9!@    Kumar        ${EMPTY}      94934627523    AKC@gmail.com
# TC_CL_21_Invalid Mail id&First name                    Sh!in      marva           93         94934007523    sh/!gmail.com
# TC_CL_22_Invalid Mail id&Last name                     Balaji     @ram            94         97934007523    bal><.gmail.com
# TC_CL_23_Invalid Mail id&First name                    Mo$%n      marva           95         94934007523    mo<>gmail.com
# TC_CL_24_Invalid Mobile number                         Anu        Thilak          96         949@!007523    Mohan@gmail.com
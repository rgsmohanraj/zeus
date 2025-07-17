*** Settings *** 
Documentation     Login Negative test for Zeus
Metadata    Version    ZEUS 1.0
Resource           ../keywords/common.robot    
Resource           ../PageObjects/01__loginPage.robot
Test Template      login to Zeus
Suite Setup        Invalid login check in Zeus    ${userName}        ${password}
Test Teardown     Close Browser


*** Test Cases ***                                             UserName                                            Password
TC_LI_02_Invalid username and password                         @dmin                                              @Viv
TC_LI_03_Correct username and wrong password                   admin                                              password
TC_LI_04_Wrong username and Correct password                   Keyan                                              Password@1234
TC_LI_05_Empty username and Correct password                   ${EMPTY}                                           Password@1234
TC_LI_06_Empty username and Empty password                     ${EMPTY}                                           ${EMPTY}

*** Settings *** 
Resource           ../keywords/common.robot    
Resource           ../PageObjects/01__loginPage.robot
Resource           ../PageObjects/03__createUserPage.robot
Test Template      Create New User
Test Teardown      Invalid User Onboarding      
Suite Setup        Open Browser and login to Zeus    ${userName}        ${password}    


*** Test Cases ***                                user         mail            firstname      lastname     password      repassword
TC_US_02_Already a user                          Nagaraj      akn@gmail.com      Karthi         aru        Pass@qwert   Pass@qwert
# TC_US_03_Invalid first name                     Karthik      AK@gmail.com      kash17          Aka        Pass@qwerty   Pass@qwe
# TC_US_04_Invalid mail Id                        Arun          ar1#$!gmail.com    Pandian        aru        Pass@qwerty   Pass@qwerty
# TC_US_05_Invalid First Name                     Alagu         al@gmail.com       Muj           aru        Pass@qwerty   Pass@qwerty
# TC_US_06_Invalid Last Name                      Sarnya      aru@gmail.com    Saranya           aru        Pass@qwerty   Pass@qwerty
# TC_US_07_Invalid Password                       Pavithra      aru@gmail.com    Pavi             aru        Pa!#>@qwerty  Pa!#>@qwerty
# TC_US_08_Wrong RePassword                       Nagaraj      aru@gmail.com    Naga              aru        Pass@qwerty   qwerty
# TC_US_09_Missing User Name                      ${EMPTY}      aru@gmail.com    Vijay            aru        Pass@qwerty   Pass@qwerty
# TC_US_10_Missing Mail                           Alagu         ${EMPTY}         Bala             aru        Pass@qwerty   Pass@qwerty
# TC_US_11_Missing First name                     Priya         aru@gmail.com    ${EMPTY}         aru        Pass@qwerty   Pass@qwerty
# TC_US_12_Missing Last name                      Ajith         aru@gmail.com    Ajith            ${EMPTY}   Pass@qwerty   Pass@qwerty
# TC_US_13_Missing password                       Jayakar       aru@gmail.com    Jayakar          aru        ${EMPTY}      Pass@qwerty
# TC_US_14_Missing repassword                     Sherin        aru@gmail.com    Sherin           aru        Pass@qwerty   ${EMPTY}
# TC_US_15_Missing Password and repassword        Balaji        aru@gmail.com    Balaji           aru        ${EMPTY}      ${EMPTY}
# TC_US_16_Missing mail and repassword            donis          ${EMPTY}         Balaji           aru        ass@qwerty   ${EMPTY}

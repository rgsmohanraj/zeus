*** Settings ***
Resource    ../PageObjects/24__mysqlConnect.robot

*** Test Cases ***
Test connect to mysql DB
    [Documentation]   MYsql connection test
    # QA Mysql DB connection
    Take Loan Id from Mysql DB
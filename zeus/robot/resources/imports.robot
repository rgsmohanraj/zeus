*** Settings ***
Library     SeleniumLibrary
Library     RequestsLibrary
Library     JSONLibrary
Library     String
Library     Collections
Library     FakerLibrary
Library     DateTime
Library     Dialogs
Library     OperatingSystem
Library     ExcellentLibrary
Library     ../PageObjects/CustomLibrary.py
Resource    ../keywords/common.robot
Variables   config/application_config.yaml
Library     CSVLibrary
Library     random
Library     DatabaseLibrary
Library    base64
Library    RPA.PDF


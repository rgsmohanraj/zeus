--liquibase formatted sql

-- changeset zeus :1
-- comment added interest due and received columns m_loan_accrual
ALTER table m_loan_accrual add column interest_accrued_but_not_due decimal(19,6) after accrued_amount;
ALTER table m_loan_accrual add column interest_accrued_but_not_received decimal(19,6) after accrued_amount;
ALTER table m_loan_accrual add column interest_due_received decimal(19,6) after accrued_amount;
ALTER table m_loan_accrual add column cumulative_accrued_amount decimal(19,6) after accrued_amount;
ALTER table m_loan_accrual add column self_interest_accrued_but_not_due decimal(19,6) after self_accrued_amount;
ALTER table m_loan_accrual add column self_interest_accrued_but_not_received decimal(19,6) after self_accrued_amount;
ALTER table m_loan_accrual add column self_interest_due_received decimal(19,6) after self_accrued_amount;
ALTER table m_loan_accrual add column self_cumulative_accrued_amount decimal(19,6) after self_accrued_amount;
ALTER table m_loan_accrual add column partner_interest_accrued_but_not_due decimal(19,6) after partner_accrued_amount;
ALTER table m_loan_accrual add column partner_interest_accrued_but_not_received decimal(19,6) after partner_accrued_amount;
ALTER table m_loan_accrual add column partner_interest_due_received decimal(19,6) after partner_accrued_amount;
ALTER table m_loan_accrual add column partner_cumulative_accrued_amount decimal(19,6) after partner_accrued_amount;
-- changeset zeus:2
-- comment Partner-wise accrual report for the day.
Insert into `stretchy_report` (`report_name`,`report_type`,`report_subtype`,`report_category`,`report_sql`,`description`,`core_report`,`use_report`,`self_service_user_report`,`jasper_report_template_path`) VALUES ('Partner Accrual Report','CSV',NULL,'Loan','SELECT p.partner_name as "Partner name", SUM(self_interest_accrued_but_not_received) as "Interest accrued but not received", SUM(self_interest_accrued_but_not_due) as "Interest accrued but not due" FROM m_loan_accrual acc INNER JOIN m_loan l ON acc.loan_id = l.id INNER JOIN m_partner p ON l.product_id = p.id WHERE date_format(from_date,"%Y-%m-%d") = DATE_SUB(STR_TO_DATE("${asOn}", "%d %M %Y"), INTERVAL 1 DAY) GROUP BY l.partner_id',NULL,b'0',b'1',b'0',NULL);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Partner Accrual Report' and p.id in (1009);
-- changeset zeus:3
-- comment Daily Accrual Report
Insert into `stretchy_report` (`report_name`,`report_type`,`report_subtype`,`report_category`,`report_sql`,`description`,`core_report`,`use_report`,`self_service_user_report`,`jasper_report_template_path`) VALUES ('Daily Accrual Report','CSV',NULL,'Loan','SELECT date_format(STR_TO_DATE("${asOn}", "%d %M %Y"),"%d-%m-%Y") as "Date", p.partner_name as "Partner name", SUM(self_accrued_amount) as "Sum of Daily interest accruals" FROM m_loan_accrual acc INNER JOIN m_loan l ON acc.loan_id = l.id INNER JOIN m_partner p ON l.product_id = p.id WHERE date_format(from_date,"%Y-%m-%d") <= DATE_SUB(STR_TO_DATE("${asOn}", "%d %M %Y"), INTERVAL 1 DAY) GROUP BY l.partner_id',NULL,b'0',b'1',b'0',NULL);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Daily Accrual Report' and p.id in (1009);
-- changeset zeus:4
-- comment Foreclosure Method Types
ALTER TABLE m_product_loan_collection_config ADD COLUMN foreclosure_method_type int(10);
-- changeset zeus:5
-- comment Foreclosure Method Types Enum addition
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('foreclosure_method_types', '1', 'PRINCIPAL_OUTSTANDING_INTEREST_OUTSTANDING', 'PRINCIPAL_OUTSTANDING_INTEREST_OUTSTANDING', b'0');
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('foreclosure_method_types', '2', 'PRINCIPAL_OUTSTANDING_INTEREST_DUE', 'PRINCIPAL_OUTSTANDING_INTEREST_DUE', b'0');
-- changeset zeus:6
-- comment Cooling Off data points in Loan Product Configuration
alter table m_product_loan_collection_config add column cooling_off_applicability bit(1) default 0,add column cooling_off_threshold_days int(10),add column cooling_off_interest_and_charge_applicability int(10),add column cooling_off_interest_logic_applicability int(10), add column cooling_off_days_in_year int(10),add column cooling_off_rounding_mode varchar(20),add column cooling_off_rounding_decimals int(10);
alter table m_loan_charge add column cooling_off_reversed bit(1) default 0 after is_active, add column cooling_off_retained_amount decimal(19,2) default 0 after cooling_off_reversed;
ALTER table m_loan add column cooling_off_reversed_charge_amount decimal(19,2) default 0 after sync_disbursement_with_meeting;
INSERT INTO m_permission (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('transaction_loan', 'COOLING_OFF_LOAN', 'LOAN', 'COOLING_OFF', 0);
-- changeset zeus:7
-- comment Foreclosure Method Types Enum addition
INSERT INTO `r_enum_value` (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('foreclosure_method_types', '3', 'PRINCIPAL_OUTSTANDING_INTEREST_ACCRUED', 'PRINCIPAL_OUTSTANDING_INTEREST_ACCRUED', b'0');
--changeset zeus:8
-- comment Collection Flag drop down values
INSERT INTO m_code (code_name, is_system_defined) VALUES ('CollectionFlag', true);
INSERT INTO m_code_value ( code_id, code_value, order_position, is_active, is_mandatory)
VALUES ((SELECT id FROM m_code WHERE code_name = 'CollectionFlag'), 'Cooling Off', 1, true, false),
((SELECT id FROM m_code WHERE code_name = 'CollectionFlag'), 'Collection', 2, true, false),
((SELECT id FROM m_code WHERE code_name = 'CollectionFlag'), 'Foreclosure', 3, true, false);

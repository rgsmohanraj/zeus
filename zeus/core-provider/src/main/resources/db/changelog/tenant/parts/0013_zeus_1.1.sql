--liquibase formatted sql

--changeset zeus:1
--comment Inserting Foreclosed status for loan closure
INSERT INTO r_enum_value (enum_name, enum_id, enum_message_property, enum_value, enum_type)
VALUES ('loan_status_id', 710, 'Foreclosed', 'Foreclosed', 0);
--changeset zeus:2
--comment Servicer Fees
alter table m_product_loan drop column vcpl_hurdle_rate;
INSERT INTO `zeus_colending`.`m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'CREATE_SERVICERFEECONFIG', 'SERVICERFEECONFIG', 'CREATE', b'0');
INSERT INTO `zeus_colending`.`m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('portfolio', 'UPDATE_SERVICERFEECONFIG', 'SERVICERFEECONFIG', 'UPDATE', b'0');
CREATE TABLE `m_servicer_fee_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `servicer_fee_enabled` bit(1) default NULL,
  `vcl_hurdle_rate` decimal(19,6) DEFAULT NULL,
  `vcl_interest_round` varchar(10) DEFAULT NULL,
  `vcl_interest_decimal` int(10) DEFAULT NULL,
  `servicer_fee_round` varchar(10) DEFAULT NULL,
  `servicer_fee_decimal` int(10) DEFAULT NULL,
  `sf_base_amt_gst_loss_enabled` bit(1) DEFAULT NULL,
  `sf_base_amt_gst_loss` decimal(19,6) DEFAULT NULL,
  `sf_gst` decimal(19,6) DEFAULT NULL,
  `sf_gst_round` varchar(10) DEFAULT NULL,
  `sf_gst_decimal` int(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_servicer_product_id` (`product_id`),
  CONSTRAINT `FK_servicer_product_id` FOREIGN KEY (`product_id`) REFERENCES `m_product_loan` (`id`)
);
CREATE TABLE `m_servicer_fee_charges_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `servicer_fee_config_id` bigint(20) NOT NULL,
  `charge_id` bigint(20) NOT NULL,
  `sf_self_share_charge` decimal(19,6) default NULL,
  `sf_partner_share_charge` decimal(19,6) default NULL,
  `sf_charge_amt_gst_loss_enabled` bit(1) default NULL,
  `sf_charge_amt_gst_loss` decimal(19,6) default NULL,
  `sf_charge_round` varchar(20) default NULL,
  `sf_charge_decimal` int(10) default NULL,
  `sf_charge_base_amount_roundingmode` varchar(20) default NULL,
  `sf_charge_base_amount_decimal` int(10) default NULL,
  `sf_charge_gst_roundingmode` varchar(20) default NULL,
  `sf_charge_gst_decimal` int(10) default NULL,
  `sf_charge_gst` decimal(19,6) default NULL,
  `is_active` bit(1) default NULL,
  PRIMARY KEY (`id`),
  KEY `FK_servicer_charges_servicer_fee_config_id` (`servicer_fee_config_id`),
  CONSTRAINT `FK_servicer_charges_servicer_fee_config_id` FOREIGN KEY (`servicer_fee_config_id`) REFERENCES `m_servicer_fee_config` (`id`)
);
CREATE TABLE `m_servicer_fee_calculation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `loan_transaction_rs_mapping_id` bigint(20) NOT NULL,
  `vcl_interest_amt_hurdle_rate` decimal(19,6) DEFAULT NULL,
  `sf_interest_base_amount` decimal(19,6) DEFAULT NULL,
  `sf_interest_gst_loss_amount` decimal(19,6) DEFAULT NULL,
  `sf_interest_gst_amount` decimal(19,6) DEFAULT NULL,
  `sf_interest_invoice_amount` decimal(19,6) DEFAULT NULL,
  `vcl_penal_amt_hurdle_rate` decimal(19,6) DEFAULT NULL,
  `sf_penal_base_amount` decimal(19,6) DEFAULT NULL,
  `sf_penal_gst_loss_amount` decimal(19,6) DEFAULT NULL,
  `sf_penal_gst_amount` decimal(19,6) DEFAULT NULL,
  `sf_penal_invoice_amount` decimal(19,6) DEFAULT NULL,
  `createdon_date` date DEFAULT NULL,
  `transaction_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_servicer_loan_id` (`loan_id`),
  KEY `FK_servicer_loan_transaction_rs_mapping_id` (`loan_transaction_rs_mapping_id`),
  CONSTRAINT `FK_servicer_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
  CONSTRAINT `FK_servicer_loan_transaction_rs_mapping_id` FOREIGN KEY (`loan_transaction_rs_mapping_id`) REFERENCES `m_loan_transaction_repayment_schedule_mapping` (`id`)
);
alter table m_loan_charge add column sf_charge_invoice_amount decimal(19,6) after partner_amount_waived_derived,
    add column sf_charge_gst_amount decimal(19,6) after partner_amount_waived_derived,
    add column sf_charge_gst_loss_amount decimal(19,6) after partner_amount_waived_derived,
    add column sf_partner_base_amount decimal(19,6) after partner_amount_waived_derived,
    add column sf_self_base_amount decimal(19,6) after partner_amount_waived_derived,
    add column sf_partner_share decimal(19,6) after partner_amount_waived_derived,
    add column sf_self_share decimal(19,6) after partner_amount_waived_derived,
    add column servicer_fee_enabled bit(1) after partner_amount_waived_derived;
alter table m_loan add column servicer_fee_config_id bigint(10) after product_id;
--changeset zeus:3
--comment Demand Report Supporting Future Date
Alter table stretchy_report add column jasper_report_template_path varchar(500);
INSERT INTO `stretchy_report` (`report_name`,`report_type`,`report_subtype`,`report_category`,`report_sql`,`description`,`core_report`,`use_report`,`self_service_user_report`,`jasper_report_template_path`) VALUES ('Demand Report','CSV',NULL,'Loan','select cv.code_value as \"Product\",\nlo.account_no as \"Loan Account No\",\nlo.external_id as \"External ID\",\nrs.installment as \"Installment Number\",\ndate_format(rs.duedate, \"%d-%m-%Y\") as \"Due Date\",\n(IFNULL(rs.principal_amount,0) - IFNULL(rs.principal_completed_derived,0)) as \"Principal\",\n(IFNULL(rs.interest_amount,0) - IFNULL(rs.interest_completed_derived,0)) as \"Interest\",\nIFNULL(penalty_charges_amount,0) as \"Penal Charges\",\n(IFNULL(rs.principal_amount,0) - ifnull(rs.principal_completed_derived,0)) + ((IFNULL(rs.interest_amount,0) - (IFNULL(rs.interest_completed_derived,0)))) as \"Amount\",\n(case rs.completed_derived\nwhen 0 then \"Not Paid/Partially Paid\" \nend) as \"Status\",\n(IFNULL(rs.self_principal_amount,0) - IFNULL(rs.self_principal_completed_derived,0)) as \"Self Principal\",\n(IFNULL(rs.self_interest_amount,0) - IFNULL(rs.self_interest_completed_derived,0)) as \"Self Interest\",\nIFNULL(rs.self_penalty_charges_amount,0) as \"Self Penal Charges\",\n(IFNULL(rs.self_principal_amount,0) - ifnull(rs.self_principal_completed_derived,0)) + ((IFNULL(rs.self_interest_amount,0) - (IFNULL(rs.self_interest_completed_derived,0)))) as \"Self Amount\",\n(IFNULL(rs.partner_principal_amount,0) - IFNULL(rs.partner_principal_completed_derived,0)) as \"Partner Principal\",\n(IFNULL(rs.partner_interest_amount,0) - IFNULL(rs.partner_interest_completed_derived,0)) as \"Partner Interest\",\nIFNULL(rs.partner_penalty_charges_amount,0) as \"Partner Penal Charges\",\n(IFNULL(rs.partner_principal_amount,0) - ifnull(rs.partner_principal_completed_derived,0)) + ((IFNULL(rs.partner_interest_amount,0) - (IFNULL(rs.partner_interest_completed_derived,0)))) as \"Partner Amount\",\nsf.vcpl_hurdle_rate as \"Self Yield\",\nlp.self_principal_share as \"Participation Ratio\"\nfrom m_loan lo\nleft join m_partner pr on pr.id = lo.partner_id\nleft join m_product_loan lp on lp.id = lo.product_id\nleft join m_loan_transaction txn on txn.loan_id = lo.id\nleft join m_loan_repayment_schedule rs on rs.loan_id = lo.id\nleft join r_enum_value en on en.enum_name = \"loan_status_id\" and en.enum_id = lo.loan_status_id\nleft join m_code_value cv on cv.id = lp.asset_class_cv_id\nleft join m_servicer_fee_config sf on sf.id = lo.product_id\nwhere pr.id = ${partnerId}\nand rs.completed_derived in (0)\nand lo.loan_status_id not in (100,200,500)\nand rs.duedate <= STR_TO_DATE(\"${dueDate}\", \"%d %M %Y\")\ngroup by rs.id\norder by lo.id,rs.installment',NULL,b'0',b'1',b'0',NULL);
INSERT INTO `stretchy_parameter` (`id`,`parameter_name`,`parameter_variable`,`parameter_label`,`parameter_displayType`,`parameter_FormatType`,`parameter_default`,`special`,`selectOne`,`selectAll`,`parameter_sql`,`parent_id`) VALUES (1027,'dueDate','dueDate','Pending Dues Till Date','fDate','date','today',NULL,NULL,NULL,NULL,NULL);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Demand Report' and p.id in (1023);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Demand Report' and p.id in (1027);
--changeset zeus:4
--comment SOA Report Which supports scripts for Template file mapping and Parameters mapping
Insert into stretchy_report(report_name,report_type,report_category,core_report,use_report,jasper_report_template_path,self_service_user_report) values
('Statement of Accounts','PDF','Loan',false,true,'/home/ec2-user/zeus-reports/StatementofAccount.jrxml',false);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Statement of Accounts' and p.id in (1004);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Statement of Accounts' and p.id in (1009);
-- changeset zeus:5
-- comment collection inflow report and collection appropriation report
INSERT INTO `stretchy_report` (`report_name`,`report_type`,`report_subtype`,`report_category`,`report_sql`,`description`,`core_report`,`use_report`,`self_service_user_report`,`jasper_report_template_path`) VALUES ('Collection Inflow Report','CSV',NULL,'Loan','select lo.partner_id as \"Partner ID\",\n pr.partner_name as \"Partner Name\",\n cl.display_name as \"End Borrower Name\",\n lo.external_id as \"External ID\",\n lo.account_no as \"Loan Account No\", \n txn.amount as \"Paid Amount\",\n txn.transaction_date as \"Paid Date\",\n txn.partner_transfer_utr as \"PartnerTransaferutr\",\n txn.partner_transfer_date as \"PartnerTransferDate\",\n txn.receipt_reference_number as \"Receipt reference Number\",\n cv.code_value as \"Repayment Mode\"\n from m_loan_transaction txn\n left join m_loan lo on  lo.id  = txn.loan_id \n left join m_client cl on cl.id = lo.client_id\n left join m_partner pr on pr.id = lo.partner_id\n left join m_code_value cv on cv.id = txn.repayment_mode_cv_id\n where pr.id = ${partnerId} and\n txn.transaction_type_enum not in (1) and \n txn.transaction_date between STR_TO_DATE(\"${startDate}\", \"%d %M %Y\") and STR_TO_DATE(\"${endDate}\", \"%d %M %Y\")   and \n txn.transaction_type_enum in (2,6,11,25) and txn.createdon_userid not in (2) and\n txn.is_reversed = 0 \n order by txn.transaction_date',NULL,b'0',b'1',b'0',NULL);
INSERT INTO `stretchy_report` (`report_name`,`report_type`,`report_subtype`,`report_category`,`report_sql`,`description`,`core_report`,`use_report`,`self_service_user_report`,`jasper_report_template_path`) VALUES ('Collection Appropriation Report','CSV',NULL,'Loan','select lo.partner_id as \"Partner ID\",\nlo.id as \"loan_id\",\n pr.partner_name as \"Partner Name\",\n cl.display_name as \"End Borrower Name\",\n lo.external_id as \"External ID\",\n lo.account_no as \"Loan Account No\", \n rs.installment as \"Installemt Number\",\n txn.transaction_date as \"Paid Date\",\n txnrs.interest_portion_derived as \"Interest Paid\",\ntxnrs.principal_portion_derived as \"Principal Paid\",\ntxnrs.advance_amount as \"Advance Amount\",\nif(rs.obligations_met_on_date between STR_TO_DATE(\"${startDate}\", \"%d %M %Y\") and STR_TO_DATE(\"${endDate}\", \"%d %M %Y\"), \"PAID\",\"PARTIALLY PAID\") as \"status\"\n from  m_loan_transaction_repayment_schedule_mapping txnrs\n left join m_loan_transaction txn on txn.id = txnrs.loan_transaction_id\n left join m_loan_repayment_schedule rs on rs.id= txnrs.loan_repayment_schedule_id\n left join m_loan lo on  lo.id  = txn.loan_id \n  left join m_client cl on cl.id = lo.client_id\n left join m_partner pr on pr.id = lo.partner_id\n where  pr.id = 1 and\n txnrs.loan_transaction_id in (select id from m_loan_transaction txn where  txn.transaction_date  between STR_TO_DATE(\"${startDate}\", \"%d %M %Y\") and STR_TO_DATE(\"${endDate}\", \"%d %M %Y\"))\n group by txnrs.id\n order by txn.transaction_date,txn.loan_id,rs.installment\n \n \n \n',NULL,b'0',b'1',b'0',NULL);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Collection Inflow Report' and p.id in (1);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Collection Inflow Report' and p.id in (2);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Collection Inflow Report' and p.id in (1023);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Collection Appropriation Report' and p.id in (1);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Collection Appropriation Report' and p.id in (2);
Insert into stretchy_report_parameter(report_id,parameter_id) select distinct r.id, p.id from stretchy_report r join stretchy_parameter p on r.report_name='Collection Appropriation Report' and p.id in (1023);
-- changeset zeus:6
-- comment Loan Transaction table alter script
Alter table m_loan_transaction modify column advance_amount decimal (19,6) after partner_outstanding_loan_balance_derived;
Alter table m_loan_transaction add column advance_amount_processed smallint(6) after advance_amount;
Alter table m_loan_transaction add column parent_id int(20) after advance_amount_processed;
-- changeset zeus:7
-- comment Collection Report table create script
CREATE TABLE `m_collection_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `loan_id` bigint(20) NOT NULL,
  `loan_transaction_id` bigint(20) NOT NULL,
  `installment_number` bigint(20) NOT NULL,
  `external_id` varchar(100) DEFAULT NULL,
  `transaction_type_enum` smallint(6) NOT NULL,
  `createdon_date` datetime DEFAULT NULL,
  `transaction_date` date NOT NULL,
  `amount` decimal(19,6) NOT NULL,
  `self_amount` decimal(19,6) DEFAULT NULL,
  `partner_amount` decimal(19,6) DEFAULT NULL,
  `principal_portion_derived` decimal(19,6) DEFAULT NULL,
  `self_principal_portion_derived` decimal(19,6) DEFAULT NULL,
  `partner_principal_portion_derived` decimal(19,6) DEFAULT NULL,
  `interest_portion_derived` decimal(19,6) DEFAULT NULL,
  `self_interest_portion_derived` decimal(19,6) DEFAULT NULL,
  `partner_interest_portion_derived` decimal(19,6) DEFAULT NULL,
  `fee_charges_portion_derived` decimal(19,6) DEFAULT NULL,
  `self_fee_charges_portion_derived` decimal(19,6) DEFAULT NULL,
  `partner_fee_charges_portion_derived` decimal(19,6) DEFAULT NULL,
  `penalty_charges_portion_derived` decimal(19,6) DEFAULT NULL,
  `self_penalty_charges_portion_derived` decimal(19,6) DEFAULT NULL,
  `partner_penalty_charges_portion_derived` decimal(19,6) DEFAULT NULL,
  `outstanding_loan_balance_derived` decimal(19,6) DEFAULT NULL,
  `self_outstanding_loan_balance_derived` decimal(19,6) DEFAULT NULL,
  `partner_outstanding_loan_balance_derived` decimal(19,6) DEFAULT NULL,
  `receipt_reference_number` varchar(40) DEFAULT NULL,
  `partner_transfer_utr` varchar(40) DEFAULT NULL,
  `partner_transfer_date` date DEFAULT NULL,
  `event` varchar(30) DEFAULT NULL,
  `advance_amount` decimal(19,6) DEFAULT NULL,
  `status` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_m_coolection_report_m_loan_transaction` (`loan_transaction_id`),
  CONSTRAINT `FK_m_coolection_report_m_loan_transaction` FOREIGN KEY (`loan_transaction_id`) REFERENCES `m_loan_transaction` (`id`)
 );
-- changeset zeus:8
-- comment adding vclhurdlerate in loan and loanhistory
alter table m_loan add column vcl_hurdle_rate decimal(19,6) DEFAULT NULL after product_id;
alter table m_loan_history add column vcl_hurdle_rate decimal(19,6) DEFAULT NULL after product_id;
-- changeset zeus:9
-- comment Added obligation_met_date,dpd & dpd_history to loan_repayment_schedule_history table during foreclosure scenario
alter table m_loan_repayment_schedule_history add column obligations_met_on_date date;
alter table m_loan_repayment_schedule_history add column dpd bigint;
alter table m_loan_repayment_schedule_history add column dpd_history bigint;
-- changeset zeus:10
-- comment Modified the datatype of closedon_date in m_loan_history table
ALTER TABLE m_loan_history modify column closedon_date date;
-- changeset zeus:11
-- comment Adding and modification of columns in m_loan_repayment_schedule_history
alter table m_loan_repayment_schedule_history add column due decimal(19,6) after installment,
add column principal_outstanding decimal(19,6) after partner_interest_amount,
add column self_principal_outstanding decimal(19,6) after principal_outstanding,
add column partner_principal_outstanding decimal(19,6) after self_principal_outstanding,
add column total_paid_late_derived decimal(19,6) after partner_principal_outstanding,
add column self_total_paid_late_derived decimal(19,6) after total_paid_late_derived,
add column partner_total_paid_late_derived decimal(19,6) after self_total_paid_late_derived,
add column dpd_tilldate date after partner_total_paid_late_derived,
add column self_fee_charges_amount decimal(19,6) after dpd_tilldate,
add column partner_fee_charges_amount decimal(19,6) after self_fee_charges_amount,
add column self_penalty_charges_amount decimal(19,6) after partner_fee_charges_amount,
add column partner_penalty_charges_amount decimal(19,6) after self_penalty_charges_amount,
add column accrual_interest_derived decimal(19,6) after partner_penalty_charges_amount,
add column self_accrual_interest_derived decimal(19,6) after accrual_interest_derived,
add column partner_accrual_interest_derived decimal(19,6) after self_accrual_interest_derived,
add column accrual_fee_charges_derived decimal(19,6) after partner_accrual_interest_derived,
add column self_accrual_fee_charges_derived decimal(19,6) after accrual_fee_charges_derived,
add column partner_accrual_fee_charges_derived decimal(19,6) after self_accrual_fee_charges_derived,
add column accrual_penalty_charges_derived decimal(19,6) after partner_accrual_fee_charges_derived,
add column self_accrual_penalty_charges_derived decimal(19,6) after accrual_penalty_charges_derived,
add column partner_accrual_penalty_charges_derived decimal(19,6) after self_accrual_penalty_charges_derived,
add column principal_completed_derived decimal(19,6) after partner_accrual_penalty_charges_derived,
add column self_principal_completed_derived decimal(19,6) after principal_completed_derived,
add column partner_principal_completed_derived decimal(19,6) after self_principal_completed_derived,
add column interest_completed_derived decimal(19,6) after partner_principal_completed_derived,
add column self_interest_completed_derived decimal(19,6) after interest_completed_derived,
add column partner_interest_completed_derived decimal(19,6) after self_interest_completed_derived,
add column interest_waived_derived decimal(19,6) after partner_interest_completed_derived,
add column self_interest_waived_derived decimal(19,6) after interest_waived_derived,
add column partner_interest_waived_derived decimal(19,6) after self_interest_waived_derived,
add column principal_writtenoff_derived decimal(19,6) after partner_interest_waived_derived,
add column self_principal_writtenoff_derived decimal(19,6) after principal_writtenoff_derived,
add column partner_principal_writtenoff_derived decimal(19,6) after self_principal_writtenoff_derived,
add column interest_writtenoff_derived decimal(19,6) after partner_principal_writtenoff_derived,
add column self_interest_writtenoff_derived decimal(19,6) after interest_writtenoff_derived,
add column partner_interest_writtenoff_derived decimal(19,6) after self_interest_writtenoff_derived,
add column fee_charges_completed_derived decimal(19,6) after partner_interest_writtenoff_derived,
add column self_fee_charges_completed_derived decimal(19,6) after fee_charges_completed_derived,
add column partner_fee_charges_completed_derived decimal(19,6) after self_fee_charges_completed_derived,
add column penalty_charges_completed_derived decimal(19,6) after partner_fee_charges_completed_derived,
add column self_penalty_charges_completed_derived decimal(19,6) after penalty_charges_completed_derived,
add column partner_penalty_charges_completed_derived decimal(19,6) after self_penalty_charges_completed_derived,
add column fee_charges_waived_derived decimal(19,6) after partner_penalty_charges_completed_derived,
add column self_fee_charges_waived_derived decimal(19,6) after fee_charges_waived_derived,
add column partner_fee_charges_waived_derived decimal(19,6) after self_fee_charges_waived_derived,
add column penalty_charges_waived_derived decimal(19,6) after partner_fee_charges_waived_derived,
add column self_penalty_charges_waived_derived decimal(19,6) after penalty_charges_waived_derived,
add column partner_penalty_charges_waived_derived decimal(19,6) after self_penalty_charges_waived_derived,
add column fee_charges_writtenoff_derived decimal(19,6) after partner_penalty_charges_waived_derived,
add column self_fee_charges_writtenoff_derived decimal(19,6) after fee_charges_writtenoff_derived,
add column partner_fee_charges_writtenoff_derived decimal(19,6) after self_fee_charges_writtenoff_derived,
add column penalty_charges_writtenoff_derived decimal(19,6) after partner_fee_charges_writtenoff_derived,
add column self_penalty_charges_writtenoff_derived decimal(19,6) after penalty_charges_writtenoff_derived,
add column partner_penalty_charges_writtenoff_derived decimal(19,6) after self_penalty_charges_writtenoff_derived,
add column total_paid_in_advance_derived	 decimal(19,6) after partner_penalty_charges_writtenoff_derived,
add column self_total_paid_in_advance_derived decimal(19,6) after total_paid_in_advance_derived,
add column completed_derived bit(1) after self_total_paid_in_advance_derived,
add column recalculated_interest_component bit(1) after completed_derived,
add column reschedule_interest_portion decimal(19,6) after recalculated_interest_component,
add column self_reschedule_interest_portion decimal(19,6) after reschedule_interest_portion,
add column partner_reschedule_interest_portion decimal(19,6) after self_reschedule_interest_portion;
alter table m_loan_repayment_schedule_history modify column self_due decimal(19,6) after due,modify column partner_due decimal(19,6) after self_due, modify column self_principal_amount decimal(19,6) after principal_amount,
modify column partner_principal_amount decimal(19,6) after self_principal_amount,modify column self_interest_amount decimal(19,6) after interest_amount,
modify column partner_interest_amount decimal(19,6) after self_interest_amount,modify column dpd int(11) after partner_total_paid_late_derived,
modify column dpd_history int(11) after dpd,modify column fee_charges_amount decimal(19,6) after dpd_tilldate,modify column self_fee_charges_amount decimal(19,6) after fee_charges_amount,modify column partner_fee_charges_amount decimal(19,6) after self_fee_charges_amount,
modify column penalty_charges_amount decimal(19,6) after partner_fee_charges_amount,modify column self_penalty_charges_amount decimal(19,6) after penalty_charges_amount,
modify column partner_penalty_charges_amount decimal(19,6) after self_penalty_charges_amount,
modify column obligations_met_on_date date after self_total_paid_in_advance_derived,
modify column created_date datetime after partner_reschedule_interest_portion,modify column createdby_id bigint(20) after created_date,
modify column lastmodified_date datetime after createdby_id,modify column lastmodifiedby_id bigint(20) after lastmodified_date,modify column version int(11) after lastmodifiedby_id;
--changeset zeus:12
-- comment modification of servicerfeeconfig and adding columns in Loanproduct
alter table m_product_loan add column servicer_fee_interest_config_enabled bit(1) after over_applied_number;
alter table m_product_loan add column servicer_fee_charges_config_enabled bit(1) after servicer_fee_interest_config_enabled;
alter table m_product_loan modify column createdon_date datetime after servicer_fee_charges_config_enabled,
    modify column createdon_userid smallint(6) after createdon_date,
    modify column modifiedon_date datetime after createdon_userid,
    modify column modifiedon_userid smallint(6) after modifiedon_date;
alter table m_loan drop column servicer_fee_config_id;
alter table m_servicer_fee_config drop column servicer_fee_enabled;
alter table m_servicer_fee_config add column createdon_date date, add column modifiedon_date date;
-- changeset zeus:13 endDelimiter:/
--comment Function to get DPD Count
DROP FUNCTION IF EXISTS get_dpd;
/
CREATE FUNCTION `get_dpd`(loanId BIGINT, reportdate DATE, loanStatus INTEGER, loanClosedDate DATE, reportType varchar(10)) RETURNS int(11)
BEGIN
	IF(reportDate > loanClosedDate AND (loanStatus = 600 OR loanStatus = 710) AND timestampdiff(MONTH,loanClosedDate, reportDate) >= 1)
	THEN
		RETURN 0;
	ELSE
		RETURN (SELECT (CASE WHEN(dpd_tbl.obligations_met_on_date IS NULL) THEN DATEDIFF(reportdate, dpd_tbl.duedate)
			WHEN(reportdate >= dpd_tbl.duedate AND reportdate < dpd_tbl.obligations_met_on_date) THEN DATEDIFF(reportdate, dpd_tbl.duedate)
			WHEN(reportdate <= dpd_tbl.duedate OR dpd_tbl.obligations_met_on_date < dpd_tbl.duedate) THEN 0
            WHEN(reportType = 'BUREAU' AND reportdate > dpd_tbl.duedate AND reportdate >= dpd_tbl.obligations_met_on_date) THEN DATEDIFF(dpd_tbl.obligations_met_on_date, dpd_tbl.duedate)
			WHEN(reportType = 'POS' AND reportdate > dpd_tbl.duedate AND reportdate >= dpd_tbl.obligations_met_on_date) THEN 0
		END) as dpd FROM ((SELECT duedate,obligations_met_on_date FROM m_loan_repayment_schedule
			WHERE loan_id = loanId AND duedate <= reportdate
            and ( obligations_met_on_date is null or obligations_met_on_date > reportdate)
			ORDER BY installment limit 1)
			UNION
			(SELECT duedate,obligations_met_on_date FROM m_loan_repayment_schedule
			WHERE loan_id = loanId AND duedate <= reportdate and obligations_met_on_date is not null
			ORDER BY obligations_met_on_date desc limit 1)) as dpd_tbl limit 1 );
	END IF;
END
/
-- changeset zeus:14 endDelimiter:/
--comment Function to get emi amount
DROP FUNCTION IF EXISTS reterive_emi_amount;
/
CREATE FUNCTION `reterive_emi_amount`(loanId BIGINT, reportdate DATE, closedOn DATE, maturedOn DATE, loanStatus INTEGER) RETURNS decimal(19,2)
BEGIN
	DECLARE loanClosedOn DATE;
	DECLARE loanMaturedOn DATE;
    SET loanClosedOn = date_format(closedOn,"%Y-%m-%d");
    SET loanMaturedOn = date_format(maturedOn,"%Y-%m-%d");
    IF (loanClosedOn IS NULL AND reportdate > loanMaturedOn) THEN
		RETURN (SELECT due FROM m_loan_repayment_schedule WHERE loan_id = loanId
			-- AND obligations_met_on_date IS NULL
            ORDER BY installment DESC LIMIT 1);
    ELSEIF (loanClosedOn IS NOT NULL AND (reportdate > loanClosedOn or reportdate = loanClosedOn)) THEN
			RETURN null;
	ELSEIF (loanClosedOn IS NOT NULL AND reportdate < loanClosedOn) THEN
		RETURN IF(loanStatus = 710,
			(SELECT (principal_amount + interest_amount) FROM m_loan_repayment_schedule_history
				WHERE loan_id  = loanId AND date_format(duedate,"%Y-%m-%d") >= reportdate
				ORDER BY installment LIMIT 1),
			(SELECT due FROM m_loan_repayment_schedule
				WHERE loan_id  = loanId AND date_format(duedate,"%Y-%m-%d") >= reportdate
				ORDER BY installment LIMIT 1));
    ELSE
		RETURN (SELECT due FROM m_loan_repayment_schedule
				WHERE loan_id = loanId
					AND date_format(duedate,"%Y-%m-%d") >= reportdate
				ORDER BY installment limit 1);
	END IF;
END
/
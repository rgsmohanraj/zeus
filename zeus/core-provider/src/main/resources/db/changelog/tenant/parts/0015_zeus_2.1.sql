--liquibase formatted sql

-- changeset zeus:1
--comment Adding m_loan_scheduler_registry to maintain scheduler last run
CREATE TABLE m_loan_scheduler_registry (
  id int(11) NOT NULL AUTO_INCREMENT,
  loan_id int(11) NOT NULL,
  installment int(11) DEFAULT NULL,
  penal_last_run_on date DEFAULT NULL,
  PRIMARY KEY (id)
);
-- changeset zeus:2
--comment Adding is_default to apply default charge
 ALTER TABLE m_charge ADD COLUMN is_default_loan_charge BIT(1) DEFAULT 0 AFTER is_active
-- changeset zeus:3
-- comment Adding foreclosure and advance data points in loan product
CREATE TABLE m_product_loan_collection_config (
id bigint(20) NOT NULL AUTO_INCREMENT,
product_id smallint(6) DEFAULT NULL,
advance_appropriation_on smallint(6) DEFAULT NULL,
advance_appropriation_against_on smallint(6) DEFAULT NULL,
advance_entry_enabled bit(1) DEFAULT NULL,
interest_benefit_enabled bit(1) DEFAULT NULL,
foreclosure_on_due_date_interest int(5) DEFAULT NULL,
foreclosure_on_due_date_charge int(5) DEFAULT NULL,
foreclosure_other_than_due_date_interest int(5) DEFAULT NULL,
foreclosure_other_than_due_date_charge int(5) DEFAULT NULL,
foreclosure_one_month_overdue_interest int(5) DEFAULT NULL,
foreclosure_one_month_overdue_charge int(5) DEFAULT NULL,
foreclosure_short_paid_interest int(5) DEFAULT NULL,
foreclosure_short_paid_interest_charge int(5) DEFAULT NULL,
foreclosure_principal_short_paid_interest int(5) DEFAULT NULL,
foreclosure_principal_short_paid_charge int(5) DEFAULT NULL,
foreclosure_two_months_overdue_interest int(5) DEFAULT NULL,
foreclosure_two_months_overdue_charge int(5) DEFAULT NULL,
foreclosure_pos_advance_on_due_date int(5) DEFAULT NULL,
foreclosure_advance_on_due_date_interest int(5) DEFAULT NULL,
foreclosure_advance_on_due_date_charge int(5) DEFAULT NULL,
foreclosure_pos_advance_other_than_due_date int(5) DEFAULT NULL,
foreclosure_advance_after_due_date_interest int(5) DEFAULT NULL,
foreclosure_advance_after_due_date_charge int(5) DEFAULT NULL,
foreclosure_backdated_short_paid_interest int(5) DEFAULT NULL,
foreclosure_backdated_short_paid_interest_charge int(5) DEFAULT NULL,
foreclosure_backdated_fully_paid_interest int(5) DEFAULT NULL,
foreclosure_backdated_fully_paid_interest_charge int(5) DEFAULT NULL,
foreclosure_backdated_short_paid_principal_interest int(5) DEFAULT NULL,
foreclosure_backdated_short_paid_principal_charge int(5) DEFAULT NULL,
foreclosure_backdated_fully_paid_emi_interest int(5) DEFAULT NULL,
foreclosure_backdated_fully_paid_emi_charge int(5) DEFAULT NULL,
foreclosure_backdated_advance_interest int(5) DEFAULT NULL,
foreclosure_backdated_advance_charge int(5) DEFAULT NULL,
PRIMARY KEY (id));
-- changeset zeus :4
-- comment Adding update script
INSERT INTO ref_loan_transaction_processing_strategy ( code, name, sort_order) VALUES ('vertical-interest-principal-order', 'Vertical Interest Principal Order', 8),('horizontal-interest-principal-order', 'Horizontal Interest Principal Order', 9);
-- changeset zeus :5 endDelimiter:/
--comment Function to get aadhaar decrypt
DROP FUNCTION IF EXISTS decrypt;
/
CREATE FUNCTION decrypt(encrypted_value VARCHAR(50), secret_key VARCHAR(16)) RETURNS VARCHAR(300)
BEGIN
RETURN (SELECT CAST(AES_DECRYPT(FROM_BASE64(encrypted_value),secret_key)AS CHAR));
END
/
-- changeset zeus :6
-- comment Inserting bounce charge job name in job table
INSERT INTO job (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `job_key`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`, `node_id`, `is_mismatched_job`) VALUES
('Apply Bounce Charge To Overdue Loans', 'Apply Bounce Charge To Overdue Loans', '0 1 0 * * ? *', now(), '7', 'Apply Bounce Charge To Overdue LoansJobDetail1 _ DEFAULT', true, false, true, 1, false, 1, false);
-- changeset zeus :7
-- comment added new column in servicer fee charges config table
alter table m_servicer_fee_charges_config add column servicer_fee_charges_ratio smallint(5);
-- changeset zeus :8
-- comment added new column for bounce charge development.
ALTER TABLE m_loan_transaction ADD COLUMN bounce_charges_portion_derived DECIMAL(19,6) AFTER partner_penalty_charges_portion_derived,
ADD  self_bounce_charges_portion_derived DECIMAL(19,6) AFTER bounce_charges_portion_derived ,
ADD partner_bounce_charges_portion_derived  DECIMAL(19,6) AFTER self_bounce_charges_portion_derived;


ALTER TABLE m_loan_repayment_schedule ADD COLUMN bounce_charges_amount DECIMAL(19,6) AFTER partner_penalty_charges_amount,
ADD bounce_charges_completed_derived  DECIMAL(19,6) AFTER partner_penalty_charges_completed_derived,
ADD bounce_charges_waived_derived  DECIMAL(19,6) AFTER partner_penalty_charges_waived_derived,
ADD bounce_charges_writtenoff_derived  DECIMAL(19,6) AFTER penalty_charges_writtenoff_derived,
ADD self_bounce_charges_amount  DECIMAL(19,6) AFTER bounce_charges_amount,
ADD self_bounce_charges_completed_derived  DECIMAL(19,6) AFTER bounce_charges_completed_derived,
ADD self_bounce_charges_writtenoff_derived  DECIMAL(19,6) AFTER bounce_charges_writtenoff_derived,
ADD self_bounce_charges_waived_derived  DECIMAL(19,6) AFTER bounce_charges_waived_derived,
ADD partner_bounce_charges_amount  DECIMAL(19,6) AFTER self_bounce_charges_amount,
ADD partner_bounce_charges_completed_derived  DECIMAL(19,6) AFTER self_bounce_charges_completed_derived,
ADD partner_bounce_charges_writtenoff_derived  DECIMAL(19,6) AFTER self_bounce_charges_writtenoff_derived,
ADD partner_bounce_charges_waived_derived  DECIMAL(19,6) AFTER self_bounce_charges_waived_derived;

ALTER TABLE m_loan_transaction_repayment_schedule_mapping ADD COLUMN bounce_charges_portion_derived DECIMAL(19,6) AFTER penalty_charges_portion_derived;

ALTER TABLE m_loan ADD COLUMN bounce_charges_repaid_derived  DECIMAL(19,6) AFTER partner_penalty_charges_charged_derived,
ADD bounce_charges_writtenoff_derived  DECIMAL(19,6) AFTER partner_penalty_charges_writtenoff_derived,
ADD self_bounce_charges_writtenoff_derived  DECIMAL(19,6) AFTER bounce_charges_writtenoff_derived,
ADD self_bounce_charges_repaid_derived  DECIMAL(19,6) AFTER bounce_charges_repaid_derived,
ADD partner_bounce_charges_repaid_derived  DECIMAL(19,6) AFTER self_bounce_charges_repaid_derived,
ADD bounce_charges_waived_derived  DECIMAL(19,6) AFTER partner_bounce_charges_repaid_derived,
ADD self_bounce_charges_waived_derived  DECIMAL(19,6) AFTER bounce_charges_waived_derived,
ADD partner_bounce_charges_waived_derived  DECIMAL(19,6) AFTER self_bounce_charges_waived_derived,
ADD bounce_charges_outstanding_derived  DECIMAL(19,6) AFTER partner_bounce_charges_waived_derived,
ADD self_bounce_charges_outstanding_derived  DECIMAL(19,6) AFTER bounce_charges_outstanding_derived,
ADD partner_bounce_charges_outstanding_derived  DECIMAL(19,6) AFTER self_bounce_charges_outstanding_derived,
ADD bounce_charges_charged_derived DECIMAL(19,6) AFTER partner_bounce_charges_outstanding_derived,
ADD self_bounce_charges_charged_derived DECIMAL(19,6) AFTER bounce_charges_charged_derived,
ADD partner_bounce_charges_charged_derived  DECIMAL(19,6) AFTER self_bounce_charges_charged_derived,
ADD partner_bounce_charges_writtenoff_derived  DECIMAL(19,6) AFTER self_bounce_charges_writtenoff_derived;

ALTER TABLE m_loan_history
ADD COLUMN bounce_charges_repaid_derived  DECIMAL(19,6) AFTER partner_penalty_charges_charged_derived,
ADD bounce_charges_writtenoff_derived  DECIMAL(19,6) AFTER partner_penalty_charges_writtenoff_derived,
Add self_bounce_charges_repaid_derived  decimal(19,6) AFTER bounce_charges_repaid_derived,
Add self_bounce_charges_writtenoff_derived  decimal(19,6) AFTER bounce_charges_writtenoff_derived,
Add partner_bounce_charges_repaid_derived  decimal(19,6) AFTER self_bounce_charges_repaid_derived,
ADD bounce_charges_waived_derived  DECIMAL(19,6) AFTER partner_bounce_charges_repaid_derived,
Add self_bounce_charges_waived_derived  decimal(19,6) AFTER bounce_charges_waived_derived,
Add partner_bounce_charges_waived_derived  decimal(19,6) AFTER self_bounce_charges_waived_derived,
ADD bounce_charges_outstanding_derived  DECIMAL(19,6) AFTER partner_bounce_charges_waived_derived,
Add self_bounce_charges_outstanding_derived  decimal(19,6) AFTER bounce_charges_outstanding_derived,
Add partner_bounce_charges_outstanding_derived  decimal(19,6) AFTER self_bounce_charges_outstanding_derived,
ADD bounce_charges_charged_derived DECIMAL(19,6) AFTER partner_bounce_charges_outstanding_derived,
Add self_bounce_charges_charged_derived decimal(19,6) AFTER bounce_charges_charged_derived,
Add partner_bounce_charges_charged_derived  decimal(19,6) AFTER self_bounce_charges_charged_derived,
Add partner_bounce_charges_writtenoff_derived  decimal(19,6) AFTER self_bounce_charges_writtenoff_derived;

ALTER TABLE m_loan_scheduler_registry ADD COLUMN bounce_last_run_on DATE AFTER penal_last_run_on;
-- changeset zeus :9
-- comment updated disbursement and servicer fee report.
UPDATE `zeus_colending`.`stretchy_report` SET `report_sql` = 'select lo.partner_id as \"Partner ID\", pr.partner_name as \"Partner Name\", cl.display_name as \"End Borrower Name\", lo.external_id as \"External Id\", lo.account_no as \"Loan Account No\", lo.principal_disbursed_derived as \"Loan Amount\", lo.annual_nominal_interest_rate as \"Interest Rate\", lo.term_frequency as \"Tenure (in months)\", lo.total_charges_due_at_disbursement_derived as \"Total Charges Deducted\", lo.total_gst_due_at_disbursement_derived as \"Total GST Deducted\", lo.net_disbursal_amount as \"Net Disbursal Amount\", lo.total_outstanding_derived as \"Total Outstanding Amount\", lxhd.xirr_value as \"XIRR Value\", sf.vcl_hurdle_rate as \"VCL Hurdle Rate\", date_format(rs.duedate, \"%d-%m-%Y\") as \"First Repayment Date\", date_format(lo.expected_maturedon_date, \"%d-%m-%Y\") as \"Maturity Date\", vp.payment_type as \"Transaction Type\", (select action from m_vpay_transaction_details vpp where vpp.event_type=\"PENNY_DROP\" and vpp.loan_id=lo.id order by vpp.id desc limit 1) as \"Penny Drop Status\", vp.utr as \"UTR No\", sum(coalesce(case lc.fees_charge_type_cv_id when  (select id from m_code_value where code_value=\"Processing Fees\" and code_id in (select id from m_code where code_name = \"FeesChargeType\")) then lc.amount + lc.total_gst end,0)) as \"Processing Fee\", sum(coalesce(case lc.fees_charge_type_cv_id when (select id from m_code_value where code_value=\"Insurance Charges\" and code_id in (select id from m_code where code_name = \"FeesChargeType\"))  then  lc.amount + lc.total_gst end,0)) as \"Insurance Charge\", sum(coalesce(case lc.fees_charge_type_cv_id when (select id from m_code_value where code_value=\"Insurance Life Cover\" and code_id in (select id from m_code where code_name = \"FeesChargeType\")) then lc.amount + lc.total_gst end,0)) as \"Insurance Life Cover\", sum(coalesce(case lc.fees_charge_type_cv_id when (select id from m_code_value where code_value=\"Insurance Hospicash\" and code_id in (select id from m_code where code_name = \"FeesChargeType\")) then lc.amount + lc.total_gst end,0)) as \"Insurance Hospicash\", sum(coalesce(case lc.fees_charge_type_cv_id when (select id from m_code_value where code_value=\"Stamp Duty\" and code_id in (select id from m_code where code_name = \"FeesChargeType\"))  then  lc.amount + lc.total_gst end,0)) as \"Stamp Duty\", date_format(lo.disbursedon_date, \"%d-%m-%Y\") as \"Disbursement Date\", en.enum_message_property as \"Status\" from m_loan lo left join m_client cl on cl.id = lo.client_id left join m_partner pr on pr.id = lo.partner_id left join m_product_loan lp on lp.id = lo.product_id left join m_loan_xirr_history_details lxhd on lxhd.loan_id = lo.id left join m_loan_repayment_schedule rs on rs.loan_id = lo.id and rs.installment = 1 left join m_loan_charge lc on lo.id = lc.loan_id and lc.fees_charge_type_cv_id = lc.fees_charge_type_cv_id left join r_enum_value en on en.enum_name = \"loan_status_id\" and en.enum_id = lo.loan_status_id left join m_vpay_transaction_details vp on vp.event_type=\"DISBURSEMENT\" and vp.loan_id = lo.id left join m_servicer_fee_config sf on sf.product_id = lp.id where pr.id = ${partnerId} and lo.loan_status_id not in (100,200,500) and lxhd.loan_event in (1) and lo.disbursedon_date between STR_TO_DATE(\"${startDate}\", \"%d %M %Y\") and STR_TO_DATE(\"${endDate}\", \"%d %M %Y\") group by lo.id order by lo.id' WHERE `report_name` like '%Disbursement Report%';
UPDATE `zeus_colending`.`stretchy_report` SET `report_sql` = 'SELECT * FROM ((select (SELECT code_value FROM m_code_value WHERE id = (SELECT asset_class_cv_id FROM m_product_loan WHERE id = lo.product_id)) as Product, pr.partner_name as \"Partner Name\", lo.account_no as \"Loan Account No\", (SELECT self_principal_share FROM m_product_loan WHERE id = (SELECT id FROM m_product_loan WHERE id = lo.product_id)) as \"Lender Participation Ratio\", lo.annual_nominal_interest_rate as \"Customer Interest Rate\", lo.vcl_hurdle_rate as \"Self Interest Rate\",\r \"\" as \"Installment\", lo.disbursedon_date as \"Due Date\", lo.disbursedon_date as \"Paid Date\", lo.disbursedon_date as \"Partner Transfer Date\",\r \"0\" as \"Principal\",\r \"0\" as \"Interest\", lc.amount as \"Pre Disbursement Charges\",\r \"0\" as \"Adhoc Charges\",\r \"0\" as \"Over Due Charge\",\r \"0\" as \"Penalty Charges\",\r \"0\" as \"Bounce Charges\",\r \"0\" as \"Foreclosure Charges\",\r \"0\" as \"Prepayment Charges\",\r \"0\" as \"Servicer Fee Interest\",\r \"0\" as \"Servicer Fee Interest Gross down for GST Loss\", lc.sf_partner_base_amount as \"Servicer Pre-Disbursement Charges\", lc.sf_charge_gst_loss_amount as \"Servicer Pre-Disbursement Charges Gross Down for GST loss\",\r \"0\" as \"Servicer BPI Share\",\r \"0\" as \"Servicer Penalty Share\",\r \"0\" as \"Servicer Bounce Share\",\r \"0\" as \"Servicer Prepayment Charges\",\r \"0\" as \"Servicer Foreclosure Charges\",\r \"0\" as \"Servicer Foreclosure Charges gross down for gst loss\",\r \"0\" as \"Servicer Bounce Charges\", \r \"0\" as \"Servicer Bounce Charges gross down for gst loss\",\r \"0\" as \"Servicer Overdue Share\", lc.sf_charge_gst_amount as \"GST\", lc.sf_charge_invoice_amount as \"Servicer Fee Payable\" from m_loan lo left join m_partner pr on pr.id = lo.partner_id left join m_loan_charge lc on lc.loan_id = lo.id where pr.id = ${partnerId} and lo.loan_status_id not in (100,\r 200,\r 500) and lc.servicer_fee_enabled = 1 and lc.charge_time_enum  in(1) and date_format(lo.disbursedon_date ,\r \"%Y-%m-%d\") BETWEEN STR_TO_DATE(\"${startDate}\",\r \"%d %M %Y\") AND STR_TO_DATE(\"${endDate}\",	\r \"%d %M %Y\")) \r union all (select  (SELECT code_value FROM m_code_value WHERE id = (SELECT asset_class_cv_id FROM m_product_loan WHERE id = lo.product_id)) as Product, pr.partner_name as \"Partner Name\", lo.account_no as \"Loan Account No\", (SELECT self_principal_share FROM m_product_loan WHERE id = (SELECT id FROM m_product_loan WHERE id = lo.product_id)) as \"Lender Participation Ratio\", lo.annual_nominal_interest_rate as \"Customer Interest Rate\", lo.vcl_hurdle_rate as \"Self Interest Rate\", lcp.installment_number as \"Installment\", txn.transaction_date as \"Due Date\", txn.transaction_date as \"Paid Date\", txn.partner_transfer_date as \"Partner Transfer Date\",\r \"0\" as \"Principal\",\r \"0\" as \"Interest\",\r \"0\" as \"Pre Disbursement Charges\",\r \"0\" as \"Adhoc Charges\",\r \"0\" as \"Over Due Charge\",\r \"0\" as \"Penalty Charges\",\r if(lc.charge_time_enum = 19 ,lc.amount ,0) as \"Bounce Charges\", \r if(lc.charge_time_enum = 17 ,lc.amount ,0) as \"Foreclosure Charges\",\r \"0\" as \"Prepayment Charges\",\r \"0\" as \"Servicer Fee Interest\",\r \"0\" as \"Servicer Fee Interest Gross down for GST Loss\",\r \"0\" as \"Servicer Pre-Disbursement Charges\",\r \"0\" as \"Servicer Pre-Disbursement Charges Gross Down for GST loss\",\r \"0\" as \"Servicer BPI Share\",\r \"0\" as \"Servicer Penalty Share\",\r \"0\" as \"Servicer Bounce Share\",\r \"0\" as \"Servicer Prepayment Charges\", \r if(lc.charge_time_enum = 17,lc.sf_partner_base_amount ,0) as \"Servicer Foreclosure Charges\", \r if(lc.charge_time_enum = 17,lc.sf_charge_gst_loss_amount,0) as \"Servicer Foreclosure Charges gross down for gst loss\",\r if(lc.charge_time_enum = 19,lc.sf_partner_base_amount,0) as \"Servicer Bounce Charges\", \r if(lc.charge_time_enum = 19,lc.sf_charge_gst_loss_amount,0) as \"Servicer Bounce Charges gross down for gst loss\",\r \"0\" as \"Servicer Overdue Share\", \r lc.sf_charge_gst_amount as \"GST\", \r lc.sf_charge_invoice_amount as \"Servicer Fee Payable\" from m_loan lo \r left join m_loan_charge lc on lc.loan_id = lo.id\r left join m_partner pr on pr.id = lo.partner_id \r left join m_loan_charge_paid_by lcp on lcp.loan_charge_id = lc.id\r left join m_loan_transaction txn on txn.id = lcp.loan_transaction_id\r where pr.id = ${partnerId} and txn.transaction_type_enum in (11,17) and lo.loan_status_id not in (100,\r 200,\r 500) and lc.servicer_fee_enabled = 1 and lc.charge_time_enum in (17,19) and date_format(txn.partner_transfer_date ,\r \"%Y-%m-%d\") BETWEEN STR_TO_DATE(\"${startDate}\",\r \"%d %M %Y\") AND STR_TO_DATE(\"${endDate}\",\r \"%d %M %Y\")) union all (select  cv.code_value as Product, pr.partner_name as \"Partner Name\", lo.account_no as \"Loan Account No\", lp.self_principal_share as \"Lender Participation Ratio\", lo.annual_nominal_interest_rate as \"Customer Interest Rate\", lo.vcl_hurdle_rate as \"Self Interest Rate\", rs.installment as \"Installment\", rs.duedate as \"Due Date\", txn.transaction_date as \"Paid Date\", txn.partner_transfer_date as \"Partner Transfer Date\", coalesce(txnrs.principal_portion_derived,\r 0) as \"Principal\", coalesce(txnrs.interest_portion_derived,\r 0) as \"Appropriated Interest Amount\",\r \"0\" as \"Pre Disbursement Charges\",\r \"0\" as \"Adhoc Charges\",\r \"0\" as \"Over Due Charge\",\r \"0\" as \"Penalty Charges\",\r \"0\" as \"Bounce Charges\",\r \"0\" as \"Foreclosure Charges\",\r \"0\" as \"Prepayment Charges\", sfcal.sf_interest_base_amount as \"Servicer Fee Interest\", sfcal.sf_interest_gst_loss_amount as \"Servicer Fee Interest Gross down for GST loss\",\r \"0\" as \"Servicer Pre-Disbursement Charges\",\r \"0\" as \"Servicer Pre-Disbursement Charges gross down for GST loss\",\r \"0\" as \"Servicer BPI Share\",\r \"0\" as \"Servicer Penalty Share\",\r \"0\" as \"Servicer Bounce Share\",\r \"0\" as \"Servicer Prepayment Charges\",\r \"0\" as \"Servicer Foreclosure Charges\",\r \"0\" as \"Servicer Foreclosure Charges gross down for gst loss\",\r \"0\" as \"Servicer Bounce Charges\", \r \"0\" as \"Servicer Bounce Charges gross down for gst loss\",\r \"0\" as \"Servicer Overdue Share\", sfcal.sf_interest_gst_amount as \"GST\", sfcal.sf_interest_invoice_amount as \"Servicer Fee Payable\" from m_loan lo left join m_partner pr on pr.id = lo.partner_id left join m_product_loan lp on lp.id = lo.product_id left join m_loan_transaction txn on txn.loan_id = lo.id left join m_loan_transaction_repayment_schedule_mapping txnrs on txnrs.loan_transaction_id = txn.id left join m_servicer_fee_calculation sfcal on sfcal.loan_transaction_rs_mapping_id = txnrs.id left join m_loan_repayment_schedule rs on rs.id = txnrs.loan_repayment_schedule_id left join r_enum_value en on en.enum_name = \"loan_status_id\" and en.enum_id = lo.loan_status_id left join m_code_value cv on cv.id = lp.asset_class_cv_id where pr.id = ${partnerId} and txn.transaction_type_enum not in (1,\r 5) and lo.loan_status_id not in (100,\r 200,\r 500) and txn.event not in ( \"ADVANCE\") and txnrs.interest_portion_derived>0 and txn.is_reversed=0 and lp.servicer_fee_interest_config_enabled = 1 and date_format(txn.partner_transfer_date ,\r \"%Y-%m-%d\") BETWEEN STR_TO_DATE(\"${startDate}\",\r \"%d %M %Y\") AND STR_TO_DATE(\"${endDate}\",\r \"%d %M %Y\"))) AS sf \r order by sf.Installment' WHERE `report_name` like '%Servicer Fee%';
-- changeset zeus :10
-- comment add new column for emi calculation
ALTER TABLE m_product_loan
ADD COLUMN emi_calc_logic smallint(6) After emi_multiples_of,
ADD COLUMN emi_days_in_month smallint(6) After emi_calc_logic,
ADD COLUMN emi_days_in_year smallint(6) After emi_days_in_month;
-- changeset zeus :11
-- comment added new enum for enum table
INSERT INTO r_enum_value (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('advance_appropriation_on', '1', 'RECEIPT_DATE', 'RECEIPT_DATE', false);
INSERT INTO r_enum_value (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('advance_appropriation_on', '2', 'ON_DUE_DATE', 'ON_DUE_DATE', false);
INSERT INTO r_enum_value (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('advance_appropriation_againt_on', '1', 'PRINCIPAL', 'PRINCIPAL', false);
INSERT INTO r_enum_value (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('advance_appropriation_againt_on', '2', 'INTEREST_PRINCIPAL', 'INTEREST_PRINCIPAL', false);
INSERT INTO r_enum_value (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('emi_calc_logic', '1', 'PMT_WITH_MONTHLY_INTEREST_RATE', 'PMT_WITH_MONTHLY_INTEREST_RATE', false);
INSERT INTO r_enum_value (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('emi_calc_logic', '2', 'PMT_WITH_YEARLY_INTEREST_RATE', 'PMT_WITH_YEARLY_INTEREST_RATE', false);
INSERT INTO r_enum_value (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('servicer_fee_charge_ratio', '1', 'FIXED_SPLIT', 'FIXED_SPLIT', false);
INSERT INTO r_enum_value (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('servicer_fee_charge_ratio', '2', 'DYNAMIC_SPLIT', 'DYNAMIC_SPLIT', false);
INSERT INTO r_enum_value (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('foreclosure_pos_enum', '1', 'RS_POS', 'RS_POS', false);
INSERT INTO r_enum_value (`enum_name`, `enum_id`, `enum_message_property`, `enum_value`, `enum_type`) VALUES ('foreclosure_pos_enum', '2', 'REVISED_POS', 'REVISED_POS', false);
-- changeset zeus :12
-- comment Gst changes for Bounce charge
ALTER TABLE m_loan_charge ADD COLUMN  gst_outstanding_derived  DECIMAL(19,6) AFTER partner_gst_paid,
ADD gst_paid_derived  DECIMAL(19,6) AFTER partner_gst_derived,
ADD gst_waivedoff_derived  DECIMAL(19,6) AFTER partner_gst_outstanding,
ADD self_gst_waivedOff  DECIMAL(19,6) AFTER gst_waivedoff_derived,
ADD partner_gst_waivedOff  DECIMAL(19,6) AFTER self_gst_waivedOff;
-- changeset zeus :13
-- comment added for sequence generator table name
CREATE TABLE sequence_generator(
    seq_name VARCHAR(255),
    seq_count BIGINT,
    PRIMARY KEY (`seq_name`)
);
-- changeset zeus :14
-- comment added zeus scheduler for vpay transaction enquiry
INSERT INTO job (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `job_key`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`, `node_id`, `is_mismatched_job`) VALUES
('Vpay transaction Enquiry', 'Vpay transaction Enquiry', '0 0 * ? * * *', now(), '1' , 'Vpay transaction EnquiryJobDetail1 _ DEFAULT', true, false, true, 2, false, 1, false);
-- changeset zeus :15
-- comment new record is added to m_permission table
INSERT INTO `zeus_colending`.`m_permission` (`id`, `grouping`, `code`, `entity_name`, `action_name`,
`can_maker_checker`) VALUES ('909', 'portfolio', 'UPDATE_AADHAAR', 'AADHAAR', 'UPDATE', b'0');
-- changeset zeus :16
-- comment added for indexing in loan history
CREATE INDEX loan_history_loan_id ON m_loan_history(loan_id);
-- changeset zeus :17
-- comment added to dropping the tenant schema
drop schema if exists zeus_tenants;
-- changeset zeus :18
-- comment added to  update LoanProduct
Alter table m_product_loan add column enable_backdated_disbursement bit(1) after over_amount_details;
-- changeset zeus :19
-- comment sync user scheduler
INSERT INTO job (`name`, `display_name`, `cron_expression`, `create_time`, `task_priority`, `job_key`, `is_active`, `currently_running`, `updates_allowed`, `scheduler_group`, `is_misfired`, `node_id`, `is_mismatched_job`,`group_name`) VALUES
('Sync Keycloak User', 'Sync Keycloak User', '0 0 0 * * ? *', now(), '1' , 'Sync Keycloak UserJobDetail1 _ zeus-colending', true, false, true, 3, false, 1, false,'zeus-colending');
-- changeset zeus :20
-- comment added to insert sequence number
insert ignore into sequence_generator (seq_name,seq_count) values ("m_loan_charge",(Select IFNULL(max(id),0) from m_loan_charge));
insert ignore into sequence_generator (seq_name,seq_count) values ("m_loan_repayment_schedule",(Select IFNULL(max(id),0) from m_loan_repayment_schedule));
-- changeset zeus :21
-- comment added to update the loan history numeric column to UUID
ALTER TABLE m_loan_history MODIFY id varchar(50);
UPDATE m_loan_history SET id = UUID();
-- changeset zeus :22 endDelimiter:/
--comment function to get dpd bucket
DROP FUNCTION IF EXISTS get_dpd_bucket;
/
CREATE FUNCTION get_dpd_bucket(dpd BIGINT) RETURNS varchar(20)
BEGIN
	IF (dpd <= 0) THEN RETURN NULL;
		ELSEIF (dpd > 0 && dpd <= 30) THEN RETURN 'SMA_0';
		ELSEIF (dpd >= 31 && dpd <= 60) THEN RETURN 'SMA_1';
		ELSEIF (dpd >= 61 && dpd <= 90) THEN RETURN 'SMA_2';
		ELSE RETURN 'NPA';
    END IF;
END
/
-- changeset zeus :23
-- comment added to update group name and job table
UPDATE `job` SET `group_name` = 'zeus-colending', `job_key` = 'Update loan SummaryJobDetail1 _ zeus-colending' WHERE (`name` = 'Update loan Summary');
UPDATE `job` SET `group_name` = 'zeus-colending', `job_key` = 'Update Loan Arrears AgeingJobDetail1 _ zeus-colending' WHERE (`name` = 'Update Loan Arrears Ageing');
UPDATE `job` SET `group_name` = 'zeus-colending', `job_key` = 'Add Accrual TransactionsJobDetail1 _ zeus-colending' WHERE (`name` = 'Add Accrual Transactions');
UPDATE `job` SET `group_name` = 'zeus-colending', `job_key` = 'Apply penalty to overdue loansJobDetail1 _ zeus-colending' WHERE (`name` = 'Apply penalty to overdue loans');
UPDATE `job` SET `group_name` = 'zeus-colending', `job_key` = 'Add Periodic Accrual TransactionsJobDetail1 _ zeus-colending' WHERE (`name` = 'Add Periodic Accrual Transactions');
UPDATE `job` SET `group_name` = 'zeus-colending', `job_key` = 'Execute All Dirty JobsJobDetail1 _ zeus-colending' WHERE (`name` = 'Execute All Dirty Jobs');
UPDATE `job` SET `group_name` = 'zeus-colending', `job_key` = 'DPD Calculation For Loans when no Overdue ChargeJobDetail1 _ zeus-colending' WHERE (`name` = 'DPD Calculation For Loans when no Overdue Charge');
UPDATE `job` SET `group_name` = 'zeus-colending', `job_key` = 'Advance Amount Appropriation on Due DateJobDetail1 _ zeus-colending' WHERE (`name` = 'Advance Amount Appropriation on Due Date');
UPDATE `job` SET `group_name` = 'zeus-colending', `job_key` = 'Apply Bounce Charge To Overdue LoansJobDetail1 _ zeus-colending' WHERE (`name` = 'Apply Bounce Charge To Overdue Loans');
UPDATE `job` SET `group_name` = 'zeus-colending', `job_key` = 'Vpay transaction EnquiryJobDetail1 _ zeus-colending' WHERE (`name` = 'Vpay transaction Enquiry');
UPDATE `job` SET `group_name` = 'zeus-colending', `job_key` = 'Sync Keycloak UserJobDetail1 _ zeus-colending' WHERE (`name` = 'Sync Keycloak User');
-- changeset zeus :24
-- comment added to add a new column in Repayment Schedule table
Alter table m_loan_repayment_schedule add column interest_principal_appropriated_on_date date after obligations_met_on_date,add column self_interest_principal_appropriated_on_date date after interest_principal_appropriated_on_date,add column partner_interest_principal_appropriated_on_date date after self_interest_principal_appropriated_on_date;
-- changeset zeus :25
-- comment added to add a new column in Repayment Schedule History table
Alter table m_loan_repayment_schedule_history add column interest_principal_appropriated_on_date date after obligations_met_on_date,add column self_interest_principal_appropriated_on_date date after interest_principal_appropriated_on_date ,add column partner_interest_principal_appropriated_on_date date after self_interest_principal_appropriated_on_date;
-- changeset zeus :26
-- comment updated demand and demand vs collection report
UPDATE `zeus_colending`.`stretchy_report` SET `report_sql` = 'select cv.code_value as \"Product\",lo.account_no as \"Loan Account No\",lo.external_id as \"External ID\",rs.installment as \"Installment Number\",date_format(rs.duedate, \"%d-%m-%Y\") as \"Due Date\",(IFNULL(rs.principal_amount,0) - IFNULL(rs.principal_completed_derived,0)) as \"Principal\",(IFNULL(rs.interest_amount,0) - IFNULL(rs.interest_completed_derived,0)) as \"Interest\",(IFNULL(rs.bounce_charges_amount,0) - IFNULL(rs.bounce_charges_completed_derived,0)) as \"Bounce Charge\",IFNULL(rs.penalty_charges_amount,0) as \"Penal Charges\",(IFNULL(rs.principal_amount,0) - ifnull(rs.principal_completed_derived,0)) + ((IFNULL(rs.interest_amount,0) - (IFNULL(rs.interest_completed_derived,0)))) as \"Amount\",(case rs.completed_derived when 0 then \"Not Paid/Partially Paid\" end) as \"Status\",(IFNULL(rs.self_principal_amount,0) - IFNULL(rs.self_principal_completed_derived,0)) as \"Self Principal\",(IFNULL(rs.self_interest_amount,0) - IFNULL(rs.self_interest_completed_derived,0)) as \"Self Interest\",(IFNULL(rs.self_bounce_charges_amount,0) - IFNULL(rs.self_bounce_charges_completed_derived,0)) as \"Self Bounce Charge\",IFNULL(rs.self_penalty_charges_amount,0) as \"Self Penal Charges\",(IFNULL(rs.self_principal_amount,0) - ifnull(rs.self_principal_completed_derived,0)) + ((IFNULL(rs.self_interest_amount,0) - (IFNULL(rs.self_interest_completed_derived,0)))) as \"Self Amount\",(IFNULL(rs.partner_principal_amount,0) - IFNULL(rs.partner_principal_completed_derived,0)) as \"Partner Principal\",(IFNULL(rs.partner_interest_amount,0) - IFNULL(rs.partner_interest_completed_derived,0)) as \"Partner Interest\",(IFNULL(rs.partner_bounce_charges_amount,0) - IFNULL(rs.partner_bounce_charges_completed_derived,0)) as \"Partner Bounce Charge\",IFNULL(rs.partner_penalty_charges_amount,0) as \"Partner Penal Charges\",(IFNULL(rs.partner_principal_amount,0) - ifnull(rs.partner_principal_completed_derived,0)) + ((IFNULL(rs.partner_interest_amount,0) - (IFNULL(rs.partner_interest_completed_derived,0)))) as \"Partner Amount\",sf.vcl_hurdle_rate as \"Self Yield\",lp.self_principal_share as \"Participation Ratio\" from m_loan lo left join m_partner pr on pr.id = lo.partner_id left join m_product_loan lp on lp.id = lo.product_id left join m_loan_transaction txn on txn.loan_id = lo.id left join m_loan_repayment_schedule rs on rs.loan_id = lo.id left join r_enum_value en on en.enum_name = \"loan_status_id\" and en.enum_id = lo.loan_status_id left join m_code_value cv on cv.id = lp.asset_class_cv_id left join m_servicer_fee_config sf on sf.id = lo.product_id where pr.id = ${partnerId} and rs.completed_derived in (0) and lo.loan_status_id not in (100,200,500) and rs.duedate <= STR_TO_DATE(\"${dueDate}\", \"%d %M %Y\") group by rs.id order by lo.id,rs.installment' WHERE `report_name` like '%Demand Report%';
UPDATE `zeus_colending`.`stretchy_report` SET `report_sql` = 'SELECT (SELECT code_value FROM m_code_value WHERE id = (SELECT asset_class_cv_id FROM m_product_loan WHERE id = lo.product_id)) as \"Product\", lo.account_no as \"Loan Account No\", lo.external_id as \"External ID\", \"\" as \"Partner Loan ID\", lo.vcl_hurdle_rate as \"VCL Hurdle Rate\", (SELECT principal_share FROM m_product_loan WHERE id = (SELECT product_id FROM m_loan WHERE id = rs.loan_id)) as \"Participation Ratio\", rs.installment as \"Installment Number\", date_format(rs.duedate, \"%d-%m-%Y\") as \"Due Date\", rs.principal_amount as \"Principal\", rs.interest_amount as \"Interest\", \"0\" as \"Overdue Charges\",IFNULL(rs.bounce_charges_amount,0) as \"Bounce Charges\", IFNULL(rs.penalty_charges_amount,0) as \"Penal Charges\", IFNULL(rs.fee_charges_amount,0) as \"Foreclosure Charges\", \"0\" as \"Prepayment Charges\", \"0\" as \"Adhoc Charges\", (IFNULL(rs.principal_amount,0) + IFNULL(rs.interest_amount,0) + IFNULL(rs.penalty_charges_amount,0) + IFNULL(rs.fee_charges_amount,0)) as \"Amount\", rs.self_principal_amount as \"Self Principal\", rs.self_interest_amount as \"Self Interest\", \"0\" as \"Self Overdue Charges\",IFNULL(rs.self_bounce_charges_amount,0) as \"Self Bounce Charges\", IFNULL(rs.self_penalty_charges_amount,0) as \"Self Penal Charges\", IFNULL(rs.self_fee_charges_amount,0) as \"Self Foreclosure Charges\", \"0\" as \"Self Prepayment Charges\", \"0\" as \"Self Adhoc Charges\", (IFNULL(rs.self_principal_amount,0) + IFNULL(rs.self_interest_amount,0) + IFNULL(rs.self_penalty_charges_amount,0) + IFNULL(rs.self_fee_charges_amount,0)) as \"Self Amount\", (IFNULL(rs.partner_principal_amount,0)) as \"Partner Principal\", (IFNULL(rs.partner_interest_amount,0)) as \"Partner Interest\", \"0\" as \"Partner  Overdue Charges\",IFNULL(rs.partner_bounce_charges_amount,0) as \"Partner Bounce Charges\", IFNULL(rs.partner_penalty_charges_amount,0) as \"Partner Penal Charges\", IFNULL(rs.partner_fee_charges_amount,0) as \"Partner Foreclosure Charges\", \"0\" as \"Partner Prepayment Charges\",	\"0\" as \"Partner Adhoc Charges\", (IFNULL(rs.partner_principal_amount,0) + IFNULL(rs.partner_interest_amount,0) + IFNULL(rs.partner_penalty_charges_amount,0) + IFNULL(rs.partner_fee_charges_amount,0)) as \"Partner Amount\", IFNULL(rs.principal_completed_derived,0) as \"Principal Received\", IFNULL(rs.interest_completed_derived,0) as \"Interest Received\", \"0\" as \"Overdue Charges Received\",IFNULL(rs.bounce_charges_completed_derived,0) as \"Bounce Charges Received\", IFNULL(rs.penalty_charges_completed_derived,0) as \"Penal Charges Received\", IFNULL(rs.fee_charges_completed_derived,0) as \"Foreclosure Charges Received\", \"0\" as \"Prepayment Charges Received\", \"0\" as \"Adhoc Charges Received\", (IFNULL(rs.principal_completed_derived,0) + IFNULL(rs.interest_completed_derived,0) + IFNULL(rs.penalty_charges_completed_derived,0) + IFNULL(rs.fee_charges_completed_derived,0)) as \"Amount Received\", IFNULL(rs.self_principal_completed_derived,0) as \"Self Principal Received\", IFNULL(rs.self_interest_completed_derived,0) as \"Self Interest Received\", \"0\" as \"Self Overdue Charges Received\",IFNULL(rs.self_bounce_charges_completed_derived,0) as \"Self Bounce Charges Received\",IFNULL(rs.self_penalty_charges_completed_derived,0) as \"Self Penal Charges Received\", IFNULL(rs.self_fee_charges_completed_derived,0) as \"Self Foreclosure Charges Received\", \"0\" as \"Self Prepayment Charges Received\", \"0\" as \"Self Adhoc Charges Received\", (IFNULL(rs.self_principal_completed_derived,0) + IFNULL(rs.self_interest_completed_derived,0) + IFNULL(rs.self_penalty_charges_completed_derived,0) + IFNULL(rs.self_fee_charges_completed_derived,0)) as \"Self Amount Received\", IFNULL(rs.partner_principal_completed_derived,0) as \"Partner Principal Received\", IFNULL(rs.partner_interest_completed_derived,0) as \"Partner Interest Received\", \"0\" as \"Partner Overdue Charges Received\",IFNULL(rs.partner_bounce_charges_completed_derived,0) as \"Partner Bounce Charges Received\", IFNULL(rs.partner_penalty_charges_completed_derived,0) as \"Partner Penal Charges Received\", IFNULL(rs.partner_fee_charges_completed_derived,0) as \"Partner Foreclosure Charges Received\", \"0\" as \"Partner Prepayment Charges Received\", \"0\" as \"Partner Adhoc Charges Received\",	(IFNULL(rs.partner_principal_completed_derived,0) + IFNULL(rs.partner_interest_completed_derived,0) + IFNULL(rs.partner_penalty_charges_completed_derived,0) + IFNULL(rs.partner_fee_charges_completed_derived,0)) as \"Partner  Amount Received\",  (CASE rs.completed_derived WHEN 0 THEN (CASE WHEN ((IFNULL(rs.principal_completed_derived,0) + IFNULL(rs.interest_completed_derived,0) + IFNULL(rs.penalty_charges_completed_derived,0)) = 0) THEN \"Not Paid\"  WHEN ((IFNULL(rs.principal_completed_derived,0) + IFNULL(rs.interest_completed_derived,0) + IFNULL(rs.penalty_charges_completed_derived,0)) > 0) THEN \"Partially Paid\" END)  WHEN 1 THEN \"Paid\"  END) as \"Status\", (SELECT txn.transaction_date FROM m_loan_transaction txn  WHERE txn.id = (SELECT max(txnrs.loan_transaction_id) FROM m_loan_transaction_repayment_schedule_mapping txnrs  WHERE txnrs.loan_repayment_schedule_id in (SELECT id FROM m_loan_repayment_schedule WHERE installment = rs.installment AND loan_id = rs.loan_id))) as \"Date of Collection\", 	(CASE rs.completed_derived  WHEN 1 THEN date_format(rs.obligations_met_on_date,\"%d-%m-%Y\")  END) as \"EMI Settlement Date\" FROM m_loan lo LEFT JOIN m_partner pr ON pr.id = lo.partner_id LEFT JOIN m_loan_repayment_schedule rs ON rs.loan_id = lo.id WHERE pr.id = ${partnerId} AND lo.loan_status_id NOT IN (100,200,500) AND rs.duedate BETWEEN STR_TO_DATE(\"${startDate}\", \"%d %M %Y\") AND STR_TO_DATE(\"${endDate}\", \"%d %M %Y\") GROUP BY rs.id ORDER BY lo.id, rs.installment' WHERE `report_name` like '%Demand vs Collections%';
-- changeset zeus :27
-- comment added to add collection inflow and appropriation Report adding bounce column
UPDATE stretchy_report SET report_sql = 'select lo.partner_id as \"Partner ID\",\r pr.partner_name as \"Partner Name\",\r cl.display_name as \"End Borrower Name\",\r lo.external_id as \"External ID\",\r lo.account_no as \"Loan Account No\",\r txn.amount as \"Paid Amount\",\r txn.transaction_date as \"Paid Date\",\r txn.partner_transfer_utr as \"Partner Transfer UTR\",\r txn.partner_transfer_date as \"Partner Transfer Date\",\r   txn.receipt_reference_number as\"Receipt Reference Number\",\r   cv.code_value as \"Repayment Mode\"\r   from m_loan_transaction txn\r   left join m_loan lo on  lo.id  = txn.loan_id \r   left join m_client cl on cl.id = lo.client_id\r   left join m_partner pr on pr.id = lo.partner_id\r   left join m_code_value cv on cv.id = txn.repayment_mode_cv_id\r   where pr.id = ${partnerId} and\r   txn.transaction_type_enum not in (1) and\r   txn.transaction_date between STR_TO_DATE(\"${startDate}\", \"%d %M %Y\") and STR_TO_DATE(\"${endDate}\", \"%d %M %Y\") and \r   txn.transaction_type_enum in (2,6,11,17,25) and txn.createdon_userid not in (2) and txn.amount>0 and\r   txn.is_reversed = 0 \r   group by txn.id\r   order by txn.transaction_date,lo.account_no' WHERE `report_name` like '%Collection Inflow Report%';
UPDATE stretchy_report SET report_sql = 'select lo.partner_id as \"Partner ID\",\r lo.id as \"loan_id\",\r pr.partner_name as \"Partner Name\",\r cl.display_name as \"End Borrower Name\",\r lo.external_id as \"External ID\",\r lo.account_no as \"Loan Account No\", \r rs.installment as \"Installment Number\",\r txn.transaction_date as \"Paid Date\",\r ifnull(txnrs.interest_portion_derived,0) as \"Interest Paid\",\r ifnull(txnrs.principal_portion_derived,0) as \"Principal Paid\",\r ifnull(txnrs.fee_charges_portion_derived,0) as \"Fees Paid\",\r ifnull(txnrs.bounce_charges_portion_derived,0) as \"Bounce Paid\",\r ifnull(txnrs.advance_amount,0) as \"Advance Amount\",\r if(rs.obligations_met_on_date between STR_TO_DATE(\"${startDate}\", \"%d %M %Y\") and STR_TO_DATE(\"${endDate}\", \"%d %M %Y\"), \"PAID\",\"PARTIALLY PAID\") as \"status\"\r from  m_loan_transaction_repayment_schedule_mapping txnrs\r left join m_loan_transaction txn on txn.id = txnrs.loan_transaction_id\r left join m_loan_repayment_schedule rs on rs.id= txnrs.loan_repayment_schedule_id\r left join m_loan lo on  lo.id  = txn.loan_id \r left join m_client cl on cl.id = lo.client_id\r left join m_partner pr on pr.id = lo.partner_id\r where  pr.id = ${partnerId}  and txn.is_reversed = 0 and\r txnrs.loan_transaction_id in (select id from m_loan_transaction txn where \r txn.transaction_date between STR_TO_DATE(\"${startDate}\", \"%d %M %Y\") and STR_TO_DATE(\"${endDate}\", \"%d %M %Y\"))\r group by txnrs.id\r order by txn.transaction_date,txn.loan_id,rs.installment' WHERE `report_name` like '%Collection Appropriation Report%';
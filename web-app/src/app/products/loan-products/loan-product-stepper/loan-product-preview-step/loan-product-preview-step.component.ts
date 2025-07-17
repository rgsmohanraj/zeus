import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'mifosx-loan-product-preview-step',
  templateUrl: './loan-product-preview-step.component.html',
  styleUrls: ['./loan-product-preview-step.component.scss']
})
export class LoanProductPreviewStepComponent implements OnInit {

  @Input() loanProductsTemplate: any;
  @Input() accountingRuleData: any;
  @Input() loanProduct: any;
  @Output() submit = new EventEmitter();

  colendingFeesDisplayedColumns: string[] = ['colendingFees', 'selfFees', 'partnerFees'];
  colendingChargesDisplayedColumns: string[] = ['colendingCharge', 'selfCharge', 'partnerCharge'];
  variationsDisplayedColumns: string[] = ['valueConditionType', 'borrowerCycleNumber', 'minValue', 'defaultValue', 'maxValue'];
  chargesDisplayedColumns: string[] = ['name', 'chargeCalculationType', 'amount', 'chargeTimeType'];
  paymentFundSourceDisplayedColumns: string[] = ['paymentTypeId', 'fundSourceAccountId'];
  feesPenaltyIncomeDisplayedColumns: string[] = ['chargeId', 'incomeAccountId'];
  gstColumns:string[] = ['gstLiabilityByVcpl','gstLiabilityByPartner'];

  constructor() {
    console.log(this.loanProductsTemplate,"loanProductsTemplate");
    }

  ngOnInit() {
  console.log(this.loanProduct,"loanProduct");
  }

}

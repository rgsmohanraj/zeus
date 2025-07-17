import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'mifosx-view-loan-product',
  templateUrl: './view-loan-product.component.html',
  styleUrls: ['./view-loan-product.component.scss']
})
export class ViewLoanProductComponent implements OnInit {

  loanProduct: any;
  loanServiceData:any;
  loanServicerFeeConfig :any;
  sfCharges:[];

  colendingFeesDisplayedColumns: string[] = ['colendingFees', 'selfFees', 'partnerFees'];
  colendingChargesDisplayedColumns: string[] = ['colendingCharge', 'selfCharge', 'partnerCharge'];
  servicerFeeChargeColumns: string[] =['servicerFeeChargesRatio','servicerFee','selfShare','partnerShare','gstLoss','gstLossPercentage','active'];
  colendingOverDueChargesDisplayedColumns:string[]=['overDueCharges', 'selfOverDue', 'partnerOverDue'];
  variationsDisplayedColumns: string[] = ['valueConditionType', 'borrowerCycleNumber', 'minValue', 'defaultValue', 'maxValue'];
  chargesDisplayedColumns: string[] = ['name', 'chargeCalculationType', 'amount', 'chargeTimeType'];
  paymentFundSourceDisplayedColumns: string[] = ['paymentTypeId', 'fundSourceAccountId'];
  feesPenaltyIncomeDisplayedColumns: string[] = ['chargeId', 'incomeAccountId'];

  constructor(private route: ActivatedRoute) {
    this.route.data.subscribe((data: { loanProduct: any,loanServiceData :any}) => {
      this.loanProduct = data.loanProduct;
      console.log("loanProduct",this.loanProduct);
      this.loanServiceData = data.loanServiceData;
      console.log("loanServiceData",this.loanServiceData);
      this.loanServicerFeeConfig = data.loanServiceData.servicerFeeChargeData;
       console.log("loanServicerFeeConfig",this.loanServicerFeeConfig);
    });
  }

  ngOnInit() {
    this.loanProduct.allowAttributeConfiguration = Object.values(this.loanProduct.allowAttributeOverrides).some((attribute: boolean) => attribute);
//     this.loanServiceStep.allowAttributeConfiguration = Object.values(this.loanServiceStep.allowAttributeOverrides).some((attribute: boolean) => attribute);

  }

}

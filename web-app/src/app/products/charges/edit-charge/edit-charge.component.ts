/** Angular Imports */
import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

/** Custom Services */
import { ProductsService } from 'app/products/products.service';
import { SettingsService } from 'app/settings/settings.service';
import { NotificationService } from '../../../notification.service';

/**
 * Edit Charge component.
 */
@Component({
  selector: 'mifosx-edit-charge',
  templateUrl: './edit-charge.component.html',
  styleUrls: ['./edit-charge.component.scss']
})
export class EditChargeComponent implements OnInit {

  /** Selected Data. */
  chargeData: any;
  /** Charge form. */
  chargeForm: UntypedFormGroup;
  chargeForms: UntypedFormGroup;

  gstData: any;
  /** Select Income. */
  selectedIncome: any;

  //   chargeTypeData:any;
  /** Select Time Type. */
  selectedTime: any;
  /** Select Currency Type. */
  selectedCurrency: any;
  /** Select Calculation Type. */
  selectedCalculation: any;
  /** Charge Time Type options. */
  chargeTimeTypeOptions: any;
  /** Charge Calculation Type options. */
  chargeCalculationTypeOptions: any;
  /** Show Penalty. */
  showPenalty = true;
  /** Add Fee Frequency. */
  addFeeFrequency = true;
  /** Show GL Accounts. */
  showGLAccount = false;
  /** Charge Payment Mode. */
  chargePaymentMode = false;
  /** Show Fee Options. */
  showFeeOptions = false;

  feesTypeShow = false;

  chargeTypeOptions: any;

  /**
   * Retrieves the charge data from `resolve`.
   * @param {ProductsService} productsService Products Service.
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {ActivatedRoute} route Activated Route.
   * @param {Router} router Router for navigation.
   * @param {SettingsService} settingsService Settings Service
   */
  constructor(private productsService: ProductsService,
    private formBuilder: UntypedFormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private settingsService: SettingsService,
    private notifyService: NotificationService) {
    this.route.data.subscribe((data: { chargesTemplate: any }) => {
      this.chargeData = data.chargesTemplate;
    });
  }

  ngOnInit() {
    this.chargeTypeOptions = this.chargeData.typeOption
    this.editChargeForm();
    //   this.refreshEditChargeForm();

    //   if(this.chargeData.chargeTypeSelected.id!=16){
    //     this.feesTypeShow=false;
    //     }
    //     else {
    //     this.feesTypeShow=true;
    //     }

  }

  refreshEditChargeForm() {
    this.chargeForms = this.formBuilder.group({
    'isGstSlabEnabled':[false],
      'gstSlabLimitApplyFor': [''],
      'gstSlabLimitOperator': [''],
      'gstSlabLimitValue': [''],
    });
  }

  /**
   * Edit Charge form.
   */
  editChargeForm() {
    this.chargeForm = this.formBuilder.group({
      'name': [this.chargeData.name, Validators.required],
      'chargeAppliesTo': [{ value: this.chargeData.chargeAppliesTo.id, disabled: true }, Validators.required],
      'currencyCode': [this.chargeData.currency.code, Validators.required],
      'feesChargeTypeSelected': [this.chargeData.feesChargeTypeSelected && this.chargeData.feesChargeTypeSelected.id],
      'amount': [this.chargeData.amount, Validators.required],
      'minChargeAmount': [this.chargeData.minChargeAmount],
      'maxChargeAmount': [this.chargeData.maxChargeAmount],
      'active': [this.chargeData.active],
      'enableGstChargesSelected': [this.chargeData.enableGstChargesSelected],
      'penalty': [this.chargeData.penalty],
      'gstOption': [this.chargeData.gstSelected.id, Validators.required],
      'chargeTimeType': [this.chargeData.chargeTimeType.id, Validators.required],
      'chargeCalculationType': [this.chargeData.chargeCalculationType.id, Validators.required],
      'typeSelected': [this.chargeData.typeSelected && this.chargeData.typeSelected.id],
      'chargeDecimal': [this.chargeData.chargeDecimal],
      'chargeRoundingMode': [this.chargeData.chargeRoundingMode],
      'chargeDecimalRegex': [this.chargeData.chargeDecimalRegex],
      'gstDecimal': [this.chargeData.gstDecimal],
      'gstRoundingMode': [this.chargeData.gstRoundingMode],
      'gstDecimalRegex': [this.chargeData.gstDecimalRegex],
      'isGstSlabEnabled': [this.chargeData.isGstSlabEnabledSelected],
      'gstSlabLimitApplyFor': [this.chargeData.gstSlabLimitApplyForSelected && this.chargeData.gstSlabLimitApplyForSelected.id],
      'gstSlabLimitOperator': [this.chargeData.gstSlabLimitOperatorSelected.value && this.chargeData.gstSlabLimitOperatorSelected.id],
      'gstSlabLimitValue': [this.chargeData.gstSlabLimitValue],
      'isDefaultLoanCharge':[this.chargeData.isDefaultLoanCharge]

    });

    switch (this.chargeData.chargeAppliesTo.value) {
      case 'Loan': {
        this.chargeTimeTypeOptions = this.chargeData.loanChargeTimeTypeOptions;
        this.chargeCalculationTypeOptions = this.chargeData.loanChargeCalculationTypeOptions;
        this.addFeeFrequency = true;
        this.chargePaymentMode = true;
        this.chargeForm.addControl('chargePaymentMode', this.formBuilder.control(this.chargeData.chargePaymentMode.id, Validators.required));
        break;
      }
      case 'Savings': {
        this.chargeTimeTypeOptions = this.chargeData.savingsChargeTimeTypeOptions;
        this.chargeCalculationTypeOptions = this.chargeData.savingsChargeCalculationTypeOptions;
        this.addFeeFrequency = false;
        break;
      }
      case 'Shares': {
        this.chargeTimeTypeOptions = this.chargeData.shareChargeTimeTypeOptions;
        this.chargeCalculationTypeOptions = this.chargeData.shareChargeCalculationTypeOptions;
        this.addFeeFrequency = false;
        this.showGLAccount = false;
        this.showPenalty = false;
        break;
      }
      default: {
        this.chargeCalculationTypeOptions = this.chargeData.clientChargeCalculationTypeOptions;
        this.chargeTimeTypeOptions = this.chargeData.clientChargeTimeTypeOptions;
        this.showGLAccount = true;
        this.addFeeFrequency = false;
        this.chargeForm.addControl('incomeAccountId', this.formBuilder.control(this.chargeData.incomeOrLiabilityAccount.id, Validators.required));
        break;
      }
    }

    if (this.chargeData.taxGroup) {
      this.chargeForm.addControl('taxGroupId', this.formBuilder.control({ value: this.chargeData.taxGroup.id, disabled: true }, Validators.required));
    } else {
      this.chargeForm.addControl('taxGroupId', this.formBuilder.control({ value: '?', disabled: true }));
    }
  }

  /**
   * Get Add Fee Frequency value.
   */
  getFeeFrequency(isChecked: boolean) {
    this.showFeeOptions = isChecked;
    if (isChecked) {
      this.chargeForm.addControl('feeInterval', this.formBuilder.control('', Validators.required));
      this.chargeForm.addControl('feeFrequency', this.formBuilder.control('', Validators.required));
      this.chargeForm.addControl('penaltyInterestDaysInYear', this.formBuilder.control('', Validators.required));
    } else {
      this.chargeForm.removeControl('feeInterval');
      this.chargeForm.removeControl('feeFrequency');
      this.chargeForm.removeControl('penaltyInterestDaysInYear');
    }
  }

  /**
   * Submits Edit Charge form.
   */
  submit() {
    const charges = this.chargeForm.value;
    charges.locale = this.settingsService.language.code;
    charges.chargePaymentMode = this.chargeData.chargePaymentMode.id;
    this.productsService.updateCharge(this.chargeData.id.toString(), charges)
      .subscribe((response: any) => {
        this.router.navigate(['../'], { relativeTo: this.route });
      }, error => {
        for (let i = 0; i < error.error.errors.length; i++) {
          this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
        }
      });
  }


  //   typeEvent(e){
  //     console.log(e);
  //     let feesTyp=e.value;
  //     if(feesTyp==16){
  //     this.feesTypeShow=true;
  //     }
  //     else{
  //     this.feesTypeShow=false;
  //     }
  //     }


  //  refreshData() {
  //
  //     'gstSlabLimitApplyFor':[''],
  //       'gstSlabLimitOperator':[''],
  //       'gstSlabLimitValue':[''],
  //
  //   }
  //  refreshData() {
  //     if (!this.isChecked) {
  //       // Refresh data when the checkbox is unchecked
  //       this.refreshData();
  //     }
  //   }

  refreshData(isChecked) {
  console.log(isChecked,"isChecked");
    if(!isChecked.checked){
     console.log(" this..chargeForms ", this.chargeForms );
      this.refreshEditChargeForm();
      console.log(" this..chargeForms ", this.chargeForms );
      console.log(" this.refreshEditChargeForm ", this.refreshEditChargeForm );
    }
  }

}

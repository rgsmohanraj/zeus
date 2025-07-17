/** Angular Imports */
import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators } from '@angular/forms';
import { Dates } from 'app/core/utils/dates';

/** Custom Services */
import { SettingsService } from 'app/settings/settings.service';

@Component({
  selector: 'mifosx-loan-product-details-step',
  templateUrl: './loan-product-details-step.component.html',
  styleUrls: ['./loan-product-details-step.component.scss']
})
export class LoanProductDetailsStepComponent implements OnInit {

  @Input() loanProductsTemplate: any;

  loanProductDetailsForm: UntypedFormGroup;

  fundData: any;
  prodClsList:any;
  prodTypList:any;
  loanProductClassData:any;
  loanProductTypeData:any;
  assetClassData:any;

  minDate = new Date(2000, 0, 1);
  maxDate = new Date(new Date().setFullYear(new Date().getFullYear() + 10));

  /**
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {Dates} dateUtils Date Utils.
   * @param {SettingsService} settingsService Settings Service.
   */

  constructor(private formBuilder: UntypedFormBuilder,
              private dateUtils: Dates,
              private settingsService: SettingsService) {
    this.createLoanProductDetailsForm();
  }

  ngOnInit() {
  this.prodClsList = this.loanProductsTemplate.classOptions;
  this.prodTypList = this.loanProductsTemplate.typeOptions;
  this.fundData = this.loanProductsTemplate.fundOptions;
  this.assetClassData = this.loanProductsTemplate.assetClassOptions;
  this.loanProductClassData = this.loanProductsTemplate.loanProductClassOptions;
  this.loanProductTypeData = this.loanProductsTemplate.loanProductTypeOptions;

    this.loanProductDetailsForm.patchValue({
      'name': this.loanProductsTemplate.name,
      'shortName': this.loanProductsTemplate.shortName,
      'loanAccNoPreference': this.loanProductsTemplate.loanAccNoPreference,
//       'classId': this.loanProductsTemplate.classId,
//       'typeId': this.loanProductsTemplate.typeId,
      'description': this.loanProductsTemplate.description,
      'fundId': this.loanProductsTemplate.fundId,
      'startDate': this.loanProductsTemplate.startDate && new Date(this.loanProductsTemplate.startDate),
      'closeDate': this.loanProductsTemplate.closeDate && new Date(this.loanProductsTemplate.closeDate),
      'includeInBorrowerCycle': this.loanProductsTemplate.includeInBorrowerCycle,
      'loanProductClass':this.loanProductsTemplate.loanProductClass && this.loanProductsTemplate.loanProductClass.id,
      'loanProductType':this.loanProductsTemplate.loanProductType && this.loanProductsTemplate.loanProductType.id,
      'assetClass':this.loanProductsTemplate.assetClass && this.loanProductsTemplate.assetClass.id,


    });
  }

  createLoanProductDetailsForm() {
    this.loanProductDetailsForm = this.formBuilder.group({
      'name': ['', Validators.required],
      'shortName': ['', Validators.required],
      'loanAccNoPreference': ['', Validators.required],
//       'classId': ['', Validators.required],
//       'typeId': ['', Validators.required],
      'description': [''],
      'fundId': [''],
      'startDate': [''],
      'closeDate': [''],
      'includeInBorrowerCycle': [false],
      'loanProductClass': [''],
      'loanProductType': [''],
      'assetClass':['',Validators.required],

    });
  }

  get loanProductDetails() {
    const loanProductDetailsFormData = this.loanProductDetailsForm.value;
    const prevStartDate: Date = this.loanProductDetailsForm.value.startDate;
    const prevCloseDate: Date = this.loanProductDetailsForm.value.closeDate;
    const dateFormat = this.settingsService.dateFormat;

    if (loanProductDetailsFormData.startDate instanceof Date)
    {
      loanProductDetailsFormData.startDate = this.dateUtils.formatDate(prevStartDate, dateFormat) || '';
    }
    if (loanProductDetailsFormData.closeDate instanceof Date)
    {
      loanProductDetailsFormData.closeDate = this.dateUtils.formatDate(prevCloseDate, dateFormat) || '';
    }
    return loanProductDetailsFormData;
  }


}

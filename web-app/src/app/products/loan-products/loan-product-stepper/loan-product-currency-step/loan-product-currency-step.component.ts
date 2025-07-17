import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'mifosx-loan-product-currency-step',
  templateUrl: './loan-product-currency-step.component.html',
  styleUrls: ['./loan-product-currency-step.component.scss']
})
export class LoanProductCurrencyStepComponent implements OnInit {

  @Input() loanProductsTemplate: any;

  loanProductCurrencyForm: UntypedFormGroup;
 emiCalcEnum:any;
  currencyData: any;
  interestRateFrequencyTypeData: any;
  roundingModes : any;
  emimultiplesOf : any;
 daysInYearTypeData: any;
  daysInMonthTypeData: any;
   emiCalcEnumData: any;

  constructor(private formBuilder: UntypedFormBuilder) {
    this.createLoanProductCurrencyForm();
  }

  ngOnInit() {
    this.currencyData = this.loanProductsTemplate.currencyOptions;
     this.roundingModes= this.loanProductsTemplate.roundingModes;
      this.emimultiplesOf = this.loanProductsTemplate.emimultiplesOf;
      this.emiCalcEnumData = this.loanProductsTemplate.emiCalcEnum;
       this.daysInYearTypeData = this.loanProductsTemplate.daysInYearTypeOptions;
       this.daysInMonthTypeData = this.loanProductsTemplate.daysInMonthTypeOptions;

    this.loanProductCurrencyForm.patchValue({
       'currencyCode': this.loanProductsTemplate.currency.code || this.currencyData[0].code,
       'digitsAfterDecimal': this.loanProductsTemplate.emiDecimalSelected,
       'emiRoundingMode':this.loanProductsTemplate.emiRoundingModeSelected,
       'interestDecimal' : this.loanProductsTemplate.interestDecimalSelected,
       'emimultiples' : this.loanProductsTemplate.emiMultiplesOfSelected,
       'interestRoundingMode': this.loanProductsTemplate.interestRoundingModeSelected,
       'interestDecimalRegex':this.loanProductsTemplate.interestDecimalRegexSelected,
       'emiDecimalRegex' : this.loanProductsTemplate.emiDecimalRegexSelected,
       'pmtFormulaCalculation' : this.loanProductsTemplate.emiCalcSelected && this.loanProductsTemplate.emiCalcSelected.id,
       'pmtDaysInYearType' : this.loanProductsTemplate.emiDaysInYearSelected?.id,
       'pmtDaysInMonthType' : this.loanProductsTemplate.emiDaysInMonthSelected?.id,

//       'inMultiplesOf': this.loanProductsTemplate.currency.inMultiplesOf,
//       'installmentAmountInMultiplesOf': this.loanProductsTemplate.installmentAmountInMultiplesOf
    });
  }

  createLoanProductCurrencyForm() {
    this.loanProductCurrencyForm = this.formBuilder.group({
      'currencyCode': ['', Validators.required],
      'digitsAfterDecimal': ['', Validators.required],
      'emiRoundingMode':['',Validators.required],
      'emimultiples':['',Validators.required],
      'interestRoundingMode':['', Validators.required],
      'interestDecimal':['',Validators.required],
      'interestDecimalRegex':[''],
      'emiDecimalRegex':[''],
      'pmtFormulaCalculation':['', Validators.required],
      'pmtDaysInYearType':[''],
      'pmtDaysInMonthType':['']
//       'inMultiplesOf': [1, Validators.required],
//       'installmentAmountInMultiplesOf': ['', Validators.required]
    });
  }

  get loanProductCurrency() {
    return this.loanProductCurrencyForm.value;
  }

}

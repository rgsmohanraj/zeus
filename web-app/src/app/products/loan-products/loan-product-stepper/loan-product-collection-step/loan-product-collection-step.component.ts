import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Dates } from 'app/core/utils/dates';
import { SettingsService } from 'app/settings/settings.service';

@Component({
  selector: 'mifosx-loan-product-collection-step',
  templateUrl: './loan-product-collection-step.component.html',
  styleUrls: ['./loan-product-collection-step.component.scss']
})
export class LoanProductCollectionStepComponent implements OnInit {

  @Input() loanProductsTemplate: any;
  loanProductCollectionForm: FormGroup;
  advanceAppropriationData:any;
  foreclosurePosCalculationData:any;
  advanceAppropriationAgainstOnData: any;
  roundingModes :any;
    daysInYearTypeData: any;
    daysInMonthTypeData: any;
    coolingOffInterestAndChargeData :any;
    coolingOffInterestData:any;
    foreclosureMethodData :any;
    /**
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {Dates} dateUtils Date Utils.
   * @param {SettingsService} settingsService Settings Service.
   */


    constructor(private formBuilder: FormBuilder,
      private dateUtils: Dates,
      private settingsService: SettingsService) {
        this.createLoanProductCollectionForm();
      }

  ngOnInit() {

    this.advanceAppropriationData = this.loanProductsTemplate.advanceAppropriations;
    this.foreclosurePosCalculationData =this.loanProductsTemplate.foreclosurePosCalculation;
    this.advanceAppropriationAgainstOnData = this.loanProductsTemplate.advanceAppropriationAgainstOn;
     this.roundingModes = this.loanProductsTemplate.roundingModes;
       this.daysInYearTypeData = this.loanProductsTemplate.daysInYearTypeOptions;
         this.daysInMonthTypeData = this.loanProductsTemplate.daysInMonthTypeOptions;
        this.coolingOffInterestAndChargeData = this.loanProductsTemplate.coolingOffInterestAndChargeApplicability;
      this.coolingOffInterestData = this.loanProductsTemplate.coolingOffInterestLogicApplicability;
       this.foreclosureMethodData = this.loanProductsTemplate.foreclosureMethodType;


    this.loanProductCollectionForm.patchValue({

     'interestBenefitEnabled' : this.loanProductsTemplate.interestBenefitEnabled,
        'advanceAppropriation': this.loanProductsTemplate.advanceAppropriationSelected?.id,
      'enableEntryForAdvanceTransaction':this.loanProductsTemplate.advanceEntryEnabledSelected,
      'foreclosureOnDueDateInterest':this.loanProductsTemplate.foreclosureOnDueDateInterestSelected?.id,
      'foreclosureOnDueDateCharge':this.loanProductsTemplate.foreclosureOnDueDateChargeSelected?.id,
      'foreclosureOtherThanDueDateInterest':this.loanProductsTemplate.foreclosureOtherThanDueDateInterestSelected?.id,
       'foreclosureOtherThanDueDateCharge':this.loanProductsTemplate.foreclosureOtherThanDueDateChargeSelected?.id,
       'foreclosureOneMonthOverdueInterest':this.loanProductsTemplate.foreclosureOneMonthOverdueInterestSelected?.id,
       'foreclosureOneMonthOverdueCharge':this.loanProductsTemplate.foreclosureOneMonthOverdueChargeSelected?.id,
       'foreclosureShortPaidInterest':this.loanProductsTemplate.foreclosureShortPaidInterestSelected?.id,
      'foreclosureShortPaidInterestCharge':this.loanProductsTemplate.foreclosureShortPaidInterestChargeSelected?.id,
       'foreclosurePrincipalShortPaidInterest':this.loanProductsTemplate.foreclosurePrincipalShortPaidInterestSelected?.id,
       'foreclosurePrincipalShortPaidCharge':this.loanProductsTemplate.foreclosurePrincipalShortPaidChargeSelected?.id,
       'foreclosureTwoMonthsOverdueInterest':this.loanProductsTemplate.foreclosureTwoMonthsOverdueInterestSelected?.id,
       'foreclosureTwoMonthsOverdueCharge':this.loanProductsTemplate.foreclosureTwoMonthsOverdueChargeSelected?.id,
       'foreclosurePosAdvanceOnDueDate':this.loanProductsTemplate.foreclosurePosAdvanceOnDueDateSelected?.id,
       'foreclosureAdvanceOnDueDateInterest':this.loanProductsTemplate.foreclosureAdvanceOnDueDateInterestSelected?.id,
       'foreclosureAdvanceOnDueDateCharge':this.loanProductsTemplate.foreclosureAdvanceOnDueDateChargeSelected?.id,
       'foreclosurePosAdvanceOtherThanDueDate':this.loanProductsTemplate.foreclosurePosAdvanceOtherThanDueDateSelected?.id,
       'foreclosureAdvanceAfterDueDateInterest':this.loanProductsTemplate.foreclosureAdvanceAfterDueDateInterestSelected?.id,
       'foreclosureAdvanceAfterDueDateCharge':this.loanProductsTemplate.foreclosureAdvanceAfterDueDateChargeSelected?.id,
       'foreclosureBackdatedShortPaidInterest':this.loanProductsTemplate.foreclosureBackdatedShortPaidInterestSelected?.id,
       'foreclosureBackdatedShortPaidInterestCharge':this.loanProductsTemplate.foreclosureBackdatedShortPaidInterestChargeSelected?.id,
       'foreclosureBackdatedFullyPaidInterest':this.loanProductsTemplate.foreclosureBackdatedFullyPaidInterestSelected?.id,
       'foreclosureBackdatedFullyPaidInterestCharge':this.loanProductsTemplate.foreclosureBackdatedFullyPaidInterestChargeSelected?.id,
       'foreclosureBackdatedShortPaidPrincipalInterest':this.loanProductsTemplate.foreclosureBackdatedShortPaidPrincipalInterestSelected?.id,
       'foreclosureBackdatedShortPaidPrincipalCharge':this.loanProductsTemplate.foreclosureBackdatedShortPaidPrincipalChargeSelected?.id,
       'foreclosureBackdatedFullyPaidEmiInterest':this.loanProductsTemplate.foreclosureBackdatedFullyPaidEmiInterestSelected?.id,
       'foreclosureBackdatedFullyPaidEmiCharge':this.loanProductsTemplate.foreclosureBackdatedFullyPaidEmiChargeSelected?.id,
       'foreclosureBackdatedAdvanceInterest':this.loanProductsTemplate.foreclosureBackdatedAdvanceInterestSelected?.id,
       'foreclosureBackdatedAdvanceCharge': this.loanProductsTemplate.foreclosureBackdatedAdvanceChargeSelected && this.loanProductsTemplate.foreclosureBackdatedAdvanceChargeSelected.id,
       'advanceAppropriationAgainstOn': this.loanProductsTemplate.advanceAppropriationAgainstOnSelected && this.loanProductsTemplate.advanceAppropriationAgainstOnSelected.id,
       'coolingOffApplicability': this.loanProductsTemplate.coolingOffApplicability,
       'coolingOffThresholdDays': this.loanProductsTemplate.coolingOffThresholdDays,
       'coolingOffInterestAndChargeApplicability':this.loanProductsTemplate.coolingOffInterestAndChargeApplicabilitySelected && this.loanProductsTemplate.coolingOffInterestAndChargeApplicabilitySelected.id,
       'coolingOffInterestLogicApplicability':this.loanProductsTemplate.coolingOffInterestLogicApplicabilitySelected && this.loanProductsTemplate.coolingOffInterestLogicApplicabilitySelected.id,
       'coolingOffDaysInYear':this.loanProductsTemplate.coolingOffDaysInYearSelected?.id,
       'coolingOffRoundingMode':this.loanProductsTemplate.coolingOffRoundingModeSelected,
       'coolingOffRoundingDecimals':this.loanProductsTemplate.coolingOffRoundingDecimals,
       'foreClosureMethodType' :this.loanProductsTemplate.foreclosureMethodTypeSelected && this.loanProductsTemplate.foreclosureMethodTypeSelected.id
    });
  }

  createLoanProductCollectionForm() {

    this.loanProductCollectionForm = this.formBuilder.group({

       'interestBenefitEnabled': [false],
      'advanceAppropriation': ['',Validators.required],
      'enableEntryForAdvanceTransaction': [false],
      'foreClosureMethodType' : [''],
      'foreclosureOnDueDateInterest' : [''],
      'foreclosureOnDueDateCharge' : [''],
      'foreclosureOtherThanDueDateInterest' : [''],
      'foreclosureOtherThanDueDateCharge' : [''],
      'foreclosureOneMonthOverdueInterest' : [''],
      'foreclosureOneMonthOverdueCharge' : [''],
      'foreclosureShortPaidInterest' : [''],
      'foreclosureShortPaidInterestCharge' : [''],
      'foreclosurePrincipalShortPaidInterest' : [''],
      'foreclosurePrincipalShortPaidCharge' : [''],
      'foreclosureTwoMonthsOverdueInterest' : [''],
      'foreclosureTwoMonthsOverdueCharge' : [''],
      'foreclosurePosAdvanceOnDueDate' : [''],
      'foreclosureAdvanceOnDueDateInterest' : [''],
      'foreclosureAdvanceOnDueDateCharge' : [''],
      'foreclosurePosAdvanceOtherThanDueDate' : [''],
      'foreclosureAdvanceAfterDueDateInterest' : [''],
      'foreclosureAdvanceAfterDueDateCharge' : [''],
      'foreclosureBackdatedShortPaidInterest' : [''],
      'foreclosureBackdatedShortPaidInterestCharge' : [''],
      'foreclosureBackdatedFullyPaidInterest' : [''],
      'foreclosureBackdatedFullyPaidInterestCharge' : [''],
      'foreclosureBackdatedShortPaidPrincipalInterest' : [''],
      'foreclosureBackdatedShortPaidPrincipalCharge' : [''],
      'foreclosureBackdatedFullyPaidEmiInterest' : [''],
      'foreclosureBackdatedFullyPaidEmiCharge' : [''],
      'foreclosureBackdatedAdvanceInterest' : [''],
      'foreclosureBackdatedAdvanceCharge' : [''],
      'advanceAppropriationAgainstOn':[''],
     'coolingOffApplicability' :[false],
     'coolingOffThresholdDays' :[''],
     'coolingOffInterestAndChargeApplicability':[''],
     'coolingOffInterestLogicApplicability':[''],
     'coolingOffDaysInYear':[''],
     'coolingOffRoundingMode':[''],
     'coolingOffRoundingDecimals':['']

    });

  }

  get loanProductCollectionAppropriation()
  {
    return this.loanProductCollectionForm.value;

  }

}

import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators, UntypedFormArray, UntypedFormControl } from '@angular/forms';

@Component({
  selector: 'mifosx-loan-product-settings-step',
  templateUrl: './loan-product-settings-step.component.html',
  styleUrls: ['./loan-product-settings-step.component.scss']
})
export class LoanProductSettingsStepComponent implements OnInit {

  @Input() loanProductsTemplate: any;
  @Input() isLinkedToFloatingInterestRates: UntypedFormControl;

  loanProductSettingsForm: UntypedFormGroup;

  amortizationTypeData: any;
  interestTypeData: any;
  colendingChargeData:any;
  interestCalculationPeriodTypeData: any;
  brokenInterestCalculationPeriodTypeData= [{id: 'Actual', value: 'Actual'}];
  brokenInterestStrategyData: any;
  brokenInterestDaysInYearsData:any;
//   brokenInterestDaysInMonthData:any;
 // brokenInterestStrategyData= [{id: 'Nobroken', value: 'Nobroken'},{id: 'Disbursement', value: 'Disbursement'},{id: 'FirstRepayment', value: 'First Repayment'},{id: 'LastRepayment', value: 'Last Repayment'}];
  transactionProcessingStrategyData: any;
  daysInYearTypeData: any;
  daysInMonthTypeData: any;
  preClosureInterestCalculationStrategyData: any;
  rescheduleStrategyTypeData: any;
  interestRecalculationCompoundingTypeData: any;
  interestRecalculationFrequencyTypeData: any;
  interestRecalculationNthDayTypeData: any;
  interestRecalculationDayOfWeekTypeData: any;
  interestRecalculationOnDayTypeData: any;
  partnerData: any;
  chargeId:any;
  chargeNameHead:any;
  insuranceApplicabilityData:any;
  fldgLogicData:any;


  constructor(private formBuilder: UntypedFormBuilder) {
    this.createLoanProductSettingsForm();
    this.setConditionalControls();
  }
  chargeValue(event){
  this.chargeId=event.value;
  console.log(this.chargeId,"this.chargeId");
  for(let index in this.colendingChargeData){
  console.log(this.chargeId,"this.chargeId");
    if(this.chargeId==this.colendingChargeData[index].id){
      this.chargeNameHead=this.colendingChargeData[index].name;
    }
  }
  }
  ngOnInit() {
    this.isLinkedToFloatingInterestRates.valueChanges
      .subscribe((isLinkedToFloatingInterestRates: any) => {
        if (isLinkedToFloatingInterestRates) {
          this.loanProductSettingsForm.get('isInterestRecalculationEnabled').setValue(true);
        }
    });
    console.log(this.loanProductsTemplate,"sett.loanProductsTemplate");
    this.colendingChargeData = this.loanProductsTemplate.chargeOptions;
    this.amortizationTypeData = this.loanProductsTemplate.amortizationTypeOptions;
    this.brokenInterestStrategyData=this.loanProductsTemplate.brokenStrategy;
    this.brokenInterestDaysInYearsData=this.loanProductsTemplate.brokenDaysInYearOptions;
//     this.brokenInterestDaysInMonthData=this.loanProductsTemplate.brokenDaysInMonthOptions;
    this.interestTypeData = this.loanProductsTemplate.interestTypeOptions;
    this.interestCalculationPeriodTypeData = this.loanProductsTemplate.interestCalculationPeriodTypeOptions;
    this.transactionProcessingStrategyData = this.loanProductsTemplate.transactionProcessingStrategyOptions;
//     this.transactionProcessingStrategyData = this.loanProductsTemplate.transactionProcessingStrategyOptions.filter(strategy => strategy.id === 5 || strategy.id === 6);
    this.daysInYearTypeData = this.loanProductsTemplate.daysInYearTypeOptions;
    this.daysInMonthTypeData = this.loanProductsTemplate.daysInMonthTypeOptions;
    this.preClosureInterestCalculationStrategyData = this.loanProductsTemplate.preClosureInterestCalculationStrategyOptions;
    this.rescheduleStrategyTypeData = this.loanProductsTemplate.rescheduleStrategyTypeOptions;
    this.interestRecalculationCompoundingTypeData = this.loanProductsTemplate.interestRecalculationCompoundingTypeOptions;
    this.interestRecalculationFrequencyTypeData = this.loanProductsTemplate.interestRecalculationFrequencyTypeOptions;
    this.interestRecalculationNthDayTypeData = this.loanProductsTemplate.interestRecalculationNthDayTypeOptions;
    this.interestRecalculationNthDayTypeData.push({ 'id': -2, 'code': 'onDay', 'value': 'on day' });
    this.interestRecalculationDayOfWeekTypeData = this.loanProductsTemplate.interestRecalculationDayOfWeekTypeOptions;
    this.interestRecalculationOnDayTypeData = Array.from({ length: 28 }, (_, index) => index + 1);
    this.partnerData = this.loanProductsTemplate.partnerData;
    this.insuranceApplicabilityData = this.loanProductsTemplate.insuranceApplicabilityOptions;
    this.fldgLogicData = this.loanProductsTemplate.fldgLogicOptions;
//     this.disbursementData = this.loanProductsTemplate.disbursementOptions;
//     this.collectionData = this.loanProductsTemplate.collectionOptions;

    this.loanProductSettingsForm.patchValue({
      'amortizationType': this.loanProductsTemplate.amortizationType.id,
      'interestType': this.loanProductsTemplate.interestType.id,
      'isEqualAmortization': this.loanProductsTemplate.isEqualAmortization,
      'useDaysInMonthForLoanProvisioning': this.loanProductsTemplate.useDaysInMonthForLoanProvisioning,
      'divideByThirtyForPartialPeriod': this.loanProductsTemplate.divideByThirtyForPartialPeriod,
      'interestCalculationPeriodType': this.loanProductsTemplate.interestCalculationPeriodType.id,
      'brokenInterestCalculationPeriod': this.loanProductsTemplate.brokenInterestCalculationPeriod,
      'brokenInterestStrategy': this.loanProductsTemplate.brokenStrategyId.id,
      'brokenInterestDaysInYears': this.loanProductsTemplate.brokenInterestDaysInYearSelected.id,
      'allowPartialPeriodInterestCalcualtion': this.loanProductsTemplate.allowPartialPeriodInterestCalcualtion,
      'transactionProcessingStrategyId': this.loanProductsTemplate.transactionProcessingStrategyId || this.transactionProcessingStrategyData[0].id,
      'graceOnPrincipalPayment': this.loanProductsTemplate.graceOnPrincipalPayment,
      'repaymentStrategyForNpaId': this.loanProductsTemplate.repaymentStrategyForNpaId,
      'loanForeclosureStrategy': this.loanProductsTemplate.loanForeclosureStrategy,
      'graceOnInterestPayment': this.loanProductsTemplate.graceOnInterestPayment,
      'graceOnInterestCharged': this.loanProductsTemplate.graceOnInterestCharged,
      'repaymentStrategyForNpa': this.loanProductsTemplate.repaymentStrategyForNpa,
      'inArrearsTolerance': this.loanProductsTemplate.inArrearsTolerance,
      'daysInYearType': this.loanProductsTemplate.daysInYearType.id,
      'daysInMonthType': this.loanProductsTemplate.daysInMonthType.id,
      'canDefineInstallmentAmount': this.loanProductsTemplate.canDefineInstallmentAmount,
      'graceOnArrearsAgeing': this.loanProductsTemplate.graceOnArrearsAgeing,
      'overdueDaysForNPA': this.loanProductsTemplate.overdueDaysForNPA,
      'accountMovesOutOfNPAOnlyOnArrearsCompletion': this.loanProductsTemplate.accountMovesOutOfNPAOnlyOnArrearsCompletion,
      'principalThresholdForLastInstallment': this.loanProductsTemplate.principalThresholdForLastInstallment,
      'allowVariableInstallments': this.loanProductsTemplate.allowVariableInstallments,
//       'enableColendingLoan': this.loanProductsTemplate.enableColendingLoan,
      'disallowExpectedDisbursements': this.loanProductsTemplate.disallowExpectedDisbursements,
      'minimumGap': this.loanProductsTemplate.minimumGap,
      'maximumGap': this.loanProductsTemplate.maximumGap,
//       'byPercentageSplit': this.loanProductsTemplate.byPercentageSplit,
      'trancheCutOffDate': this.loanProductsTemplate.trancheCutOffDate,
      'maximumTrancheCount': this.loanProductsTemplate.maximumTrancheCount,
      'maximumAllowedOutstandingBalance': this.loanProductsTemplate.maximumAllowedOutstandingBalance,
//       'principalShare': this.loanProductsTemplate.principalShare,
//       'feeShare': this.loanProductsTemplate.feeShare,
//       'penaltyShare': this.loanProductsTemplate.penaltyShare,
//       'overpaidShare': this.loanProductsTemplate.overpaidShare,
//       'selfPrincipalShare': this.loanProductsTemplate.selfPrincipalShare,
//       'selfFeeShare': this.loanProductsTemplate.selfFeeShare,
//       'selfPenaltyShare': this.loanProductsTemplate.selfPenaltyShare,
//       'selfOverpaidShares': this.loanProductsTemplate.selfOverpaidShares,
//       'selfInterestRate': this.loanProductsTemplate.selfInterestRate,
//       'partnerPrincipalShare': this.loanProductsTemplate.partnerPrincipalShare,
//       'partnerFeeShare': this.loanProductsTemplate.partnerFeeShare,
//       'partnerPenaltyShare': this.loanProductsTemplate.partnerPenaltyShare,
//       'partnerOverpaidShare': this.loanProductsTemplate.partnerOverpaidShare,
//       'partnerInterestRate': this.loanProductsTemplate.partnerInterestRate,
//       'partnerId': this.loanProductsTemplate.partnerId,
//       'enableChargeWiseBifacation': this.loanProductsTemplate.enableChargeWiseBifacation,
//       'colendingCharge': this.loanProductsTemplate.colendingCharge,
//       'selfCharge': this.loanProductsTemplate.selfCharge,
//       'partnerCharge': this.loanProductsTemplate.partnerCharge,
//       'interestRate': this.loanProductsTemplate.interestRate,
      'canUseForTopup': this.loanProductsTemplate.canUseForTopup,
      'isInterestRecalculationEnabled': this.loanProductsTemplate.isInterestRecalculationEnabled,
      'holdGuaranteeFunds': this.loanProductsTemplate.holdGuaranteeFunds,
      'multiDisburseLoan': this.loanProductsTemplate.multiDisburseLoan,
      'maxTrancheCount': this.loanProductsTemplate.maxTrancheCount,
      'outstandingLoanBalance': this.loanProductsTemplate.outstandingLoanBalance,
      'aumSlabRate': this.loanProductsTemplate.aumSlabRate,
      'fldgLogic': this.loanProductsTemplate.fldgLogic && this.loanProductsTemplate.fldgLogic.id,
//       'disbursement': this.loanProductsTemplate.disbursementId.id ,
//       'collection': this.loanProductsTemplate.collectionId.id,
      'monitoringTriggerPar30': this.loanProductsTemplate.monitoringTriggerPar30,
      'monitoringTriggerPar90': this.loanProductsTemplate.monitoringTriggerPar90,
      'insuranceApplicability':this.loanProductsTemplate.insuranceApplicability && this.loanProductsTemplate.insuranceApplicability.id,


    });

//     if (this.loanProductsTemplate.brokenInterestStrategy) {
//           this.loanProductSettingsForm.patchValue({
//                    'isBrokenNetOff': this.loanProductsTemplate.isBrokenNetOff,
//           });
//         }

    if (this.loanProductsTemplate.isInterestRecalculationEnabled) {
      this.loanProductSettingsForm.patchValue({
        'preClosureInterestCalculationStrategy': this.loanProductsTemplate.interestRecalculationData.preClosureInterestCalculationStrategy.id,
        'rescheduleStrategyMethod': this.loanProductsTemplate.interestRecalculationData.rescheduleStrategyType.id,
        'interestRecalculationCompoundingMethod': this.loanProductsTemplate.interestRecalculationData.interestRecalculationCompoundingType.id,
        'recalculationRestFrequencyType': this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyType.id,
        'isArrearsBasedOnOriginalSchedule': this.loanProductsTemplate.interestRecalculationData.isArrearsBasedOnOriginalSchedule,
        'recalculationCompoundingFrequencyType': this.loanProductsTemplate.interestRecalculationData.interestRecalculationCompoundingType.id && this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyType.id,
        'recalculationCompoundingFrequencyInterval': this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyInterval,
        'recalculationRestFrequencyInterval': this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyInterval,
        'recalculationRestFrequencyNthDayType': this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyType.id === 4 && this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyOnDay ?
          -2 : this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyNthDay && this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyNthDay.id,
        'recalculationCompoundingFrequencyNthDayType': this.loanProductsTemplate.interestRecalculationData.interestRecalculationCompoundingType.id && this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyType.id === 4
          && this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyOnDay ? -2 : this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyNthDay
          && this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyNthDay.id,
        'recalculationCompoundingFrequencyDayOfWeekType': this.loanProductsTemplate.interestRecalculationData.interestRecalculationCompoundingType.id && ((this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyType.id
          === 4 && !this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyOnDay) || this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyType.id === 3)
          && this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyWeekday && this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyWeekday.id,
        'recalculationRestFrequencyDayOfWeekType': ((this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyType.id === 4 && !this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyOnDay)
          || this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyType.id === 3) && this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyWeekday
          && this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyWeekday.id,
        'recalculationCompoundingFrequencyOnDayType': this.loanProductsTemplate.interestRecalculationData.recalculationCompoundingFrequencyOnDay,
        'recalculationRestFrequencyOnDayType': this.loanProductsTemplate.interestRecalculationData.recalculationRestFrequencyOnDay
      });
    }

    if (this.loanProductsTemplate.holdGuaranteeFunds) {
      this.loanProductSettingsForm.patchValue({
        'mandatoryGuarantee': this.loanProductsTemplate.productGuaranteeData.mandatoryGuarantee,
        'minimumGuaranteeFromOwnFunds': this.loanProductsTemplate.productGuaranteeData.minimumGuaranteeFromOwnFunds,
        'minimumGuaranteeFromGuarantor': this.loanProductsTemplate.productGuaranteeData.minimumGuaranteeFromGuarantor
      });
    }

    if (this.loanProductsTemplate.allowAttributeOverrides) {
      this.loanProductSettingsForm.patchValue({
        'allowAttributeConfiguration': Object.values(this.loanProductsTemplate.allowAttributeOverrides).some((attribute: boolean) => attribute),
        'allowAttributeOverrides': {
          'amortizationType': this.loanProductsTemplate.allowAttributeOverrides.amortizationType,
          'interestType': this.loanProductsTemplate.allowAttributeOverrides.interestType,
          'transactionProcessingStrategyId': this.loanProductsTemplate.allowAttributeOverrides.transactionProcessingStrategyId,
          'interestCalculationPeriodType': this.loanProductsTemplate.allowAttributeOverrides.interestCalculationPeriodType,
          'repaymentStrategyForNpa': this.loanProductsTemplate.allowAttributeOverrides.repaymentStrategyForNpa,
          'inArrearsTolerance': this.loanProductsTemplate.allowAttributeOverrides.inArrearsTolerance,
          'repaymentEvery': this.loanProductsTemplate.allowAttributeOverrides.repaymentEvery,
          'graceOnPrincipalAndInterestPayment': this.loanProductsTemplate.allowAttributeOverrides.graceOnPrincipalAndInterestPayment,
          'graceOnArrearsAgeing': this.loanProductsTemplate.allowAttributeOverrides.graceOnArrearsAgeing,
          'aumSlabRate': this.loanProductsTemplate.allowAttributeOverrides.aumSlabRate
        }
      });
    }
  }

  createLoanProductSettingsForm() {
    this.loanProductSettingsForm = this.formBuilder.group({
      'amortizationType': ['', Validators.required],
      'interestType': ['', Validators.required],
      'isEqualAmortization': [false],
      'useDaysInMonthForLoanProvisioning': [false],
      'divideByThirtyForPartialPeriod': [false],
      'interestCalculationPeriodType': ['', Validators.required],
      'brokenInterestCalculationPeriod': [''],
      'brokenInterestStrategy': ['',Validators.required],
      'brokenInterestDaysInYears':['',Validators.required],
      'transactionProcessingStrategyId': ['', Validators.required],
      'graceOnPrincipalPayment': [''],
      'repaymentStrategyForNpaId': [''],
      'loanForeclosureStrategy': [''],
//       'brokenInterestDaysInMonth': ['', Validators.required],
      'graceOnInterestPayment': [''],
      'graceOnInterestCharged': [''],
      'repaymentStrategyForNpa': [''],
      'inArrearsTolerance': [''],
      'daysInYearType': ['', Validators.required],
      'daysInMonthType': ['', Validators.required],
      'canDefineInstallmentAmount': [false],
      'graceOnArrearsAgeing': [''],
      'overdueDaysForNPA': [''],
      'accountMovesOutOfNPAOnlyOnArrearsCompletion': [false],
      'principalThresholdForLastInstallment': [''],
      'allowVariableInstallments': [false],
       'aumSlabRate': [''],
//       'enableColendingLoan': [false],
//       'partnerId': ['', Validators.required],
      'disallowExpectedDisbursements': [false],
      'canUseForTopup': [false],
      'isInterestRecalculationEnabled': [false],
      'holdGuaranteeFunds': [false],
      'multiDisburseLoan': [false],
      'fldgLogic': ['',Validators.required],
      'insuranceApplicability':[''],
//       'disbursement': ['',Validators.required],
//       'collection': ['',Validators.required],
      'monitoringTriggerPar30': [''],
      'monitoringTriggerPar90': [''],
      'allowAttributeConfiguration': [true],
      'allowAttributeOverrides': this.formBuilder.group({
      'amortizationType': [true],
      'interestType': [true],
      'transactionProcessingStrategyId': [true],
      'interestCalculationPeriodType': [true],
      'repaymentStrategyForNpa': [true],
      'inArrearsTolerance': [true],
      'repaymentEvery': [true],
      'graceOnPrincipalAndInterestPayment': [true],
       'graceOnArrearsAgeing': [true]
      }),

    });
  }

createselectChargeForm(): UntypedFormGroup {
    return this.formBuilder.group({
      'colendingCharge': [''],
      'selfCharge': [100],
      'partnerCharge': [''],
    });
  }
  get selectCharge(): UntypedFormArray {
      return this.loanProductSettingsForm.get('selectCharge') as UntypedFormArray;
    }
      removeSelectChargeArr(affectedSelectChargeFormArray: UntypedFormArray, index: number) {
        affectedSelectChargeFormArray.removeAt(index);
      }
      addSelectChargeArr(affectedSelectChargeFormArray: UntypedFormArray) {
          affectedSelectChargeFormArray.push(this.createselectChargeForm());
        }
  setConditionalControls() {
    const allowAttributeOverrides = this.loanProductSettingsForm.get('allowAttributeOverrides');

    this.loanProductSettingsForm.get('interestCalculationPeriodType').valueChanges
      .subscribe((interestCalculationPeriodType: any) => {
        if (interestCalculationPeriodType === 1) {
          this.loanProductSettingsForm.addControl('allowPartialPeriodInterestCalcualtion', new UntypedFormControl(false));
        } else {
          this.loanProductSettingsForm.removeControl('allowPartialPeriodInterestCalcualtion');
        }
      });



//       this.loanProductSettingsForm.get('brokenInterestStrategy').valueChanges
//             .subscribe((brokenInterestStrategy: any) => {
//               if (brokenInterestStrategy == '1') {
//                 this.loanProductSettingsForm.addControl('isBrokenNetOff', new FormControl(false));
//               } else {
//                 this.loanProductSettingsForm.removeControl('isBrokenNetOff');
//               }
//             });

    this.loanProductSettingsForm.get('allowVariableInstallments').valueChanges
      .subscribe((allowVariableInstallments: any) => {
        if (allowVariableInstallments) {
          this.loanProductSettingsForm.addControl('minimumGap', new UntypedFormControl('', Validators.required));
          this.loanProductSettingsForm.addControl('maximumGap', new UntypedFormControl(''));
        } else {
          this.loanProductSettingsForm.removeControl('minimumGap');
          this.loanProductSettingsForm.removeControl('maximumGap');
        }
      });


//       this.loanProductSettingsForm.get('enableColendingLoan').valueChanges
//             .subscribe((enableColendingLoan: any) => {
//               if (enableColendingLoan) {
//                 this.loanProductSettingsForm.addControl('byPercentageSplit', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('partnerId', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('principalShare', new FormControl(100));
//                 this.loanProductSettingsForm.addControl('feeShare', new FormControl(100));
//                 this.loanProductSettingsForm.addControl('penaltyShare', new FormControl(100));
//                 this.loanProductSettingsForm.addControl('overpaidShare', new FormControl(100));
//                 this.loanProductSettingsForm.addControl('interestRate', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('selfPrincipalShare', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('selfFeeShare', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('selfPenaltyShare', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('selfOverpaidShares', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('selfInterestRate', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('partnerPrincipalShare', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('partnerFeeShare', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('partnerPenaltyShare', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('partnerOverpaidShare', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('partnerInterestRate', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('enableChargeWiseBifacation', new FormControl(''));
// //                 this.loanProductSettingsForm.addControl('colendingCharge', new FormControl(''));
// //                 this.loanProductSettingsForm.addControl('selfCharge', new FormControl({value : 100-this.loanProductSettingsForm.value.partnerCharge,disabled: true}));
// //                 this.loanProductSettingsForm.addControl('partnerCharge', new FormControl(''));
//                 this.loanProductSettingsForm.addControl('selectCharge', this.formBuilder.array([this.createselectChargeForm()]));
//                 console.log(this.loanProductSettingsForm.value.selectCharge,"createselectChargeForm");
//               } else {
//                 this.loanProductSettingsForm.removeControl('byPercentageSplit');
//                 this.loanProductSettingsForm.removeControl('partnerId');
//                 this.loanProductSettingsForm.removeControl('principalShare');
//                 this.loanProductSettingsForm.removeControl('feeShare');
//                 this.loanProductSettingsForm.removeControl('penaltyShare');
//                 this.loanProductSettingsForm.removeControl('overpaidShare');
//                 this.loanProductSettingsForm.removeControl('interestRate');
//                 this.loanProductSettingsForm.removeControl('selfPrincipalShare');
//                 this.loanProductSettingsForm.removeControl('selfFeeShare');
//                 this.loanProductSettingsForm.removeControl('selfPenaltyShare');
//                 this.loanProductSettingsForm.removeControl('selfOverpaidShares');
//                 this.loanProductSettingsForm.removeControl('selfInterestRate');
//                 this.loanProductSettingsForm.removeControl('partnerPrincipalShare');
//                 this.loanProductSettingsForm.removeControl('partnerFeeShare');
//                 this.loanProductSettingsForm.removeControl('partnerPenaltyShare');
//                 this.loanProductSettingsForm.removeControl('partnerOverpaidShare');
//                 this.loanProductSettingsForm.removeControl('partnerInterestRate');
//                 this.loanProductSettingsForm.removeControl('enableChargeWiseBifacation');
// //                 this.loanProductSettingsForm.removeControl('colendingCharge');
//                 this.loanProductSettingsForm.removeControl('selectCharge');
// //                 this.loanProductSettingsForm.removeControl('selfCharge');
// //                 this.loanProductSettingsForm.removeControl('partnerCharge');
//               }
//             });



    this.loanProductSettingsForm.get('isInterestRecalculationEnabled').valueChanges
      .subscribe((isInterestRecalculationEnabled: any) => {
        if (isInterestRecalculationEnabled) {
          this.loanProductSettingsForm.addControl('preClosureInterestCalculationStrategy', new UntypedFormControl(this.preClosureInterestCalculationStrategyData[0].id, Validators.required));
          this.loanProductSettingsForm.addControl('rescheduleStrategyMethod', new UntypedFormControl(this.rescheduleStrategyTypeData[0].id, Validators.required));
          this.loanProductSettingsForm.addControl('interestRecalculationCompoundingMethod', new UntypedFormControl(this.interestRecalculationCompoundingTypeData[0].id, Validators.required));
          this.loanProductSettingsForm.addControl('recalculationRestFrequencyType', new UntypedFormControl(this.interestRecalculationFrequencyTypeData[0].id, Validators.required));
          this.loanProductSettingsForm.addControl('isArrearsBasedOnOriginalSchedule', new UntypedFormControl(''));

          this.loanProductSettingsForm.get('interestRecalculationCompoundingMethod').valueChanges
            .subscribe((interestRecalculationCompoundingMethod: any) => {
              if (interestRecalculationCompoundingMethod !== 0) {
                this.loanProductSettingsForm.addControl('recalculationCompoundingFrequencyType', new UntypedFormControl(this.interestRecalculationFrequencyTypeData[0].id, Validators.required));

                this.loanProductSettingsForm.get('recalculationCompoundingFrequencyType').valueChanges
                  .subscribe((recalculationCompoundingFrequencyType: any) => {
                    if (recalculationCompoundingFrequencyType !== 1) {
                      this.loanProductSettingsForm.addControl('recalculationCompoundingFrequencyInterval', new UntypedFormControl('', Validators.required));
                    } else {
                      this.loanProductSettingsForm.removeControl('recalculationCompoundingFrequencyInterval');
                    }

                    if (recalculationCompoundingFrequencyType === 3) {
                      this.loanProductSettingsForm.addControl('recalculationCompoundingFrequencyDayOfWeekType', new UntypedFormControl(''));
                      this.loanProductSettingsForm.removeControl('recalculationCompoundingFrequencyNthDayType');
                      this.loanProductSettingsForm.removeControl('recalculationCompoundingFrequencyOnDayType');
                    } else if (recalculationCompoundingFrequencyType === 4) {
                      this.loanProductSettingsForm.addControl('recalculationCompoundingFrequencyNthDayType', new UntypedFormControl(''));
                      this.loanProductSettingsForm.addControl('recalculationCompoundingFrequencyDayOfWeekType', new UntypedFormControl(''));

                      this.loanProductSettingsForm.get('recalculationCompoundingFrequencyNthDayType').valueChanges
                        .subscribe((recalculationCompoundingFrequencyNthDayType: any) => {
                          if (recalculationCompoundingFrequencyNthDayType === -2) {
                            this.loanProductSettingsForm.addControl('recalculationCompoundingFrequencyOnDayType', new UntypedFormControl(''));
                            this.loanProductSettingsForm.removeControl('recalculationCompoundingFrequencyDayOfWeekType');
                          } else {
                            this.loanProductSettingsForm.addControl('recalculationCompoundingFrequencyDayOfWeekType', new UntypedFormControl(''));
                            this.loanProductSettingsForm.removeControl('recalculationCompoundingFrequencyOnDayType');
                          }
                        });
                    } else {
                      this.loanProductSettingsForm.removeControl('recalculationCompoundingFrequencyNthDayType');
                      this.loanProductSettingsForm.removeControl('recalculationCompoundingFrequencyDayOfWeekType');
                      this.loanProductSettingsForm.removeControl('recalculationCompoundingFrequencyOnDayType');
                    }
                  });

              } else {
                this.loanProductSettingsForm.removeControl('recalculationCompoundingFrequencyType');
              }
            });

          this.loanProductSettingsForm.get('recalculationRestFrequencyType').valueChanges
            .subscribe((recalculationRestFrequencyType: any) => {
              if (recalculationRestFrequencyType !== 1) {
                this.loanProductSettingsForm.addControl('recalculationRestFrequencyInterval', new UntypedFormControl('', Validators.required));
              } else {
                this.loanProductSettingsForm.removeControl('recalculationRestFrequencyInterval');
              }

              if (recalculationRestFrequencyType === 3) {
                this.loanProductSettingsForm.addControl('recalculationRestFrequencyDayOfWeekType', new UntypedFormControl(''));
                this.loanProductSettingsForm.removeControl('recalculationRestFrequencyNthDayType');
                this.loanProductSettingsForm.removeControl('recalculationRestFrequencyOnDayType');
              } else if (recalculationRestFrequencyType === 4) {
                this.loanProductSettingsForm.addControl('recalculationRestFrequencyNthDayType', new UntypedFormControl(''));
                this.loanProductSettingsForm.addControl('recalculationRestFrequencyDayOfWeekType', new UntypedFormControl(''));

                this.loanProductSettingsForm.get('recalculationRestFrequencyNthDayType').valueChanges
                  .subscribe((recalculationRestFrequencyNthDayType: any) => {
                    if (recalculationRestFrequencyNthDayType === -2) {
                      this.loanProductSettingsForm.addControl('recalculationRestFrequencyOnDayType', new UntypedFormControl(''));
                      this.loanProductSettingsForm.removeControl('recalculationRestFrequencyDayOfWeekType');
                    } else {
                      this.loanProductSettingsForm.addControl('recalculationRestFrequencyDayOfWeekType', new UntypedFormControl(''));
                      this.loanProductSettingsForm.removeControl('recalculationRestFrequencyOnDayType');
                    }
                  });

              } else {
                this.loanProductSettingsForm.removeControl('recalculationRestFrequencyNthDayType');
                this.loanProductSettingsForm.removeControl('recalculationRestFrequencyDayOfWeekType');
                this.loanProductSettingsForm.removeControl('recalculationRestFrequencyOnDayType');
              }
            });

        } else {
          this.loanProductSettingsForm.removeControl('preClosureInterestCalculationStrategy');
          this.loanProductSettingsForm.removeControl('rescheduleStrategyMethod');
          this.loanProductSettingsForm.removeControl('interestRecalculationCompoundingMethod');
          this.loanProductSettingsForm.removeControl('recalculationRestFrequencyType');
          this.loanProductSettingsForm.removeControl('isArrearsBasedOnOriginalSchedule');
        }
      });

    this.loanProductSettingsForm.get('holdGuaranteeFunds').valueChanges
      .subscribe(holdGuaranteeFunds => {
        if (holdGuaranteeFunds) {
          this.loanProductSettingsForm.addControl('mandatoryGuarantee', new UntypedFormControl('', Validators.required));
          this.loanProductSettingsForm.addControl('minimumGuaranteeFromOwnFunds', new UntypedFormControl(''));
          this.loanProductSettingsForm.addControl('minimumGuaranteeFromGuarantor', new UntypedFormControl(''));
        } else {
          this.loanProductSettingsForm.removeControl('mandatoryGuarantee');
          this.loanProductSettingsForm.removeControl('minimumGuaranteeFromOwnFunds');
          this.loanProductSettingsForm.removeControl('minimumGuaranteeFromGuarantor');
        }
      });

    this.loanProductSettingsForm.get('multiDisburseLoan').valueChanges
      .subscribe(multiDisburseLoan => {
        if (multiDisburseLoan) {
          this.loanProductSettingsForm.addControl('maxTrancheCount', new UntypedFormControl('', Validators.required));
          this.loanProductSettingsForm.addControl('outstandingLoanBalance', new UntypedFormControl(''));
        } else {
          this.loanProductSettingsForm.removeControl('maxTrancheCount');
          this.loanProductSettingsForm.removeControl('outstandingLoanBalance');
        }
      });

    this.loanProductSettingsForm.get('allowAttributeConfiguration').valueChanges
      .subscribe((allowAttributeConfiguration: any) => {
        if (allowAttributeConfiguration) {
          allowAttributeOverrides.patchValue({
            'amortizationType': true,
            'interestType': true,
            'transactionProcessingStrategyId': true,
            'interestCalculationPeriodType': true,
            'repaymentStrategyForNpa': true,
            'inArrearsTolerance': true,
            'repaymentEvery': true,
            'graceOnPrincipalAndInterestPayment': true,
            'graceOnArrearsAgeing': true
          });
        } else {
          allowAttributeOverrides.patchValue({
            'amortizationType': false,
            'interestType': false,
            'transactionProcessingStrategyId': false,
            'interestCalculationPeriodType': false,
            'repaymentStrategyForNpa': false,
            'inArrearsTolerance': false,
            'repaymentEvery': false,
            'graceOnPrincipalAndInterestPayment': false,
            'graceOnArrearsAgeing': false
          });
        }
      });
  }

   insuranceApplicabilityChange(event ){
    //let change=event.target.value

    console.log(event,"insuranceChange")
    }

  get loanProductSettings() {
    return this.loanProductSettingsForm.value;
  }

}

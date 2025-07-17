import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators, UntypedFormArray, UntypedFormControl } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';

import { FormDialogComponent } from 'app/shared/form-dialog/form-dialog.component';
import { DeleteDialogComponent } from 'app/shared/delete-dialog/delete-dialog.component';

import { FormfieldBase } from 'app/shared/form-dialog/formfield/model/formfield-base';
import { InputBase } from 'app/shared/form-dialog/formfield/model/input-base';
import { SelectBase } from 'app/shared/form-dialog/formfield/model/select-base';
import { Dates } from 'app/core/utils/dates';

@Component({
  selector: 'mifosx-loan-product-terms-step',
  templateUrl: './loan-product-terms-step.component.html',
  styleUrls: ['./loan-product-terms-step.component.scss']
})
export class LoanProductTermsStepComponent implements OnInit {

  @Input() loanProductsTemplate: any;

  loanProductTermsForm: UntypedFormGroup;

  valueConditionTypeData: any;
  floatingRateData: any;
  interestRateFrequencyTypeData: any;
  dedupeOptions: any;
  overAppliedCalculationTypeData: any;
  dateTypeData:any;
  minimumAge:any;
  maximumAge:any;
  prepayLockingPeriod:any;
  foreclosureLockingPeriod:any;
  repaymentFrequencyTypeData: any;
  overAmtCalTypSel:any;
  dateTypSel:any;

  displayedColumns: string[] = ['valueConditionType', 'borrowerCycleNumber', 'minValue', 'defaultValue', 'maxValue', 'actions'];

  constructor(private formBuilder: UntypedFormBuilder,private dateUtils: Dates,
              public dialog: MatDialog) {
    this.createLoanProductTermsForm();
    this.setConditionalControls();
  }
overAmtCalTyp(event){
this.overAmtCalTypSel=event.value;
}
selectDateType(event){
this.dateTypSel=event.value;

}
  ngOnInit() {
    this.valueConditionTypeData = this.loanProductsTemplate.valueConditionTypeOptions;
    this.floatingRateData = this.loanProductsTemplate.floatingRateOptions;
    this.interestRateFrequencyTypeData = this.loanProductsTemplate.interestRateFrequencyTypeOptions;
    this.dedupeOptions = this.loanProductsTemplate.dedupeOptions;
    this.repaymentFrequencyTypeData = this.loanProductsTemplate.repaymentFrequencyTypeOptions;
    this.overAppliedCalculationTypeData = [{id: 'percentage', value: 'Percentage'}, {id: 'flat', value: 'Fixed Amount'}];
    this.dateTypeData = [{id: 'Range', value: 'Range'}, {id: 'SelectDate', value: 'Select Date'}];
    this.loanProductTermsForm.patchValue({
      'minPrincipal': this.loanProductsTemplate.minPrincipal,
      'principal': this.loanProductsTemplate.principal,
      'maxPrincipal': this.loanProductsTemplate.maxPrincipal,
      'minNumberOfRepayments': this.loanProductsTemplate.minNumberOfRepayments,
      'numberOfRepayments': this.loanProductsTemplate.numberOfRepayments,
      'maxNumberOfRepayments': this.loanProductsTemplate.maxNumberOfRepayments,
      'isLinkedToFloatingInterestRates': this.loanProductsTemplate.isLinkedToFloatingInterestRates,
      'minInterestRatePerPeriod': this.loanProductsTemplate.minInterestRatePerPeriod,
      'interestRatePerPeriod': this.loanProductsTemplate.interestRatePerPeriod,
      'maxInterestRatePerPeriod': this.loanProductsTemplate.maxInterestRatePerPeriod,
      'interestRateFrequencyType': this.loanProductsTemplate.interestRateFrequencyType.id,
      'floatingRatesId': this.loanProductsTemplate.floatingRateId,
      'interestRateDifferential': this.loanProductsTemplate.interestRateDifferential,
      'isFloatingInterestRateCalculationAllowed': this.loanProductsTemplate.isFloatingInterestRateCalculationAllowed,
//       'allowApprovalOverAmountApplied': this.loanProductsTemplate.allowApprovalOverAmountApplied,
      'minDifferentialLendingRate': this.loanProductsTemplate.minDifferentialLendingRate,
      'defaultDifferentialLendingRate': this.loanProductsTemplate.defaultDifferentialLendingRate,
      'maxDifferentialLendingRate': this.loanProductsTemplate.maxDifferentialLendingRate,
      'useBorrowerCycle': this.loanProductsTemplate.useBorrowerCycle,
      'repaymentEvery': this.loanProductsTemplate.repaymentEvery,
      'repaymentFrequencyType': this.loanProductsTemplate.repaymentFrequencyType.id,
      'minimumDaysBetweenDisbursalAndFirstRepayment': this.loanProductsTemplate.minimumDaysBetweenDisbursalAndFirstRepayment,
//       'coBorrower': this.loanProductsTemplate.coBorrower,
//       'eodBalance': this.loanProductsTemplate.eodBalance,
//       'securedLoan': this.loanProductsTemplate.securedLoan,
//       'nonEquatedInstallment': this.loanProductsTemplate.nonEquatedInstallment,
//       'allowAgeLimits': this.loanProductsTemplate.allowAgeLimits,
//       'advanceEMI': this.loanProductsTemplate.advanceEMI,
//       'termBasedOnLoanCycle': this.loanProductsTemplate.termBasedOnLoanCycle,
//       'isNetOffApplied': this.loanProductsTemplate.isNetOffApplied,
      'selectAcceptedDates': this.loanProductsTemplate.selectAcceptedDates,
      'applyPrepaidLockingPeriod': this.loanProductsTemplate.applyPrepaidLockingPeriod,
      'applyForeclosureLockingPeriod': this.loanProductsTemplate.applyForeclosureLockingPeriod,
        'enableDedupe': this.loanProductsTemplate.enableDedupe,
      'dedupeType': this.loanProductsTemplate.selectedDedupe && this.loanProductsTemplate.selectedDedupe.id
    });

//     if (this.loanProductsTemplate.allowAgeLimits) {
//       this.loanProductTermsForm.patchValue({
//                'minimumAge': this.loanProductsTemplate.minimumAge,
//                'maximumAge': this.loanProductsTemplate.maximumAge,
//       });
//     }
if (this.loanProductsTemplate.selectAcceptedDates) {
      this.loanProductTermsForm.patchValue({
      'acceptedDateType': this.loanProductsTemplate.acceptedDateType,
      'acceptedStartDate': this.loanProductsTemplate.acceptedStartDate,
      'acceptedEndDate': this.loanProductsTemplate.acceptedEndDate,
        'acceptedDate': this.loanProductsTemplate.acceptedDate,
      });
    }
    if (this.loanProductsTemplate.applyPrepaidLockingPeriod) {
          this.loanProductTermsForm.patchValue({
               'prepayLockingPeriod': this.loanProductsTemplate.prepayLockingPeriod,
          });
        }
        if (this.loanProductsTemplate.applyForeclosureLockingPeriod) {
              this.loanProductTermsForm.patchValue({
                'foreclosureLockingPeriod': this.loanProductsTemplate.foreclosureLockingPeriod,
              });
            }

//     if (this.loanProductsTemplate.allowApprovalOverAmountApplied) {
//           this.loanProductTermsForm.patchValue({
//             'overAppliedCalculationType': this.loanProductsTemplate.overAppliedCalculationType,
//             'overAppliedNumber': this.loanProductsTemplate.overAppliedNumber,
//           });
//         }

    this.loanProductTermsForm.setControl('principalVariationsForBorrowerCycle',
      this.formBuilder.array(this.loanProductsTemplate.principalVariationsForBorrowerCycle.map((variation: any) => ({ ...variation, valueConditionType: variation.valueConditionType.id }))));
    this.loanProductTermsForm.setControl('numberOfRepaymentVariationsForBorrowerCycle',
      this.formBuilder.array(this.loanProductsTemplate.numberOfRepaymentVariationsForBorrowerCycle.map((variation: any) => ({ ...variation, valueConditionType: variation.valueConditionType.id }))));
    this.loanProductTermsForm.setControl('interestRateVariationsForBorrowerCycle',
      this.formBuilder.array(this.loanProductsTemplate.interestRateVariationsForBorrowerCycle.map((variation: any) => ({ ...variation, valueConditionType: variation.valueConditionType.id }))));
  }

  createLoanProductTermsForm() {
    this.loanProductTermsForm = this.formBuilder.group({
      'useBorrowerCycle': [false],
      'minPrincipal': [''],
      'principal': ['', Validators.required],
      'maxPrincipal': [''],
      'minNumberOfRepayments': [''],
      'numberOfRepayments': ['', Validators.required],
      'maxNumberOfRepayments': [''],
      'isLinkedToFloatingInterestRates': [false],
//       'allowApprovalOverAmountApplied': [false],
      'minInterestRatePerPeriod': [''],
      'interestRatePerPeriod': ['', Validators.required],
      'maxInterestRatePerPeriod': [''],
      'interestRateFrequencyType': ['', Validators.required],
      'repaymentEvery': ['', Validators.required],
      'repaymentFrequencyType': ['', Validators.required],
//       'coBorrower': [''],
//       'eodBalance': [''],
//       'securedLoan': [''],
//       'nonEquatedInstallment': [''],
//       'allowAgeLimits': [false],
//       'advanceEMI': [''],
//       'termBasedOnLoanCycle': [''],
//       'isNetOffApplied': [''],
      'selectAcceptedDates': [false],
      'applyPrepaidLockingPeriod': [''],
      'applyForeclosureLockingPeriod': [''],
      'minimumDaysBetweenDisbursalAndFirstRepayment':[''],
      'enableDedupe': [false],
      'dedupeType': ['']
    });
  }
 createselectDateForm(): UntypedFormGroup {
    return this.formBuilder.group({
      'acceptedDate': [''],
    });
  }
  get selectDateArr(): UntypedFormArray {
      return this.loanProductTermsForm.get('selectDateArr') as UntypedFormArray;
    }
      removeSelectDateArr(affectedSelectDateFormArray: UntypedFormArray, index: number) {
        affectedSelectDateFormArray.removeAt(index);
      }
      addSelectDateArr(affectedSelectDateFormArray: UntypedFormArray) {
          affectedSelectDateFormArray.push(this.createselectDateForm());
        }
  setConditionalControls() {

//   this.loanProductTermsForm.get('allowAgeLimits').valueChanges
//         .subscribe(allowAgeLimits => {
//           if (allowAgeLimits) {
//             this.loanProductTermsForm.addControl('minimumAge', new FormControl(''));
//             this.loanProductTermsForm.addControl('maximumAge', new FormControl(''));
//           } else {
//             this.loanProductTermsForm.removeControl('minimumAge');
//             this.loanProductTermsForm.removeControl('maximumAge');
//           }
//         }
//       );
      this.loanProductTermsForm.get('applyPrepaidLockingPeriod').valueChanges
              .subscribe(applyPrepaidLockingPeriod => {
                if (applyPrepaidLockingPeriod) {
                  this.loanProductTermsForm.addControl('prepayLockingPeriod', new UntypedFormControl('', Validators.required));
                } else {
                  this.loanProductTermsForm.removeControl('prepayLockingPeriod');
                }
              }
            );
      this.loanProductTermsForm.get('applyForeclosureLockingPeriod').valueChanges
                    .subscribe(applyForeclosureLockingPeriod => {
                      if (applyForeclosureLockingPeriod) {
                        this.loanProductTermsForm.addControl('foreclosureLockingPeriod', new UntypedFormControl('', Validators.required));
                      } else {
                        this.loanProductTermsForm.removeControl('foreclosureLockingPeriod');
                      }
                    }
                  );

      this.loanProductTermsForm.get('selectAcceptedDates').valueChanges
              .subscribe(selectAcceptedDates => {
                if (selectAcceptedDates) {
                this.loanProductTermsForm.addControl('acceptedDateType', new UntypedFormControl('', Validators.required));
                 this.loanProductTermsForm.addControl('selectDateArr', this.formBuilder.array([this.createselectDateForm()]),);
                  this.loanProductTermsForm.addControl('acceptedStartDate', new UntypedFormControl('', Validators.required));
                  this.loanProductTermsForm.addControl('acceptedEndDate', new UntypedFormControl('', Validators.required));
                } else {
                  this.loanProductTermsForm.removeControl('acceptedDateType');
                  this.loanProductTermsForm.removeControl('acceptedStartDate');
                  this.loanProductTermsForm.removeControl('acceptedEndDate');
                  this.loanProductTermsForm.removeControl('selectDateArr');
                }
              }
            );
//     this.loanProductTermsForm.get('allowApprovalOverAmountApplied').valueChanges
//       .subscribe(allowApprovalOverAmountApplied => {
//         if (allowApprovalOverAmountApplied) {
//           this.loanProductTermsForm.addControl('overAppliedCalculationType', new FormControl(''));
//           this.loanProductTermsForm.addControl('overAppliedNumber', new FormControl(''));
//           this.loanProductTermsForm.addControl('disallowExpectedDisbursements', new FormControl('true'));
//         } else {
//           this.loanProductTermsForm.removeControl('overAppliedCalculationType');
//           this.loanProductTermsForm.removeControl('overAppliedNumber');
//           this.loanProductTermsForm.removeControl('disallowExpectedDisbursements');
//         }
//       }
//     );

    this.loanProductTermsForm.get('isLinkedToFloatingInterestRates').valueChanges
      .subscribe(isLinkedToFloatingInterestRates => {
        if (isLinkedToFloatingInterestRates) {
          this.loanProductTermsForm.removeControl('minInterestRatePerPeriod');
          this.loanProductTermsForm.removeControl('interestRatePerPeriod');
          this.loanProductTermsForm.removeControl('maxInterestRatePerPeriod');
          this.loanProductTermsForm.removeControl('interestRateFrequencyType');
          this.loanProductTermsForm.addControl('floatingRatesId', new UntypedFormControl('', Validators.required));
          this.loanProductTermsForm.addControl('interestRateDifferential', new UntypedFormControl('', Validators.required));
          this.loanProductTermsForm.addControl('isFloatingInterestRateCalculationAllowed', new UntypedFormControl(false));
          this.loanProductTermsForm.addControl('minDifferentialLendingRate', new UntypedFormControl('', Validators.required));
          this.loanProductTermsForm.addControl('defaultDifferentialLendingRate', new UntypedFormControl('', Validators.required));
          this.loanProductTermsForm.addControl('maxDifferentialLendingRate', new UntypedFormControl('', Validators.required));
        } else {
          this.loanProductTermsForm.addControl('minInterestRatePerPeriod', new UntypedFormControl(''));
          this.loanProductTermsForm.addControl('interestRatePerPeriod', new UntypedFormControl('', Validators.required));
          this.loanProductTermsForm.addControl('maxInterestRatePerPeriod', new UntypedFormControl(''));
          this.loanProductTermsForm.addControl('interestRateFrequencyType', new UntypedFormControl(this.interestRateFrequencyTypeData.id, Validators.required));
          this.loanProductTermsForm.removeControl('floatingRatesId');
          this.loanProductTermsForm.removeControl('interestRateDifferential');
          this.loanProductTermsForm.removeControl('isFloatingInterestRateCalculationAllowed');
          this.loanProductTermsForm.removeControl('minDifferentialLendingRate');
          this.loanProductTermsForm.removeControl('defaultDifferentialLendingRate');
          this.loanProductTermsForm.removeControl('maxDifferentialLendingRate');
        }
      });

      this.loanProductTermsForm.get('useBorrowerCycle').valueChanges
        .subscribe(useBorrowerCycle => {
          if (useBorrowerCycle) {
            this.loanProductTermsForm.addControl('principalVariationsForBorrowerCycle', this.formBuilder.array([]));
            this.loanProductTermsForm.addControl('numberOfRepaymentVariationsForBorrowerCycle', this.formBuilder.array([]));
            this.loanProductTermsForm.addControl('interestRateVariationsForBorrowerCycle', this.formBuilder.array([]));
          } else {
            this.loanProductTermsForm.removeControl('principalVariationsForBorrowerCycle');
            this.loanProductTermsForm.removeControl('numberOfRepaymentVariationsForBorrowerCycle');
            this.loanProductTermsForm.removeControl('interestRateVariationsForBorrowerCycle');
          }
        });
  }

  get principalVariationsForBorrowerCycle(): UntypedFormArray {
    return this.loanProductTermsForm.get('principalVariationsForBorrowerCycle') as UntypedFormArray;
  }

  get numberOfRepaymentVariationsForBorrowerCycle(): UntypedFormArray {
    return this.loanProductTermsForm.get('numberOfRepaymentVariationsForBorrowerCycle') as UntypedFormArray;
  }

  get interestRateVariationsForBorrowerCycle(): UntypedFormArray {
    return this.loanProductTermsForm.get('interestRateVariationsForBorrowerCycle') as UntypedFormArray;
  }

  setLoanProductTermsFormDirty() {
    if (this.loanProductTermsForm.pristine) {
      this.loanProductTermsForm.markAsDirty();
    }
  }

  addVariationsForBorrowerCycle(formType: string, variationsForBorrowerCycleFormArray: UntypedFormArray) {
    const data = this.getData(formType);
    const addVariationsForBorrowerCycleDialogRef = this.dialog.open(FormDialogComponent, { data });
    addVariationsForBorrowerCycleDialogRef.afterClosed().subscribe((response: any) => {
      if (response.data) {
        variationsForBorrowerCycleFormArray.push(response.data);
        this.setLoanProductTermsFormDirty();
      }
    });
  }

  editVariationsForBorrowerCycle(formType: string, variationsForBorrowerCycleFormArray: UntypedFormArray, index: number) {
    const data = { ...this.getData(formType, variationsForBorrowerCycleFormArray.at(index).value), layout: { addButtonText: 'Edit' } };
    const addVariationsForBorrowerCycleDialogRef = this.dialog.open(FormDialogComponent, { data });
    addVariationsForBorrowerCycleDialogRef.afterClosed().subscribe((response: any) => {
      if (response.data) {
        variationsForBorrowerCycleFormArray.at(index).patchValue(response.data.value);
        this.setLoanProductTermsFormDirty();
      }
    });
  }

  deleteVariationsForBorrowerCycle(variationsForBorrowerCycleFormArray: UntypedFormArray, index: number) {
    const deleteVariationsForBorrowerCycleDialogRef = this.dialog.open(DeleteDialogComponent, {
      data: { deleteContext: `this` }
    });
    deleteVariationsForBorrowerCycleDialogRef.afterClosed().subscribe((response: any) => {
      if (response.delete) {
        variationsForBorrowerCycleFormArray.removeAt(index);
        this.setLoanProductTermsFormDirty();
      }
    });
  }

  getData(formType: string, values?: any) {
    switch (formType) {
      case 'Principal': return { title: 'Principal by loan cycle', formfields: this.getFormfields(values) };
      case 'NumberOfRepayments': return { title: 'Number of repayments by loan cycle', formfields: this.getFormfields(values) };
      case 'NominalInterestRate': return { title: 'Nominal interest rate by loan cycle', formfields: this.getFormfields(values) };
    }
  }

  getFormfields(values?: any) {
    const formfields: FormfieldBase[] = [
      new SelectBase({
        controlName: 'valueConditionType',
        label: 'Condition',
        value: values ? values.valueConditionType : this.valueConditionTypeData[0].id,
        options: { label: 'value', value: 'id', data: this.valueConditionTypeData },
        required: true,
        order: 1
      }),
      new InputBase({
        controlName: 'borrowerCycleNumber',
        label: 'Loan Cycle',
        value: values ? values.borrowerCycleNumber : undefined,
        type: 'number',
        required: true,
        order: 2
      }),
      new InputBase({
        controlName: 'minValue',
        label: 'Minimum',
        value: values ? values.minValue : undefined,
        type: 'number',
        order: 3
      }),
      new InputBase({
        controlName: 'defaultValue',
        label: 'Default',
        value: values ? values.defaultValue : undefined,
        type: 'number',
        required: true,
        order: 4
      }),
      new InputBase({
        controlName: 'maxValue',
        label: 'Maximum',
        value: values ? values.maxValue : undefined,
        type: 'number',
        order: 5
      })
    ];
    return formfields;
  }

  get loanProductTerms() {
    return this.loanProductTermsForm.value;
  }

}

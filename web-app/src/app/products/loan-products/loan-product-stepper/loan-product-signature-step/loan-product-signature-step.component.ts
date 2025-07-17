import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators, UntypedFormArray, UntypedFormControl } from '@angular/forms';

@Component({
  selector: 'mifosx-loan-product-signature-step',
  templateUrl: './loan-product-signature-step.component.html',
  styleUrls: ['./loan-product-signature-step.component.scss']
})
export class LoanProductSignatureStepComponent implements OnInit {
  loanProductSignatureForm: UntypedFormGroup;
  @Input() loanProductsTemplate: any;
  partnerData: any;
  chargeId: any;
  chargeNameHead: any;
  feesId: any;
  feesNameHead: any;
  overDueId;
  overDueNameHead: any;
  frameworkData: any;
  loanTypeData: any;
  disbursementData: any;
  collectionData: any;
  transactionTypeData: any;
  disbursementBankAccountNameOptions: any;
  //insuranceApplicabilityData:any;

  colendingChargeData: any;
  colendingFeesData: any;
  penalInvoiceData: any;
  multipleDisbursementData: any;
  trancheClubbingData: any;
  repaymentScheduleUpdateAllowedData: any;
  colendingOverDueData: any;
  enableBackDatedDisbursementData:any;

  constructor(private formBuilder: UntypedFormBuilder) {
    this.createLoanProductSignatureForm();
    this.setConditionalControls();

  }
penaltyAmount:any;
  ngOnInit(): void {
    this.partnerData = this.loanProductsTemplate.partnerData;
    this.colendingChargeData = this.loanProductsTemplate.chargeOptions;
    this.colendingFeesData = this.loanProductsTemplate.feeOption;
    this.colendingOverDueData = this.loanProductsTemplate.penaltyOptions;
    this.frameworkData = this.loanProductsTemplate.frameWorkOptions;
    this.loanTypeData = this.loanProductsTemplate.loanTypeOptions;
    this.disbursementData = this.loanProductsTemplate.disbursementOptions;
    this.collectionData = this.loanProductsTemplate.collectionOptions;
    this.penalInvoiceData = this.loanProductsTemplate.penalInvoiceOptions;
    this.multipleDisbursementData = this.loanProductsTemplate.multipleDisbursementOptions;
    this.trancheClubbingData = this.loanProductsTemplate.trancheClubbingOptions;
    this.repaymentScheduleUpdateAllowedData = this.loanProductsTemplate.repaymentScheduleUpdateAllowedOptions;
    this.transactionTypeData = this.loanProductsTemplate.transactionTypeOptions;
    this.disbursementBankAccountNameOptions = this.loanProductsTemplate.disbursementBankAccountNameOptions;
    this.enableBackDatedDisbursementData = this.loanProductsTemplate.enableBackDatedDisbursement;

    if (this.loanProductsTemplate.colendingFees) {
      this.loanProductSignatureForm.addControl('selectFees', this.formBuilder.array([]));
      for (let i = 0; i < this.loanProductsTemplate.colendingFees.length; i++) {
        this.selectFees.push(
          this.formBuilder.group({
            colendingFees: [this.loanProductsTemplate.colendingFees[i].colendingFees],
            selfFees: [this.loanProductsTemplate.colendingFees[i].selfFees],
            partnerFees: [this.loanProductsTemplate.colendingFees[i].partnerFees],
            clientFees: [100]
          })
        )
      }
    }
    if (this.loanProductsTemplate.colendingCharges) {
      this.loanProductSignatureForm.addControl('selectCharge', this.formBuilder.array([]));
      for (let i = 0; i < this.loanProductsTemplate.colendingCharges.length; i++) {
        this.selectCharge.push(
          this.formBuilder.group({
            colendingCharge: [this.loanProductsTemplate.colendingCharges[i].colendingCharge],
            selfCharge: [this.loanProductsTemplate.colendingCharges[i].selfCharge],
            partnerCharge: [this.loanProductsTemplate.colendingCharges[i].partnerCharge],
            clientCharge: [100]
          })
        )
      }
    }
    if (this.loanProductsTemplate.overDueCharges) {
      this.loanProductSignatureForm.addControl('selectOverDue', this.formBuilder.array([]));
      for (let i = 0; i < this.loanProductsTemplate.overDueCharges.length; i++) {
        for (let j = 0; j < this.colendingOverDueData.length; j++) {
          if (this.colendingOverDueData[j].id == this.loanProductsTemplate.overDueCharges[i].chargeId) {
            this.penaltyAmount = this.colendingOverDueData[j].amount;
          }
        }
        this.selectOverDue.push(
          this.formBuilder.group({
            overDueCharge: [this.loanProductsTemplate.overDueCharges[i].chargeId],
            selfOverDue: [this.loanProductsTemplate.overDueCharges[i].selfOverDue],
            partnerOverDue: [this.loanProductsTemplate.overDueCharges[i].partnerOverDue],
            clientOverDue: [this.penaltyAmount]
          })
        )
      }
    }
    this.loanProductSignatureForm.patchValue({

      'enableColendingLoan': this.loanProductsTemplate.enableColendingLoan,
      'byPercentageSplit': this.loanProductsTemplate.byPercentageSplit,
      'principalShare': this.loanProductsTemplate.principalShare,
      'feeShare': this.loanProductsTemplate.feeShare,
      'penaltyShare': this.loanProductsTemplate.penaltyShare,
      'overpaidShare': this.loanProductsTemplate.overpaidShare,
      'selfPrincipalShare': this.loanProductsTemplate.selfPrincipalShare,
      'selfFeeShare': this.loanProductsTemplate.selfFeeShare,
      'selfPenaltyShare': this.loanProductsTemplate.selfPenaltyShare,
      'selfOverpaidShares': this.loanProductsTemplate.selfOverpaidShares,
      'selfInterestRate': this.loanProductsTemplate.selfInterestRate,
      'partnerPrincipalShare': this.loanProductsTemplate.partnerPrincipalShare,
      'partnerFeeShare': this.loanProductsTemplate.partnerFeeShare,
      'partnerPenaltyShare': this.loanProductsTemplate.partnerPenaltyShare,
      'partnerOverpaidShare': this.loanProductsTemplate.partnerOverpaidShare,
      'partnerInterestRate': this.loanProductsTemplate.partnerInterestRate,
      'partnerId': this.loanProductsTemplate.partnerId,
      'enableChargeWiseBifacation': this.loanProductsTemplate.enableChargeWiseBifacation,
      'enableFeesWiseBifacation': this.loanProductsTemplate.enableFeesWiseBifacation,
      'enableOverDue': this.loanProductsTemplate.enableOverDue,
      'colendingCharge': this.loanProductsTemplate.colendingCharge,
      'overDueCharges': this.loanProductsTemplate.overDueCharges,
      'selfCharge': this.loanProductsTemplate.selfCharge,
      'partnerCharge': this.loanProductsTemplate.partnerCharge,
      'clientCharge': this.loanProductsTemplate.clientCharge,
      'selfOverDue': this.loanProductsTemplate.selfOverDue,
      'partnerOverDue': this.loanProductsTemplate.partnerOverDue,
      //       'clientOverDue':this.loanProductsTemplate.clientOverDue,
      //       'clientOverDue':this.loanProductsTemplate.penaltyOptions[0].amount,
      'interestRate': this.loanProductsTemplate.interestRate,
      'gstLiabilityByVcpl': this.loanProductsTemplate.gstLiabilityByVcpl,
      'gstLiabilityByPartner': this.loanProductsTemplate.gstLiabilityByPartner,
      'disbursementAccountNumber': this.loanProductsTemplate.disbursementAccountNumber,
      'collectionAccountNumber': this.loanProductsTemplate.collectionAccountNumber,
//       'vcplHurdleRate': this.loanProductsTemplate.vcplHurdleRate,
      'loanType': this.loanProductsTemplate.loanType && this.loanProductsTemplate.loanType.id,
      'frameWork': this.loanProductsTemplate.frameWork && this.loanProductsTemplate.frameWork.id,
      // 'fldgLogic':this.loanProductsTemplate.fldgLogic && this.loanProductsTemplate.fldgLogic.id,
      'disbursement': this.loanProductsTemplate.disbursementId && this.loanProductsTemplate.disbursementId.id,
      'collection': this.loanProductsTemplate.collectionId && this.loanProductsTemplate.collectionId.id,
      'assetClass': this.loanProductsTemplate.assetClass && this.loanProductsTemplate.assetClass.id,
      'penalInvoice': this.loanProductsTemplate.penalInvoice && this.loanProductsTemplate.penalInvoice.id,
      'multipleDisbursement': this.loanProductsTemplate.multipleDisbursement && this.loanProductsTemplate.multipleDisbursement.id,
      'trancheClubbing': this.loanProductsTemplate.trancheClubbing && this.loanProductsTemplate.trancheClubbing.id,
      'repaymentScheduleUpdateAllowed': this.loanProductsTemplate.repaymentScheduleUpdateAllowed && this.loanProductsTemplate.repaymentScheduleUpdateAllowed.id,
      'brokenPeriodInterest': this.loanProductsTemplate.brokenPeriodInterest,
      'vcplShareInBrokenInterest': this.loanProductsTemplate.vcplShareInBrokenInterest,
      'partnerShareInBrokenInterest': this.loanProductsTemplate.partnerShareInBrokenInterest,
      'transactionTypePreference': this.loanProductsTemplate.transactionTypePreference && this.loanProductsTemplate.transactionTypePreference.id,
      'isPennyDropEnabled': this.loanProductsTemplate.isPennyDropEnabled,
      'isBankDisbursementEnabled': this.loanProductsTemplate.isBankDisbursementEnabled,
      'disbursementBankAccountName': this.loanProductsTemplate.selectedDisbursementBankAccountName && this.loanProductsTemplate.selectedDisbursementBankAccountName.id,
//       'enableBackDatedDisbursement' : this.loanProductsTemplate.enableBackDatedDisbursementSelected && this.loanProductsTemplate.enableBackDatedDisbursementSelected.id,
      'enableBackDatedDisbursement' : this.loanProductsTemplate.enableBackDatedDisbursementSelected,
    });
  }


  get selectFees(): UntypedFormArray {
    return this.loanProductSignatureForm.get('selectFees') as UntypedFormArray;
  }
  removeSelectFeesArr(index: number) {
    this.selectFees.removeAt(index);
  }
  addSelectFeesArr() {
    this.selectFees.push(this.formBuilder.group({
      colendingFees: '',
      selfFees: '',
      partnerFees: '',
      clientFees: 100
    }));
  }

  get selectCharge(): UntypedFormArray {
    return this.loanProductSignatureForm.get('selectCharge') as UntypedFormArray;
  }
  removeSelectChargeArr(index: number) {
    this.selectCharge.removeAt(index);
  }
  addSelectChargeArr() {
    this.selectCharge.push(this.formBuilder.group({
      colendingCharge: '',
      selfCharge: '',
      partnerCharge: '',
      clientCharge: 100
    }));
  }

  get selectOverDue(): UntypedFormArray {
    return this.loanProductSignatureForm.get('selectOverDue') as UntypedFormArray;
  }
  removeSelectOverDueArr(index: number) {
    this.selectOverDue.removeAt(index);
  }
  addSelectOverDueArr() {
    this.selectOverDue.push(this.formBuilder.group({
      overDueCharge: '',
      selfOverDue: '',
      partnerOverDue: '',
      clientOverDue: '',
    }));
  }


  createLoanProductSignatureForm() {
    this.loanProductSignatureForm = this.formBuilder.group({

      'enableColendingLoan': [false],
      'partnerId': ['', Validators.required],
      'gstLiabilityByVcpl': ['', Validators.required],
      'gstLiabilityByPartner': ['', Validators.required],
      'disbursementAccountNumber': [''],
      'collectionAccountNumber': [''],
      'loanType': [''],
      'frameWork': [],
      'enableBackDatedDisbursement' :[false],
      'disbursement': ['', Validators.required],
      'collection': ['', Validators.required],
      'penalInvoice': [''],
      'multipleDisbursement': [''],
      'trancheClubbing': [''],
      'repaymentScheduleUpdateAllowed': [''],
      'transactionTypePreference': [''],
      'isPennyDropEnabled': [false],
      'isBankDisbursementEnabled': [false],
      'disbursementBankAccountName': ['']
    });
  }

  get loanProductSignature() {
    return this.loanProductSignatureForm.value;
  }


  FeesValue(event) {
    this.feesId = event.value;
    for (let index in this.colendingFeesData) {
      if (this.feesId == this.colendingFeesData[index].id) {
        this.feesNameHead = this.colendingFeesData[index].name;
      }
    }
  }
  chargeValue(event) {
    this.chargeId = event.value;
    for (let index in this.colendingChargeData) {
      if (this.chargeId == this.colendingChargeData[index].id) {
        this.chargeNameHead = this.colendingChargeData[index].name;
      }
    }
  }


  OverDueValue(event){
    this.overDueId=event.value;
    for(let i=0; i < this.colendingOverDueData.length; i++) {
        if(this.overDueId==this.colendingOverDueData[i].id){
        let amount=this.colendingOverDueData[i].amount;
//         this.loanProductSignatureForm.get('clientOverDue').setValue(`${amount}`);
        this.loanProductSignatureForm.patchValue({ 'clientOverDue': {amount}});
//           this.loanProductSignatureForm.patchValue({  'clientOverDue': amount});
        }
        }
    for(let index in this.colendingOverDueData){
      if(this.overDueId==this.colendingOverDueData[index].id){
        this.overDueNameHead=this.colendingOverDueData[index].name;
      }
    }
    }

  setConditionalControls() {
    this.loanProductSignatureForm.get('enableColendingLoan').valueChanges
      .subscribe((enableColendingLoan: any) => {
        if (enableColendingLoan) {
          this.loanProductSignatureForm.addControl('byPercentageSplit', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('partnerId', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('principalShare', new UntypedFormControl(100));
          this.loanProductSignatureForm.addControl('feeShare', new UntypedFormControl(100));
          this.loanProductSignatureForm.addControl('penaltyShare', new UntypedFormControl(100));
          this.loanProductSignatureForm.addControl('overpaidShare', new UntypedFormControl(100));
          this.loanProductSignatureForm.addControl('interestRate', new UntypedFormControl(100));
          this.loanProductSignatureForm.addControl('selfPrincipalShare', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('selfFeeShare', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('selfPenaltyShare', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('selfOverpaidShares', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('selfInterestRate', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('partnerPrincipalShare', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('partnerFeeShare', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('partnerPenaltyShare', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('partnerOverpaidShare', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('partnerInterestRate', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('enableChargeWiseBifacation', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('enableFeesWiseBifacation', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('enableOverDue', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('brokenPeriodInterest', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('vcplShareInBrokenInterest', new UntypedFormControl(''));
          this.loanProductSignatureForm.addControl('partnerShareInBrokenInterest', new UntypedFormControl(''));
//           this.loanProductSignatureForm.addControl('vcplHurdleRate', new FormControl(''));

  this.loanProductSignatureForm.get('enableFeesWiseBifacation').valueChanges
  .subscribe((enableFeesWiseBifacation: any) => {
   if (enableFeesWiseBifacation) {
  this.loanProductSignatureForm.addControl('selectFees', this.formBuilder.array([]));
                 }
        else{
         this.loanProductSignatureForm.removeControl('selectFees');
        }

          });
   this.loanProductSignatureForm.get('enableChargeWiseBifacation').valueChanges
 .subscribe((enableChargeWiseBifacation: any) => {
 if (enableChargeWiseBifacation) {
   this.loanProductSignatureForm.addControl('selectCharge', this.formBuilder.array([]));
 }
  else{
  this.loanProductSignatureForm.removeControl('selectCharge');
  }
  });
   this.loanProductSignatureForm.get('enableOverDue').valueChanges
    .subscribe((enableOverDue: any) => {
     if (enableOverDue) {
    this.loanProductSignatureForm.addControl('selectOverDue', this.formBuilder.array([]));
                   }
          else{
           this.loanProductSignatureForm.removeControl('selectOverDue');
          }

            });
        } else {
          this.loanProductSignatureForm.removeControl('byPercentageSplit');
          this.loanProductSignatureForm.removeControl('partnerId');
          this.loanProductSignatureForm.removeControl('principalShare');
          this.loanProductSignatureForm.removeControl('feeShare');
          this.loanProductSignatureForm.removeControl('penaltyShare');
          this.loanProductSignatureForm.removeControl('overpaidShare');
          this.loanProductSignatureForm.removeControl('interestRate');
          this.loanProductSignatureForm.removeControl('selfPrincipalShare');
          this.loanProductSignatureForm.removeControl('selfFeeShare');
          this.loanProductSignatureForm.removeControl('selfPenaltyShare');
          this.loanProductSignatureForm.removeControl('selfOverpaidShares');
          this.loanProductSignatureForm.removeControl('selfInterestRate');
          this.loanProductSignatureForm.removeControl('partnerPrincipalShare');
          this.loanProductSignatureForm.removeControl('partnerFeeShare');
          this.loanProductSignatureForm.removeControl('partnerPenaltyShare');
          this.loanProductSignatureForm.removeControl('partnerOverpaidShare');
          this.loanProductSignatureForm.removeControl('partnerInterestRate');
          this.loanProductSignatureForm.removeControl('enableChargeWiseBifacation');
          this.loanProductSignatureForm.removeControl('enableFeesWiseBifacation');
          this.loanProductSignatureForm.removeControl(' enableOverDue');
          this.loanProductSignatureForm.removeControl('brokenPeriodInterest');
          this.loanProductSignatureForm.removeControl('vcplShareInBrokenInterest');
          this.loanProductSignatureForm.removeControl('partnerShareInBrokenInterest');
          //                 this.loanProductSignatureForm.removeControl('colendingCharge');
          this.loanProductSignatureForm.removeControl('selectCharge');
          this.loanProductSignatureForm.removeControl('selectFees');
          this.loanProductSignatureForm.removeControl('selectOverDue');
//           this.loanProductSignatureForm.removeControl('vcplHurdleRate');
          //                 this.loanProductSignatureForm.removeControl('partnerCharge');
        }
      });

  }
  principalChange() {

    this.loanProductSignatureForm.patchValue({ 'partnerPrincipalShare': 100 - this.loanProductSignatureForm.value.selfPrincipalShare });
  }
  interestChange() {
    this.loanProductSignatureForm.patchValue({ 'partnerInterestRate': 100 - this.loanProductSignatureForm.value.selfInterestRate });
  }

  brokenChange() {
    this.loanProductSignatureForm.patchValue({ 'partnerShareInBrokenInterest': 100 - this.loanProductSignatureForm.value.vcplShareInBrokenInterest });
  }
}

import { Component, OnInit,Input } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { LoansService } from 'app/loans/loans.service';
import { ActivatedRoute, Router } from '@angular/router';

/** Custom Services */
import { SettingsService } from 'app/settings/settings.service';
import { Dates } from 'app/core/utils/dates';
import { NotificationService } from '../../../../notification.service'

@Component({
  selector: 'mifosx-foreclosure',
  templateUrl: './foreclosure.component.html',
  styleUrls: ['./foreclosure.component.scss']
})
export class ForeclosureComponent implements OnInit {

  @Input() dataObject: any;
  loanId: any;
  foreclosureForm: UntypedFormGroup;
  /** Principal Portion */
    principalPortion: any;
      /** Interest Portion */
     interestPortion: any;
     advanceAmount: any;
  /** Minimum Date allowed. */
  minDate = new Date(2000, 0, 1);
  /** Maximum Date allowed. */
  maxDate = new Date();
  foreclosuredata: any;
  paymentTypes: any;
foreClosureAmount:any;
repaymentModeOptions:any;
  /**
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {LoansService} systemService Loan Service.
   * @param {ActivatedRoute} route Activated Route.
   * @param {Router} router Router for navigation.
   * @param {SettingsService} settingsService Settings Service
   */
  constructor(private formBuilder: UntypedFormBuilder,
    private loanService: LoansService,
    private route: ActivatedRoute,
    private router: Router,
    private dateUtils: Dates,
    private settingsService: SettingsService,
     private notifyService : NotificationService) {
      this.loanId = this.route.parent.snapshot.params['loanId'];
       this.route.data.subscribe((data: { actionButtonData: any }) => {
            this.foreClosureAmount = data.actionButtonData.foreClosureAmount;
          });
    }

  ngOnInit() {
    this.maxDate = this.settingsService.businessDate;
    this.repaymentModeOptions = this.dataObject.repaymentModeOptions;
    this.createforeclosureForm();
    this.onChanges();
  }

  createforeclosureForm() {
    this.foreclosureForm = this.formBuilder.group({
      'transactionDate': [new Date(), Validators.required],
      'outstandingPrincipalPortion': [{value: '', disabled: true}],
      'outstandingInterestPortion': [{value: '', disabled: true}],
      'advanceAmount': [{value: '', disabled: true}],
//       'selfPrincipal':[{value: '', disabled: true}],
//       'selfInterestCharged':[{value: '', disabled: true}],
//       'partnerPrincipal':[{value: '', disabled: true}],
//       'partnerInterestCharged':[{value: '', disabled: true}],
//       'outstandingFeeChargesPortion': [{value: '', disabled: true}],
      'outstandingPenaltyChargesPortion': [{value: '', disabled: true}],
      'outstandingBounceChargesPortion': [{value: '', disabled: true}],
      'foreClosureAmount': [{value: this.foreClosureAmount, disabled: true}],
      'transactionAmount': [{value: '', disabled: true}],
      'receiptReferenceNumber':[''],
      'partnerTransferUtr':[''],
      'partnerTransferDate':[''],
      'repaymentMode':[''],
   //   'interestAccruedAfterDeath': '',
      'note': ''
    });
  }

  onChanges(): void {
    this.foreclosureForm.get('transactionDate').valueChanges.subscribe(val => {
      this.retrieveLoanForeclosureTemplate(val);
    });

  }

  retrieveLoanForeclosureTemplate(val: any) {
    const dateFormat = this.settingsService.dateFormat;
    const transactionDateFormatted = this.dateUtils.formatDate(val, dateFormat);
    const data = {
      command: 'foreclosure',
      dateFormat: this.settingsService.dateFormat,
      locale: this.settingsService.language.code,
      transactionDate: transactionDateFormatted
    };
    this.loanService.getForeclosureData(this.loanId, data)
    .subscribe((response: any) => {
      this.foreclosuredata = response;

      this.foreclosureForm.patchValue({
        outstandingPrincipalPortion: this.foreclosuredata.principalPortion,
        outstandingInterestPortion: this.foreclosuredata.interestPortion,
        advanceAmount: this.foreclosuredata.advanceAmount,
//         selfPrincipal:this.foreclosuredata.selfPrincipal,
//         selfInterestCharged:this.foreclosuredata.selfInterestCharged,
//         partnerPrincipal:this.foreclosuredata.partnerPrincipal,
//         partnerInterestCharged:this.foreclosuredata.partnerInterestCharged,
        outstandingFeeChargesPortion: this.foreclosuredata.feeChargesPortion,
        outstandingPenaltyChargesPortion: this.foreclosuredata.penaltyChargesPortion,
        outstandingBounceChargesPortion: this.foreclosuredata.bounceCharge,
        foreClosureChargesPortion: this.foreclosuredata.foreClosureChargesPortion,
      });
      if (this.foreclosuredata.unrecognizedIncomePortion) {
        this.foreclosureForm.patchValue({
          interestAccruedAfterDeath: this.foreclosuredata.unrecognizedIncomePortion
        });
      }
      this.calculateTransactionAmount();
      this.paymentTypes = this.foreclosuredata.paymentTypeOptions;
    });
  }

  calculateTransactionAmount() {
    let transactionAmount = 0;
    transactionAmount += parseFloat(this.foreclosuredata.principalPortion);
    transactionAmount += parseFloat(this.foreclosuredata.interestPortion);
    transactionAmount -= parseFloat(this.foreclosuredata.advanceAmount);
    transactionAmount += parseFloat(this.foreclosuredata.feeChargesPortion);
    transactionAmount += parseFloat(this.foreclosuredata.penaltyChargesPortion);
    transactionAmount += parseFloat(this.foreclosuredata.bounceCharge);
    this.foreclosureForm.patchValue({
      transactionAmount: transactionAmount
    });
  }

  submit() {
    const foreclosureFormData = this.foreclosureForm.value;
    const locale = this.settingsService.language.code;
    const dateFormat = this.settingsService.dateFormat;
    const prevTransactionDate = this.foreclosureForm.value.transactionDate;
    const prevPartnerTransferDate: Date = this.foreclosureForm.value.partnerTransferDate;
    if (foreclosureFormData.transactionDate instanceof Date) {
      foreclosureFormData.transactionDate = this.dateUtils.formatDate(prevTransactionDate, dateFormat);
    }
    if (foreclosureFormData.partnerTransferDate instanceof Date) {
               foreclosureFormData.partnerTransferDate = this.dateUtils.formatDate(prevPartnerTransferDate, dateFormat);
        }
    const data = {
      ...foreclosureFormData,
      dateFormat,
      locale
    };

    this.loanService.loanForeclosureData(this.loanId, data, 'foreclosure')
      .subscribe((response: any) => {
        this.router.navigate([`../../general`], { relativeTo: this.route });
        },error=>{
                console.log(error.error.errors,"response.error]");
                for(let i=0;i<error.error.errors.length;i++){
                this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                                     }
      });
    }

}

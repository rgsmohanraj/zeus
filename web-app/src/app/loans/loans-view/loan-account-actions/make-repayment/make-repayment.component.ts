/** Angular Imports */
import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators, UntypedFormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

/** Custom Services */
import { LoansService } from 'app/loans/loans.service';
import { SettingsService } from 'app/settings/settings.service';
import { Dates } from 'app/core/utils/dates';
import { NotificationService } from '../../../../notification.service'

/**
 * Loan Make Repayment Component
 */
@Component({
  selector: 'mifosx-make-repayment',
  templateUrl: './make-repayment.component.html',
  styleUrls: ['./make-repayment.component.scss']
})
export class MakeRepaymentComponent implements OnInit {

  @Input() dataObject: any;
  /** Loan Id */
  loanId: string;
  /** Payment Type Options */
  paymentTypes: any;
  /** Show payment details */
  showPaymentDetails = false;
  /** Minimum Date allowed. */
  minDate = new Date(2000, 0, 1);
  /** Maximum Date allowed. */
  maxDate = new Date();
  /** Repayment Loan Form */
  repaymentLoanForm: UntypedFormGroup;
  repaymentModeOptions:any;

  /**
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {LoansService} loanService Loan Service.
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
    }

  /**
   * Creates the repayment loan form
   * and initialize with the required values
   */
  ngOnInit() {
    this.maxDate = this.settingsService.businessDate;
    this.repaymentModeOptions = this.dataObject.repaymentModeOptions;

    this.createRepaymentLoanForm();
    this.setRepaymentLoanDetails();
  }

  /**
   * Creates the create close form.
   */
  createRepaymentLoanForm() {
    this.repaymentLoanForm = this.formBuilder.group({
      'transactionDate': [new Date(), Validators.required],
      'outStanding':[{value: '', disabled: true}] ,
      'advanceAmount': [{value: '', disabled: true}],
      'installmentNumber':[''],
      'expectedAmount':[{value: '', disabled: true}],
      'transactionAmount':['',Validators.required],
      'receiptReferenceNumber':[''],
      'partnerTransferUtr':[''],
      'partnerTransferDate':[''],
      'repaymentMode':[''],
//       'interest': [{value: '', disabled: true}],
//       'selfPrincipal': [{value: '', disabled: true}],
//       'selfInterestCharged': [{value: '', disabled: true}],
//       'partnerPrincipal': [{value: '', disabled: true}],
//       'partnerInterestCharged': [{value: '', disabled: true}],
//       'selfDue': [''],
//       'partnerDue': [''],
//       'paymentTypeId': '',
//       'note': ''


    });
  }

  setRepaymentLoanDetails() {
    this.paymentTypes = this.dataObject.paymentTypeOptions;
    this.repaymentLoanForm.patchValue({
      outStanding: this.dataObject.amount,
      advanceAmount: this.dataObject.advanceAmount,
      expectedAmount: this.dataObject.expectedAmount,
      installmentNumber:this.dataObject.installmentNumber,

    });
  }

  /**
   * Add payment detail fields to the UI.
   */
  addPaymentDetails() {
    this.showPaymentDetails = !this.showPaymentDetails;
    if (this.showPaymentDetails) {
      this.repaymentLoanForm.addControl('accountNumber', new UntypedFormControl(''));
      this.repaymentLoanForm.addControl('checkNumber', new UntypedFormControl(''));
      this.repaymentLoanForm.addControl('routingCode', new UntypedFormControl(''));
      this.repaymentLoanForm.addControl('receiptNumber', new UntypedFormControl(''));
      this.repaymentLoanForm.addControl('bankNumber', new UntypedFormControl(''));
    } else {
      this.repaymentLoanForm.removeControl('accountNumber');
      this.repaymentLoanForm.removeControl('checkNumber');
      this.repaymentLoanForm.removeControl('routingCode');
      this.repaymentLoanForm.removeControl('receiptNumber');
      this.repaymentLoanForm.removeControl('bankNumber');
    }
  }

  /** Submits the repayment form */
  submit() {
    console.log(this.repaymentLoanForm.value);
    const repaymentLoanFormData = this.repaymentLoanForm.value;
    const locale = this.settingsService.language.code;
    const dateFormat = this.settingsService.dateFormat;
    const prevTransactionDate: Date = this.repaymentLoanForm.value.transactionDate;
    const prevPartnerTransferDate: Date = this.repaymentLoanForm.value.partnerTransferDate;
      const installmentNumber =  this.repaymentLoanForm.value.installmentNumber;
    if (repaymentLoanFormData.transactionDate instanceof Date) {
      repaymentLoanFormData.transactionDate = this.dateUtils.formatDate(prevTransactionDate, dateFormat);
    }
    if (repaymentLoanFormData.partnerTransferDate instanceof Date) {
           repaymentLoanFormData.partnerTransferDate = this.dateUtils.formatDate(prevPartnerTransferDate, dateFormat);
    }
    const data = {
      ...repaymentLoanFormData,
      installmentNumber,
      dateFormat,
      locale
    };

    this.loanService.submitLoanActionButton(this.loanId, data, 'repayment')
      .subscribe((response: any) => {
        this.router.navigate(['../../transactions'], { relativeTo: this.route });
        },error=>{
                             console.log(error.error.errors,"response.error]");
                             for(let i=0;i<error.error.errors.length;i++){
                                     this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                                     }
    });
  }
  amountCalculation()
  {
  this.repaymentLoanForm.patchValue({  'expectedAmount': ((this.dataObject.amount-this.dataObject.advanceAmount)-this.repaymentLoanForm.value.transactionAmount)});

  }

}

/** Angular Imports */
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators } from '@angular/forms';
import { SettingsService } from 'app/settings/settings.service';

/** Custom Services */
import { LoansService } from '../../loans.service';

/**
 * Loans Account Details Step
 */
@Component({
  selector: 'mifosx-loans-account-details-step',
  templateUrl: './loans-account-details-step.component.html',
  styleUrls: ['./loans-account-details-step.component.scss']
})
export class LoansAccountDetailsStepComponent implements OnInit {

  /** Loans Account Template */
  @Input() loansAccountTemplate: any;

  /** Minimum date allowed. */
  minDate = new Date(2000, 0, 1);
  /** Maximum date allowed. */
  maxDate = new Date(2100, 0, 1);
  /** Product Data */
  productData: any;
//   /** Loan Officer Data */
//   loanOfficerOptions: any;
  /** Loan Purpose Options */
  loanPurposeOptions: any;
//   /** Fund Options */
//   fundOptions: any;
  /** Account Linking Options */
  accountLinkingOptions: any;
  /** For edit loan accounts form */
  isFieldOfficerPatched = false;
  /** Loans Account Details Form */
  loansAccountDetailsForm: UntypedFormGroup;

  /** Loans Account Template with product data  */
  @Output() loansAccountProductTemplate = new EventEmitter();
  /**
   * Sets loans account details form.
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {LoansService} loansService Loans Service.
   * @param {SettingsService} settingsService SettingsService
   */
  constructor(private formBuilder: UntypedFormBuilder,
    private loansService: LoansService,
    private settingsService: SettingsService) {
    this.createLoansAccountDetailsForm();
  }

  ngOnInit() {
    this.maxDate = this.settingsService.businessDate;
    this.buildDependencies();
    if (this.loansAccountTemplate) {
      this.productData = this.loansAccountTemplate.productOptions;
      if (this.loansAccountTemplate.loanProductId) {
        this.loansAccountDetailsForm.patchValue({
          'productId': this.loansAccountTemplate.loanProductId,
          'submittedOnDate': this.loansAccountTemplate.timeline.submittedOnDate && new Date(this.loansAccountTemplate.timeline.submittedOnDate),
//           'loanOfficerId': this.loansAccountTemplate.loanOfficerId,
          'loanPurposeId': this.loansAccountTemplate.loanPurposeId,
//           'fundId': this.loansAccountTemplate.fundId,
          'expectedDisbursementDate': this.loansAccountTemplate.timeline.expectedDisbursementDate && new Date(this.loansAccountTemplate.timeline.expectedDisbursementDate),
          'externalId': this.loansAccountTemplate.externalId
        });
      }
    }
  }

  /**
   * Creates loans account details form.
   */
  createLoansAccountDetailsForm() {
    this.loansAccountDetailsForm = this.formBuilder.group({
      'productId': ['', Validators.required],
//       'loanOfficerId': [''],
      'loanPurposeId': [''],
//       'fundId': [''],
      'submittedOnDate': [new Date(), Validators.required],
      'expectedDisbursementDate': ['', Validators.required],
      'externalId': ['', Validators.required],
      'linkAccountId': [''],
      'createStandingInstructionAtDisbursement': [''],
//        "selfShare": 70,
//                   "partnerShare": 30
    });
  }

  /**
   * Fetches loans account product template on productId value changes
   */
  buildDependencies() {
    const entityId = (this.loansAccountTemplate.clientId) ? this.loansAccountTemplate.clientId : this.loansAccountTemplate.group.id;
    const isGroup = (this.loansAccountTemplate.clientId) ? false : true;
    this.loansAccountDetailsForm.get('productId').valueChanges.subscribe((productId: string) => {
      this.loansService.getLoansAccountTemplateResource(entityId, isGroup, productId).subscribe((response: any) => {
        this.loansAccountProductTemplate.emit(response);
//         this.loanOfficerOptions = response.loanOfficerOptions;
        this.loanPurposeOptions = response.loanPurposeOptions;
//         this.fundOptions = response.fundOptions;
        this.accountLinkingOptions = response.accountLinkingOptions;
        if (response.createStandingInstructionAtDisbursement) {
          this.loansAccountDetailsForm.get('createStandingInstructionAtDisbursement').patchValue(response.createStandingInstructionAtDisbursement);
        }
      });
    });
  }

  /**
   * Returns loans account details form value.
   */
  get loansAccountDetails() {
    return this.loansAccountDetailsForm.value;
  }

}

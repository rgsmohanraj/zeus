import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LoansService } from '../loans.service';
import { LoansAccountDetailsStepComponent } from '../loans-account-stepper/loans-account-details-step/loans-account-details-step.component';
import { LoansAccountTermsStepComponent } from '../loans-account-stepper/loans-account-terms-step/loans-account-terms-step.component';
import { LoansAccountChargesStepComponent } from '../loans-account-stepper/loans-account-charges-step/loans-account-charges-step.component';

/** Custom Services */
import { SettingsService } from 'app/settings/settings.service';
import { Dates } from 'app/core/utils/dates';
import { NotificationService } from '../../notification.service';

/**
 * Edit Loans
 */
@Component({
  selector: 'mifosx-edit-loans-account',
  templateUrl: './edit-loans-account.component.html',
  styleUrls: ['./edit-loans-account.component.scss']
})
export class EditLoansAccountComponent implements OnInit {

  @ViewChild(LoansAccountDetailsStepComponent, { static: true }) loansAccountDetailsStep: LoansAccountDetailsStepComponent;
  @ViewChild(LoansAccountTermsStepComponent, { static: true }) loansAccountTermsStep: LoansAccountTermsStepComponent;
  @ViewChild(LoansAccountChargesStepComponent, { static: true }) loansAccountChargesStep: LoansAccountChargesStepComponent;

  loansAccountAndTemplate: any;
  /** Loans Account Product Template */
  loansAccountProductTemplate: any;
  /** Collateral Options */
  collateralOptions: any;
  /** Loan Id */
  loanId: any;

  /**
   * Sets loans account edit form.
   * @param {route} ActivatedRoute Activated Route.
   * @param {router} Router Router.
   * @param {Dates} dateUtils Date Utils
   * @param {loansService} LoansService Loans Service
   * @param {SettingsService} settingsService Settings Service
   */
  constructor(private route: ActivatedRoute,
    private router: Router,
    private dateUtils: Dates,
    private loansService: LoansService,
    private settingsService: SettingsService,
      private notifyService: NotificationService
  ) {
    this.route.data.subscribe((data: { loansAccountAndTemplate: any }) => {
      this.loansAccountAndTemplate = data.loansAccountAndTemplate;
    });
    this.loanId = this.route.parent.snapshot.params['loanId'];
  }

  ngOnInit() {
  }

  /**
   * Sets loans account product template and collateral template
   * @param {any} $event API response
   */
  setTemplate($event: any) {
    this.loansAccountProductTemplate = $event;
    this.loansService.getLoansCollateralTemplateResource(this.loansAccountProductTemplate.loanProductId).subscribe((response: any) => {
      this.collateralOptions = response.loanCollateralOptions;
    });
  }

  /** Get Loans Account Details Form Data */
  get loansAccountDetailsForm() {
    return this.loansAccountDetailsStep.loansAccountDetailsForm;
  }

  /** Get Loans Account Terms Form Data */
  get loansAccountTermsForm() {
    return this.loansAccountTermsStep.loansAccountTermsForm;
  }

  /** Checks wheter all the forms in different steps are valid and not pristine */
  get loansAccountFormValidAndNotPristine() {
    return (
      this.loansAccountDetailsForm.valid &&
      this.loansAccountTermsForm.valid &&
      (
        !this.loansAccountDetailsForm.pristine ||
        !this.loansAccountTermsForm.pristine ||
        !this.loansAccountChargesStep.pristine
      )
    );
  }

  /** Retrieves Data of all forms except Currency to submit the data */
  get loansAccount() {
    return {
      ...this.loansAccountDetailsStep.loansAccountDetails,
      ...this.loansAccountTermsStep.loansAccountTerms,
      ...this.loansAccountChargesStep.loansAccountCharges,
    };
  }

  /**
   * Submits Data to create loan account
   */
  submit() {
  console.log(this.loansAccount,"this.loansAccount");
    const locale = this.settingsService.language.code;
    const dateFormat = this.settingsService.dateFormat;
    const loanType = 'individual';
    const loansAccountData = {
      ...this.loansAccount,
      clientId: this.loansAccountAndTemplate.clientId,
      charges: this.loansAccount.charges.map((charge: any) => ({
        chargeId: charge.id,
        amount: charge.amount,
        selfShare: charge.selfShare,
        partnerShare: charge.partnerShare,
        dueDate: charge.dueDate && this.dateUtils.formatDate(charge.dueDate, dateFormat),
      })),
      foreclosureCharges:this.loansAccount.foreclosureCharges.map((foreclosureCharge:any)=>({
                                chargeId: foreclosureCharge.id,
                                amount: foreclosureCharge.amount,
                                selfShare: foreclosureCharge.selfShare,
                                partnerShare: foreclosureCharge.partnerShare,

                        })),
            overdueCharges:this.loansAccount.overdueCharges.map((overdueCharge:any)=>({
                                chargeId: overdueCharge.id,
                                amount: overdueCharge.amount,
                                selfShare: overdueCharge.selfShare,
                                partnerShare: overdueCharge.partnerShare,
                              })),
      collateral: this.loansAccount.collateral.map((collateralEle: any) => ({
        type: collateralEle.type,
        value: collateralEle.value,
        description: collateralEle.description
      })),
      interestChargedFromDate: this.dateUtils.formatDate(this.loansAccount.interestChargedFromDate, dateFormat),
      repaymentsStartingFromDate: this.dateUtils.formatDate(this.loansAccount.repaymentsStartingFromDate, dateFormat),
      submittedOnDate: this.dateUtils.formatDate(this.loansAccount.submittedOnDate, dateFormat),
      expectedDisbursementDate: this.dateUtils.formatDate(this.loansAccount.expectedDisbursementDate, dateFormat),
      dateFormat,
      locale,
      loanType
    };

    if (loansAccountData.syncRepaymentsWithMeeting) {
      loansAccountData.calendarId = this.loansAccountProductTemplate.calendarOptions[0].id;
      delete loansAccountData.syncRepaymentsWithMeeting;
    }

    if (loansAccountData.recalculationRestFrequencyDate) {
      loansAccountData.recalculationRestFrequencyDate = this.dateUtils.formatDate(this.loansAccount.recalculationRestFrequencyDate, dateFormat);
    }

    if (loansAccountData.interestCalculationPeriodType === 0) {
      loansAccountData.allowPartialPeriodInterestCalcualtion = false;
    }
    if (!(loansAccountData.isFloatingInterestRate === false)) {
      delete loansAccountData.isFloatingInterestRate;
    }

    this.loansService.updateLoansAccount(this.loanId, loansAccountData).subscribe((response: any) => {
     console.log(loansAccountData,"loansAccountData");
      this.router.navigate(['../'], { relativeTo: this.route });
      },error=>{
                   console.log(error.error.errors,"response.error]");
                   for(let i=0;i<error.error.errors.length;i++){
                           this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                           }
    });
  }

}

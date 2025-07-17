/** Angular Imports */
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

/** Custom Services */
import { LoansService } from '../loans.service';
import { SettingsService } from 'app/settings/settings.service';

/** Step Components */
import { LoansAccountDetailsStepComponent } from '../loans-account-stepper/loans-account-details-step/loans-account-details-step.component';
import { LoansAccountTermsStepComponent } from '../loans-account-stepper/loans-account-terms-step/loans-account-terms-step.component';
import { LoansAccountChargesStepComponent } from '../loans-account-stepper/loans-account-charges-step/loans-account-charges-step.component';
import { Dates } from 'app/core/utils/dates';

import { NotificationService } from '../../notification.service';

/**
 * Create loans account
 */
@Component({
  selector: 'mifosx-create-loans-account',
  templateUrl: './create-loans-account.component.html',
  styleUrls: ['./create-loans-account.component.scss']
})
export class CreateLoansAccountComponent implements OnInit {

  /** Imports all the step component */
  @ViewChild(LoansAccountDetailsStepComponent, { static: true }) loansAccountDetailsStep: LoansAccountDetailsStepComponent;
  @ViewChild(LoansAccountTermsStepComponent, { static: true }) loansAccountTermsStep: LoansAccountTermsStepComponent;
  @ViewChild(LoansAccountChargesStepComponent, { static: true }) loansAccountChargesStep: LoansAccountChargesStepComponent;

  /** Loans Account Template */
  loansAccountTemplate: any;
  /** Loans Account Product Template */
  loansAccountProductTemplate: any;
  /** Collateral Options */
  collateralOptions: any;
  /** Multi Disburse Loan */
  multiDisburseLoan: any;

  /**
   * Sets loans account create form.
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
    this.route.data.subscribe((data: { loansAccountTemplate: any }) => {
      this.loansAccountTemplate = data.loansAccountTemplate;
    });
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
    const entityId = (this.loansAccountTemplate.clientId) ? this.loansAccountTemplate.clientId : this.loansAccountTemplate.group.id;
    const isGroup = (this.loansAccountTemplate.clientId) ? false : true;
    const productId = this.loansAccountProductTemplate.loanProductId;
    this.loansService.getLoansAccountTemplateResource(entityId, isGroup, productId).subscribe((response: any) => {
      this.multiDisburseLoan = response.multiDisburseLoan;
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

  /** Checks wheter all the forms in different steps are valid or not */
  get loansAccountFormValid() {
    return (
      this.loansAccountDetailsForm.valid &&
      this.loansAccountTermsForm.valid
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
    const loansAccountData = {
      ...this.loansAccount,
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

                        })), bounceCharge:this.loansAccount.bounceCharge.map((overdueCharge:any)=>({
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
      disbursementData: this.loansAccount.disbursementData.map((item: any) => ({
        expectedDisbursementDate: this.dateUtils.formatDate(item.expectedDisbursementDate, dateFormat),
        principal: item.principal
      })),
      interestChargedFromDate: this.dateUtils.formatDate(this.loansAccount.interestChargedFromDate, dateFormat),
      repaymentsStartingFromDate: this.dateUtils.formatDate(this.loansAccount.repaymentsStartingFromDate, dateFormat),
      submittedOnDate: this.dateUtils.formatDate(this.loansAccount.submittedOnDate, dateFormat),
      expectedDisbursementDate: this.dateUtils.formatDate(this.loansAccount.expectedDisbursementDate, dateFormat),
      dateFormat,
      locale,
    };
    if (this.loansAccountTemplate.clientId) {
      loansAccountData.clientId = this.loansAccountTemplate.clientId;
      loansAccountData.loanType = 'individual';
    } else {
      loansAccountData.groupId = this.loansAccountTemplate.group.id;
      loansAccountData.loanType = 'group';
    }

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
    if (!(this.multiDisburseLoan)) {
      delete loansAccountData.disbursementData;
    }

    this.loansService.createLoansAccount(loansAccountData).subscribe((response: any) => {
console.log(loansAccountData,"loansAccountData");
//         this.notifyService.showSuccess("Loan created","Success");
      this.router.navigate(['../', response.resourceId], { relativeTo: this.route });
      },error=>{
             console.log(error.error.errors,"response.error]");
             for(let i=0;i<error.error.errors.length;i++){
                     this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                     }
    });
  }

}

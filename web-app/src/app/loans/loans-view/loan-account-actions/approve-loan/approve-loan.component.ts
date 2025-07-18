/** Angular Imports. */
import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators, FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Dates } from 'app/core/utils/dates';

/** Custom Services. */
import { LoansService } from 'app/loans/loans.service';
import { SettingsService } from 'app/settings/settings.service';

import { NotificationService } from '../../../../notification.service';

/**
 * Approve Loan component.
 */
@Component({
  selector: 'mifosx-approve-loan',
  templateUrl: './approve-loan.component.html',
  styleUrls: ['./approve-loan.component.scss']
})
export class ApproveLoanComponent implements OnInit {

  /** Approve Loan form. */
  approveLoanForm: UntypedFormGroup;
  /** Loan data. */
  loanData: any = new Object();
  /** Association Data */
  associationData: any;
  /** Minimum Date allowed. */
  minDate = new Date(2000, 0, 1);
  /** Loan Id */
  loanId: any;

  /**
   * Retrieve data from `Resolver`.
   * @param formBuilder Form Builder.
   * @param route Activated Route.
   * @param dateUtils Date Utils.
   * @param loanService Loan Service.
   * @param router Router.
   * @param {SettingsService} settingsService Settings Service
   */
  constructor(private formBuilder: UntypedFormBuilder,
    private route: ActivatedRoute,
    private dateUtils: Dates,
    private loanService: LoansService,
    private router: Router,
    private settingsService: SettingsService,
      private notifyService : NotificationService) {
    this.route.data.subscribe((data: { actionButtonData: any }) => {
      this.loanData = data.actionButtonData;
    });
    this.loanId = this.route.parent.snapshot.params['loanId'];
  }

  ngOnInit() {
    this.setApproveLoanForm();
    this.loanService.getApproveAssociationsDetails(this.loanId).subscribe((response: any) => {
      this.associationData = response;
      this.approveLoanForm.patchValue({
        'expectedDisbursementDate': new Date(response.timeline.expectedDisbursementDate)
      });
    });
  }

  /**
   * Set Approve Loan form.
   */
  setApproveLoanForm() {
    this.approveLoanForm = this.formBuilder.group({
      'approvedOnDate': [this.loanData.approvalDate && new Date(this.loanData.approvalDate), Validators.required],
      'expectedDisbursementDate': [''],
      'approvedLoanAmount': [this.loanData.approvalAmount, Validators.required],
      'note': ['']
    });
  }

  /**
   * Submits Approve form.
   */
  submit() {
    const approveLoanFormData = this.approveLoanForm.value;
    const locale = this.settingsService.language.code;
    const dateFormat = this.settingsService.dateFormat;
    const approvedOnDate = this.approveLoanForm.value.approvedOnDate;
    const expectedDisbursementDate = this.approveLoanForm.value.expectedDisbursementDate;
    if (approveLoanFormData.approvedOnDate instanceof Date) {
      approveLoanFormData.approvedOnDate = this.dateUtils.formatDate(approvedOnDate, dateFormat);
    }
    if (approveLoanFormData.expectedDisbursementDate instanceof Date) {
      approveLoanFormData.expectedDisbursementDate = this.dateUtils.formatDate(expectedDisbursementDate, dateFormat);
    }
    const data = {
      ...approveLoanFormData,
      dateFormat,
      locale
    };
    this.loanService.loanActionButtons(this.loanId, 'approve', data).subscribe((response: any) => {
      this.router.navigate(['../../general'], { relativeTo: this.route });
      },error=>{
                     console.log(error.error.errors,"response.error]");
                     for(let i=0;i<error.error.errors.length;i++){
                             this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                             }
    });
  }

}

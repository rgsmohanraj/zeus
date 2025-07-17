/** Angular Imports */
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';

/** Custom Services */
import { LoansService } from '../loans.service';
import { NotificationService } from '../../notification.service';
import { SettingsService } from 'app/settings/settings.service';

/** Custom Buttons Configuration */
import { LoansAccountButtonConfiguration } from './loan-accounts-button-config';

/** Dialog Components */
import { ConfirmationDialogComponent } from '../../shared/confirmation-dialog/confirmation-dialog.component';
import { DeleteDialogComponent } from 'app/shared/delete-dialog/delete-dialog.component';

@Component({
  selector: 'mifosx-loans-view',
  templateUrl: './loans-view.component.html',
  styleUrls: ['./loans-view.component.scss']
})
export class LoansViewComponent implements OnInit {

  /** Loan Details Data */
  loanDetailsData: any;
  /** Loan Datatables */
  loanDatatables: any;
  /** Recalculate Interest */
  recalculateInterest: any;
  /** Status */
  status: string;
  /** Loan Id */
  loanId: string;
  /** Client Id */
  clientId: any;
  /** Button Configuration */
  buttonConfig: LoansAccountButtonConfiguration;

  isPennyDropEnabled: boolean;

  isBankDisbursementEnabled: boolean;

  constructor(private route: ActivatedRoute,
    private router: Router,
    public loansService: LoansService,
    public dialog: MatDialog, private settingsService: SettingsService,
    private notifyService: NotificationService) {
    this.route.data.subscribe((data: { loanDetailsData: any, loanDatatables: any }) => {
      this.loanDetailsData = data.loanDetailsData;
      this.loanDatatables = data.loanDatatables;

    });

    this.loanId = this.route.snapshot.params['loanId'];
    this.clientId = this.loanDetailsData.clientId;
  }

  ngOnInit() {

    this.recalculateInterest = this.loanDetailsData.recalculateInterest || true;
    this.status = this.loanDetailsData.status.value;
    this.isPennyDropEnabled = this.loanDetailsData.loanProductData.isPennyDropEnabled;
    this.isBankDisbursementEnabled = this.loanDetailsData.loanProductData.isBankDisbursementEnabled;
    this.setConditionalButtons();
  }

  // Defines the buttons based on the status of the loan account
  setConditionalButtons() {
    this.buttonConfig = new LoansAccountButtonConfiguration(this.status, this.isPennyDropEnabled,this.isBankDisbursementEnabled);

    if (this.status === 'Submitted and pending approval') {

//       this.buttonConfig.addOption({
//         name: (this.loanDetailsData.loanOfficerName ? 'Change Loan Officer' : 'Assign Loan Officer'),
//         taskPermissionName: 'DISBURSE_LOAN'
//       });

      if (this.loanDetailsData.isVariableInstallmentsAllowed) {
        this.buttonConfig.addOption({
          name: 'Edit Repayment Schedule',
          taskPermissionName: 'ADJUST_REPAYMENT_SCHEDULE'
        });
      }

    } else if (this.status === 'Approved') {

//       this.buttonConfig.addButton({
//         name: (this.loanDetailsData.loanOfficerName ? 'Change Loan Officer' : 'Assign Loan Officer'),
//         icon: 'fa fa-user',
//         taskPermissionName: 'DISBURSE_LOAN'
//       });

    } else if (this.status === 'Active') {

      if (this.loanDetailsData.canDisburse) {
        this.buttonConfig.addButton({
          name: 'Disburse',
          icon: 'fa fa-flag',
          taskPermissionName: 'DISBURSE_LOAN'
        });
        //         this.buttonConfig.addButton({
        //           name: 'Disburse To Savings',
        //           icon: 'fa fa-flag',
        //           taskPermissionName: 'DISBURSETOSAVINGS_LOAN'
        //         });
      }

      // loan officer not assigned to loan, below logic
      // helps to display otherwise not
//       if (!this.loanDetailsData.loanOfficerName) {
//         this.buttonConfig.addButton({
//           name: 'Assign Loan Officer',
//           icon: 'fa fa-user',
//           taskPermissionName: 'UPDATELOANOFFICER_LOAN'
//         });
//       }

      if (this.recalculateInterest) {
        this.buttonConfig.addButton({
          name: 'Prepay Loan',
          icon: 'fa fa-money',
          taskPermissionName: 'REPAYMENT_LOAN'
        });
      }

    }
  }

  loanAction(button: string) {

    switch (button) {
      case 'Recover From Guarantor':
        this.recoverFromGuarantor();
        break;
      case 'Delete':
        this.deleteLoanAccount();
        break;
      case 'Modify Application':
        this.router.navigate(['edit-loans-account'], { relativeTo: this.route });
        break;
      case 'Refund':
        const queryParams: any = { loanId: this.loanId, accountType: 'fromloans' };
        this.router.navigate(['transfer-funds/make-account-transfer'], { relativeTo: this.route, queryParams: queryParams });
        break;

      case 'Initiate Penny Drop':
      this.initiatePennyDrop();
        break;

      case 'Initiate Disbursement':
        this.initiateDisbursement();
        break;

      default:

        this.router.navigate(['actions', button], { relativeTo: this.route });
        break;
    }
  }

  /**
   * Recover from guarantor action
   */
  private recoverFromGuarantor() {
    const recoverFromGuarantorDialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: { heading: 'Recover from Guarantor', dialogContext: 'Are you sure you want recover from Guarantor', type: 'Mild' }
    });

   recoverFromGuarantorDialogRef.afterClosed().subscribe((response: any) => {
      if (response.confirm) {
        this.loansService.loanActionButtons(this.loanId, 'recoverGuarantees').subscribe(() => {
          this.reload();
        });
      }
    });
  }

  /**
   * Delete loan Account
   */
  private deleteLoanAccount() {
    const deleteGuarantorDialogRef = this.dialog.open(DeleteDialogComponent, {
      data: { deleteContext: `with loan id: ${this.loanId}` }
    });
    deleteGuarantorDialogRef.afterClosed().subscribe((response: any) => {
      if (response.delete) {
        this.loansService.deleteLoanAccount(this.loanId).subscribe(() => {
          this.router.navigate(['../../'], { relativeTo: this.route });
        });
      }
    });
  }

  /**
   * Refetches data for the component
   * TODO: Replace by a custom reload component instead of hard-coded back-routing.
   */
  private reload() {
    const clientId = this.clientId;
    const url: string = this.router.url;
    this.router.navigateByUrl(`/clients/${clientId}/loans-accounts`, { skipLocationChange: true })
      .then(() => this.router.navigate([url]));
  }

  private initiatePennyDrop() {

    this.loansService.initiatepennyDrop(this.loanId)
      .subscribe((response: any) => {
        console.log(response, "response.error]");
        if (response.statusCodeValue === 200) {
          this.notifyService.showSuccess(response.body, 'Success');
          this.pennydropReload();
        } else {
          let errors = response.body.split(',');
          errors.forEach(error => {
            this.notifyService.showError(error, 'Failed');
          })
          // this.pennydropReload();
        }
      }, error => {
        console.log(error.error.errors, "response.error]");
       // this.pennydropReload();
      });
  }


  private initiateDisbursement() {
    const pennyDropErrorRecord = this.loanDetailsData.bankTranscationData.pennyDropTransaction;
    if (pennyDropErrorRecord && pennyDropErrorRecord.length != 0 && pennyDropErrorRecord[0].action === 'FAILURE') {
      let message =  "Penny Drop has Failed due to '"+ pennyDropErrorRecord[0].reason +"'. Please confirm to disburse?"
      const disbursementDialog = this.dialog.open(ConfirmationDialogComponent, {
        data: { heading: 'Initiate Disbursement', dialogContext: message, type: 'Basic'}});
              disbursementDialog.afterClosed().subscribe((response: any) => {
                if (response.confirm) {this.triggerDisbursementService();}
              });
    } else {
      this.triggerDisbursementService();
    }
  }

  private triggerDisbursementService() {
    this.loansService.initiateDisbursement(this.loanId)
      .subscribe((response: any) => {
        console.log(response, "response.error]");
        if (response.statusCodeValue === 200) {
          this.notifyService.showSuccess(response.body, 'Success');
          this.pennydropReload();
        } else {
          let errors = response.body.split(',');
          errors.forEach(error => {
            this.notifyService.showError(error, 'Failed');
            // this.pennydropReload();
          })
        }
      }, error => {
        console.log(error.error.errors, "response.error]");
       // this.pennydropReload();
      });

  }

      pennydropReload() {
            const clientId = this.loanDetailsData.clientId;
            const loansid = this.loanDetailsData.id;
            const url: string = this.router.url;
            this.router.navigateByUrl(`/clients/${clientId}/loans-accounts`, {skipLocationChange: true})
                .then(() => this.router.navigate([url]));
        }
}

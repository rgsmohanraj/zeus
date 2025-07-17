/** Angular Imports */
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacyPaginator as MatPaginator } from '@angular/material/legacy-paginator';
import { MatSort } from '@angular/material/sort';
import { MatLegacyTableDataSource as MatTableDataSource, MatLegacyTable as MatTable } from '@angular/material/legacy-table';

/** Custom Services */
import { LoansService } from 'app/loans/loans.service';
import { SettingsService } from 'app/settings/settings.service';

/** Custom Dialogs */
import { FormDialogComponent } from 'app/shared/form-dialog/form-dialog.component';
import { DeleteDialogComponent } from 'app/shared/delete-dialog/delete-dialog.component';
import { ConfirmationDialogComponent } from 'app/shared/confirmation-dialog/confirmation-dialog.component';

/** Custom Models */
import { FormfieldBase } from 'app/shared/form-dialog/formfield/model/formfield-base';
import { InputBase } from 'app/shared/form-dialog/formfield/model/input-base';
import { DatepickerBase } from 'app/shared/form-dialog/formfield/model/datepicker-base';
import { Dates } from 'app/core/utils/dates';

@Component({
  selector: 'mifosx-charges-tab',
  templateUrl: './charges-tab.component.html',
  styleUrls: ['./charges-tab.component.scss']
})
export class ChargesTabComponent implements OnInit {

//   colendingFeesDisplayedColumns: string[] = ['colendingFees', 'selfFees', 'partnerFees'];
//   colendingChargesDisplayedColumns: string[] = ['colendingCharge', 'selfCharge', 'partnerCharge'];

  /** Loan Details Data */
  loanDetails: any;
  colendingChargeTabData:Array<any>=[];
  colendingFeesTabData:Array<any>=[];
  /** Charges Data */
  chargesData: any;
  feeAndChargeData:any;
//   newFeesAndCharges:any;
  /** Status */
  status: any;

  feeAndChargeColumns: string[] = ['feeList','selfFeeAmount','partnerFeeAmount','totalFeeAmount','selfGst','partnerGst','totalGst','total'];
  /** Columns to be displayed in charges table. */
  displayedColumns: string[] = ['name', 'feepenalty', 'paymentdueat',  'calculationtype', 'due','dueasof', 'self', 'partner','paid', 'waived', 'outstanding', 'actions'];
  /** Data source for charges table. */
  dataSource: MatTableDataSource<any>;

  /** Paginator for charges table. */
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  /** Sorter for charges table. */
  @ViewChild(MatSort, { static: true }) sort: MatSort;

  /**
   * Retrieves the loans data from `resolve`.
   * @param {ActivatedRoute} route Activated Route.
   * @param {SettingsService} settingsService Settings Service
   */
  constructor(private loansService: LoansService,
              private route: ActivatedRoute,
              private dateUtils: Dates,
              private router: Router,
              public dialog: MatDialog,
              private settingsService: SettingsService) {
    this.route.parent.data.subscribe(( data: { loanDetailsData: any }) => {
      this.loanDetails = data.loanDetailsData;
//        this.feeAndChargeData=data.loanDetailsData.disbursementSummaries;
       this.feeAndChargeData = data.loanDetailsData.charges;
       console.log("feeAndChargeData",this.feeAndChargeData);
    });
  }


  ngOnInit() {
    this.chargesData = this.loanDetails.charges;
    this.status = this.loanDetails.status.value;
    let actionFlag;
    this.chargesData.forEach((element: any) => {
      if (element.paid || element.waived || element.chargeTimeType.value === 'Disbursement' || this.loanDetails.status.value !== 'Active') {
        actionFlag = true;
      } else {
        actionFlag = false;
      }
      element.actionFlag = actionFlag;
    });
    this.dataSource = new MatTableDataSource(this.chargesData);
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;

//    for(let i=0;i<=this.chargesData.length;i++)
//    {
//    for(let j=0;j<=this.loanDetails.loanProductData.colendingCharges.length;j++)
//    {
//    if(this.chargesData[i].name==this.loanDetails.loanProductData.colendingCharges[j].name)
//    {
//     console.log(this.chargesData[i].name,"this.chargesData[i].name")
//    this.colendingChargeTabData.push(this.chargesData[i].name);
//
//    }
//  }
//   for(let k=0;k<=this.loanDetails.loanProductData.colendingFees.length;k++)
//      {
//      if(this.loanDetails.charges[i].name==this.loanDetails.loanProductData.colendingFees[k].name)
//      {
//      console.log(this.loanDetails.loanProductData.colendingFees[k].name,"this.loanDetails.loanProductData.colendingFees[k].name")
//      this.colendingFeesTabData.push(this.loanDetails.loanProductData.colendingFees[k]);
//
//      }
//    }
// }



  }

  /**
   * Pays the charge.
   * @param {any} chargeId Charge Id
   */
  payCharge(chargeId: any) {
    const formfields: FormfieldBase[] = [
      new InputBase({
        controlName: 'amount',
        label: 'Amount',
        value: '',
        type: 'number',
        required: true
      }),
      new DatepickerBase({
        controlName: 'dueDate',
        label: 'Payment Date',
        value: '',
        type: 'date',
        required: true
      })
    ];
    const data = {
      title: `Pay Charge ${chargeId}`,
      layout: { addButtonText: 'Confirm' },
      formfields: formfields
    };
    const payChargeDialogRef = this.dialog.open(FormDialogComponent, { data });
    payChargeDialogRef.afterClosed().subscribe((response: any) => {
      if (response.data) {
        const locale = 'en';
        const dateFormat = this.settingsService.dateFormat;
        const dataObject = {
          ...response.data.value,
          dueDate: this.dateUtils.formatDate(response.data.value.dueDate, dateFormat),
          dateFormat,
          locale
        };
        this.loansService.executeLoansAccountChargesCommand(this.loanDetails.id, 'paycharge', dataObject, chargeId)
          .subscribe(() => {
            this.reload();
          });
      }
    });
  }

  /**
   * Waive's the charge
   * @param {any} chargeId Charge Id
   */
  waiveCharge(chargeId: any) {
    const waiveChargeDialogRef = this.dialog.open(ConfirmationDialogComponent, { data: { heading: 'Waive Charge', dialogContext: `Are you sure you want to waive charge with id: ${chargeId}`, type: 'Basic' } });
    waiveChargeDialogRef.afterClosed().subscribe((response: any) => {
      if (response.confirm) {
        this.loansService.executeLoansAccountChargesCommand(this.loanDetails.id, 'waive', {}, chargeId)
          .subscribe(() => {
            this.reload();
          });
      }
    });
  }

  /**
   * Edits the charge
   * @param {any} charge Charge
   */
  editCharge(charge: any) {
    const formfields: FormfieldBase[] = [
      new InputBase({
        controlName: 'amount',
        label: 'Amount',
        value: charge.amount || charge.amountOrPercentage,
        type: 'number',
        required: true
      })
    ];
    const data = {
      title: `Edit Charge ${charge.id}`,
      layout: { addButtonText: 'Confirm' },
      formfields: formfields
    };
    const editChargeDialogRef = this.dialog.open(FormDialogComponent, { data });
    editChargeDialogRef.afterClosed().subscribe((response: any) => {
      if (response.data) {
        const locale = this.settingsService.language.code;
        const dateFormat = this.settingsService.dateFormat;
        const dataObject = {
          ...response.data.value,
          dateFormat,
          locale
        };
        this.loansService.editLoansAccountCharge(this.loanDetails.id, dataObject, charge.id)
          .subscribe(() => {
            this.reload();
          });
      }
    });
  }

  /**
   * Deletes the charge
   * @param {any} chargeId Charge Id
   */
  deleteCharge(chargeId: any) {
    const deleteChargeDialogRef = this.dialog.open(DeleteDialogComponent, {
      data: { deleteContext: `charge id:${chargeId}` }
    });
    deleteChargeDialogRef.afterClosed().subscribe((response: any) => {
      if (response.delete) {
        this.loansService.deleteLoansAccountCharge(this.loanDetails.id, chargeId)
          .subscribe(() => {
            this.reload();
          });
      }
    });
  }

  /**
   * Stops the propagation to view charge page.
   * @param $event Mouse Event
   */
  routeEdit($event: MouseEvent) {
    $event.stopPropagation();
  }

  /**
   * Refetches data fot the component
   * TODO: Replace by a custom reload component instead of hard-coded back-routing.
   */
  private reload() {
    const clientId = this.loanDetails.clientId;
    const url: string = this.router.url;
    this.router.navigateByUrl(`/clients/${clientId}/loans-accounts`, { skipLocationChange: true })
      .then(() => this.router.navigate([url]));
  }

}

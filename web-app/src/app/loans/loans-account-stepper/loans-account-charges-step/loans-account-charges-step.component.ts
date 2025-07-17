/** Angular Imports */
import { Component, OnInit, Input, OnChanges } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';

/** Dialog Components */
import { DeleteDialogComponent } from 'app/shared/delete-dialog/delete-dialog.component';
import { FormDialogComponent } from 'app/shared/form-dialog/form-dialog.component';
import { LoansAccountAddCollateralDialogComponent } from 'app/loans/custom-dialog/loans-account-add-collateral-dialog/loans-account-add-collateral-dialog.component';

/** Custom Services */
import { DatepickerBase } from 'app/shared/form-dialog/formfield/model/datepicker-base';
import { FormfieldBase } from 'app/shared/form-dialog/formfield/model/formfield-base';
import { InputBase } from 'app/shared/form-dialog/formfield/model/input-base';
import { SettingsService } from 'app/settings/settings.service';
import { Dates } from 'app/core/utils/dates';

/**
 * Recurring Deposit Account Charges Step
 */
@Component({
  selector: 'mifosx-loans-account-charges-step',
  templateUrl: './loans-account-charges-step.component.html',
  styleUrls: ['./loans-account-charges-step.component.scss']
})
export class LoansAccountChargesStepComponent implements OnInit, OnChanges {

  // @Input loansAccountProductTemplate: LoansAccountProductTemplate
  @Input() loansAccountProductTemplate: any;
  // @Imput loansAccountTemplate: LoansAccountTemplate
  @Input() loansAccountTemplate: any;
  // @Input() loansAccountFormValid: LoansAccountFormValid
  @Input() loansAccountFormValid: boolean;
  // @Input collateralOptionsL Collateral Options
  @Input() collateralOptions: any;

  /** Charges Data */
  chargeData: any;
  /** Charges Data Source */
  chargesDataSource: {}[] = [];
  /** Foreclosure Charges Data Source */
    foreclosureChargesDataSource: {}[] = [];
  /** Overdue Charges Data Source */
  overDueChargesDataSource: {}[] = [];
  /** Collateral Data Source */
  collateralDataSource: {}[] = [];
  /** Charges table columns */
  chargesDisplayedColumns: string[] = ['name', 'chargeCalculationType', 'amount', 'chargeTimeType', 'date', 'action'];
   /** Columns to be displayed in foreclosure charges table. */
    foreclosureChargesDisplayedColumns: string[] = ['name', 'type', 'amount', 'collectedon'];
  /** Columns to be displayed in overdue charges table. */
  overdueChargesDisplayedColumns: string[] = ['name', 'type', 'amount', 'collectedon'];
  /** Columns to be displayed in collateral table. */
  loanCollateralDisplayedColumns: string[] = ['type', 'value', 'description', 'action'];
  /** Component is pristine if there has been no changes by user interaction */
  pristine = true;
   /** Bounce Charges Data Source */
   bounceChargesDataSource: {}[] = [];
   bounceChargesDisplayedColumns: string[] = ['name', 'type', 'amount', 'collectedon'];

  /**
   * Loans Account Charges Form Step
   * @param {dialog} MatDialog Mat Dialog
   * @param {Dates} dateUtils Date Utils
   * @param {SettingsService} settingsService Settings Service
   */
  constructor(public dialog: MatDialog,
    private dateUtils: Dates,
    private settingsService: SettingsService) {
  }

  ngOnInit() {
    if ( this.loansAccountTemplate.charges) {
    console.log(this.loansAccountTemplate,"this.loansAccountTemplate");
      this.chargesDataSource = this.loansAccountTemplate.charges.map((charge: any) => ({ ...charge, id: charge.chargeId })) || [];

    }
  }

  /**
   * Executes on change of input values
   */
  ngOnChanges() {
    if (this.loansAccountProductTemplate) {
      this.chargeData = this.loansAccountProductTemplate.chargeOption;
      if (this.loansAccountProductTemplate.overdueCharges) {
        this.overDueChargesDataSource = this.loansAccountProductTemplate.overdueCharges;
      }
      if (this.loansAccountProductTemplate.foreClosureCharge) {
              this.foreclosureChargesDataSource = this.loansAccountProductTemplate.foreClosureCharge;
            }
            if (this.loansAccountProductTemplate.bounceCharge) {
              this.bounceChargesDataSource = this.loansAccountProductTemplate.bounceCharge;
            }
    }
  }

  /**
   * Add a charge
   */
  addCharge(charge: any) {
    this.chargesDataSource = this.chargesDataSource.concat([charge.value]);
    charge.value = '';
    this.pristine = false;
  }

 editForeclosureChargeAmount(charge: any) {
     const formfields: FormfieldBase[] = [
       new InputBase({
         controlName: 'amount',
         label: 'Amount',
         value: charge.amount,
         type: 'number',
         required: false
       }),
     ];
     const data = {
       title: 'Edit Foreclosure Charge Amount',
       layout: { addButtonText: 'Confirm' },
       formfields: formfields
     };
     const editNoteDialogRef = this.dialog.open(FormDialogComponent, { data });
     editNoteDialogRef.afterClosed().subscribe((response: any) => {
       if (response.data) {
         const newCharge = { ...charge, amount: response.data.value.amount };
         this.foreclosureChargesDataSource.splice(this.foreclosureChargesDataSource.indexOf(charge), 1, newCharge);
         this.foreclosureChargesDataSource = this.foreclosureChargesDataSource.concat([]);
       }
     });
     this.pristine = false;
   }



 editOverdueChargeAmount(charge: any) {
     const formfields: FormfieldBase[] = [
       new InputBase({
         controlName: 'amount',
         label: 'Amount',
         value: charge.amount,
         type: 'number',
         required: false
       }),
     ];
     const data = {
       title: 'Edit Overdue Charge Amount',
       layout: { addButtonText: 'Confirm' },
       formfields: formfields
     };
     const editNoteDialogRef = this.dialog.open(FormDialogComponent, { data });
     editNoteDialogRef.afterClosed().subscribe((response: any) => {
       if (response.data) {
         const newCharge = { ...charge, amount: response.data.value.amount };
         this.overDueChargesDataSource.splice(this.overDueChargesDataSource.indexOf(charge), 1, newCharge);
         this.overDueChargesDataSource = this.overDueChargesDataSource.concat([]);
         console.log("over due cec "+this.overDueChargesDataSource)
       }
     });
     this.pristine = false;
   }


  /**
   * Edits the Charge Amount
   * @param {any} charge Charge
   */
  editChargeAmount(charge: any) {
    const formfields: FormfieldBase[] = [
      new InputBase({
        controlName: 'amount',
        label: 'Amount',
        value: charge.amount,
        type: 'number',
        required: false
      }),
    ];
    const data = {
      title: 'Edit Charge Amount',
      layout: { addButtonText: 'Confirm' },
      formfields: formfields
    };
    const editNoteDialogRef = this.dialog.open(FormDialogComponent, { data });
    editNoteDialogRef.afterClosed().subscribe((response: any) => {
      if (response.data) {
      console.log(this.chargesDataSource,"this.chargesDataSource editChargeAmount");
        const newCharge = { ...charge, amount: response.data.value.amount };
        this.chargesDataSource.splice(this.chargesDataSource.indexOf(charge), 1, newCharge);
        this.chargesDataSource = this.chargesDataSource.concat([]);
        console.log(this.chargesDataSource,"this.chargesDataSource editChargeAmount");
      }
    });
    this.pristine = false;
  }

  /**
   * Edits the Charge Date
   * @param {any} charge Charge
   */
  editChargeDate(charge: any) {
    const formfields: FormfieldBase[] = [
      new DatepickerBase({
        controlName: 'date',
        label: 'Date',
        value: charge.dueDate || charge.feeOnMonthDay || '',
        type: 'date',
        required: false
      }),
    ];
    const data = {
      title: 'Edit Charge Date',
      layout: { addButtonText: 'Confirm' },
      formfields: formfields
    };
    const editNoteDialogRef = this.dialog.open(FormDialogComponent, { data });
    editNoteDialogRef.afterClosed().subscribe((response: any) => {
      if (response.data) {
      console.log(this.chargesDataSource,"this.chargesDataSource editChargeDate");
        let newCharge: any;
        const dateFormat = this.settingsService.dateFormat;
        const date = this.dateUtils.formatDate(response.data.value.date, dateFormat);
        switch (charge.chargeTimeType.value) {
          case 'Specified due date':
          case 'Weekly Fee':
            newCharge = { ...charge, dueDate: date };
            break;
          case 'Annual Fee':
            newCharge = { ...charge, feeOnMonthDay: date };
            break;
        }
        this.chargesDataSource.splice(this.chargesDataSource.indexOf(charge), 1, newCharge);
        this.chargesDataSource = this.chargesDataSource.concat([]);
        console.log(this.chargesDataSource,"this.chargesDataSource editChargeDate");
      }
    });
    this.pristine = false;
  }

  /**
   * Edits the Charge Fee Interval
   * @param {any} charge Charge
   */
  editChargeFeeInterval(charge: any) {
    const formfields: FormfieldBase[] = [
      new InputBase({
        controlName: 'feeInterval',
        label: 'Fee Interval',
        value: charge.feeInterval,
        type: 'text',
        required: false
      }),
    ];
    const data = {
      title: 'Edit Charge Fee Interval',
      layout: { addButtonText: 'Confirm' },
      formfields: formfields
    };
    const editNoteDialogRef = this.dialog.open(FormDialogComponent, { data });
    editNoteDialogRef.afterClosed().subscribe((response: any) => {
      if (response.data) {
      console.log(this.chargesDataSource,"this.chargesDataSource editChargeFeeInterval");
        const newCharge = { ...charge, feeInterval: response.data.value.feeInterval };
        this.chargesDataSource.splice(this.chargesDataSource.indexOf(charge), 1, newCharge);
        this.chargesDataSource = this.chargesDataSource.concat([]);
        console.log(this.chargesDataSource,"this.chargesDataSource editChargeFeeInterval");
      }
    });
    this.pristine = false;
  }

  /**
   * Delete a particular charge
   * @param charge Charge
   */
  deleteCharge(charge: any) {
    const deleteChargeDialogRef = this.dialog.open(DeleteDialogComponent, {
      data: { deleteContext: `charge ${charge.name}` }
    });
    deleteChargeDialogRef.afterClosed().subscribe((response: any) => {
      if (response.delete) {
        this.chargesDataSource.splice(this.chargesDataSource.indexOf(charge), 1);
        this.chargesDataSource = this.chargesDataSource.concat([]);
        this.pristine = false;
      }
    });
  }

  // TODO: Needs to be completed
  addCollateral() {
    const addCollateralDialogRef = this.dialog.open(LoansAccountAddCollateralDialogComponent, {
      data: { collateralOptions: this.collateralOptions }
    });
    addCollateralDialogRef.afterClosed().subscribe((response: any) => {
      if (response.addCollateralForm) {
        const collateralData = {
          type: response.addCollateralForm.value.type,
          value: response.addCollateralForm.value.value,
          description: response.addCollateralForm.value.description
        };
        this.collateralDataSource = this.collateralDataSource.concat(collateralData);

      }
    });
  }

  deleteCollateral(id: any) {
    const deleteCollateralDialogRef = this.dialog.open(DeleteDialogComponent, {
      data: { deleteContext: `collateral` }
    });
    deleteCollateralDialogRef.afterClosed().subscribe((response: any) => {
      if (response.delete) {
        this.collateralDataSource.splice(this.collateralDataSource.indexOf(id), 1);
        this.collateralDataSource = this.collateralDataSource.concat([]);
        this.pristine = false;
      }
    });
  }

  /**
   * Returns Loans Account Charges and Collateral Form
   */
  get loansAccountCharges() {
  console.log(this.chargesDataSource,"this.chargesDataSource account charges");
    return console.log('this.overDueChargesDataSource  '+this.overDueChargesDataSource), {

      charges: this.chargesDataSource,
      overdueCharges: this.overDueChargesDataSource,
      foreclosureCharges: this.foreclosureChargesDataSource,
      collateral: this.collateralDataSource,
      bounceCharge:this.bounceChargesDataSource
    };
  }

}

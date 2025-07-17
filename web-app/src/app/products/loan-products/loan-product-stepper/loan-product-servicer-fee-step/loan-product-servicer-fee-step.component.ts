/** Angular Imports */
import { Component, OnInit, Input, ViewChild, TemplateRef } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators, FormArray } from '@angular/forms';
import { Dates } from 'app/core/utils/dates';
import { MatLegacyDialog as MatDialog, MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef, MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatLegacyTableDataSource as MatTableDataSource, MatLegacyTable as MatTable } from '@angular/material/legacy-table';

/** Custom Dialogs */
/** Custom Models */
import { FormfieldBase } from 'app/shared/form-dialog/formfield/model/formfield-base';
import { InputBase } from 'app/shared/form-dialog/formfield/model/input-base';
import { SelectBase } from 'app/shared/form-dialog/formfield/model/select-base';

import { FormDialogComponent } from 'app/shared/form-dialog/form-dialog.component';
import { DeleteDialogComponent } from 'app/shared/delete-dialog/delete-dialog.component';

/** Custom Services */
import { SettingsService } from 'app/settings/settings.service';

@Component({
  selector: 'mifosx-loan-product-servicer-fee-step',
  templateUrl: './loan-product-servicer-fee-step.component.html',
  styleUrls: ['./loan-product-servicer-fee-step.component.scss']
})
export class LoanProductServicerFeeStepComponent implements OnInit {

  @Input() loanProductsTemplate: any;
  @Input() loanServicerFeeData: any;
  @Input() loanProduct: any;
  @ViewChild('serviceFeeCharge') serviceFeeChargeDialog = {} as TemplateRef<any>;
  chargeData: any;
  feesData: any ;
  roundingModes: any;
  servicerFeeChargesRatio : any;
  servicerFeeChargesRatioOptions :any;
//   servicerFeeChargesRatioValue: any;
  //   colendingChargeData : any;
  feesId: any;
  feesNameHead: any;
  chargeId: any;
  chargeNameHead: any;
  addSFDetailsDialogRef: any;
  sfCharges = [];
  displayEdit = false;
  required = true;
  charges: any;


  displayedColumns: string[] =
    [
    'servicerFeeChargesRatio',
      'charge',
      'sfSelfShareCharge',
      'sfPartnerShareCharge',
      'sfChargeAmtGstLossEnabled',
      'sfChargeAmtGstLoss',
      'isActive',
      'edit'
    ];


  loanProductServicerFeeForm: UntypedFormGroup;
  loanProductServicerFeeChargeForm: UntypedFormGroup;
  dataSource = new MatTableDataSource();
  /**
    * @param {FormBuilder} formBuilder Form Builder.
    * @param {Dates} dateUtils Date Utils.
    * @param {SettingsService} settingsService Settings Service.
    */

  constructor(private formBuilder: UntypedFormBuilder,
    private dateUtils: Dates,
    private settingsService: SettingsService,
    private dialog: MatDialog) {
    this.createLoanProductServiceFeeForm();
  }

  ngOnInit(): void {


    this.feesData = this.loanProductsTemplate.feeOption;
    this.roundingModes = this.loanProductsTemplate.roundingModes;
    this.servicerFeeChargesRatio = this.loanProductsTemplate.servicerFeeChargesRatioOptions;

    this.loanProductServicerFeeForm.patchValue({

      'servicerFeeInterestConfigEnabled': this.loanProductsTemplate.servicerFeeInterestConfigEnabled,
      'vclHurdleRate': this.loanServicerFeeData.vclHurdleRate,
      'vclInterestRound': this.loanServicerFeeData.vclInterestRound,
      'vclInterestDecimal': this.loanServicerFeeData.vclInterestDecimal,
      'sfBaseAmtGstLossEnabled': this.loanServicerFeeData.sfBaseAmtGstLossEnabled,
      'sfBaseAmtGstLoss': this.loanServicerFeeData.sfBaseAmtGstLoss,
      'sfGst': this.loanServicerFeeData.sfGst,
      'sfGstRound': this.loanServicerFeeData.sfGstRound,
      'sfGstDecimal': this.loanServicerFeeData.sfGstDecimal,
      'servicerFeeRound': this.loanServicerFeeData.servicerFeeRound,
      'servicerFeeDecimal': this.loanServicerFeeData.servicerFeeDecimal,
      'servicerFeeChargesConfigEnabled': this.loanProductsTemplate.servicerFeeChargesConfigEnabled,
      //      'servicerFeeChargesConfigEnabled':this.loanServicerFeeData.servicerFeeChargeData.length != 0,
      'sfChargeBaseAmountRoundingmode': this.loanServicerFeeData.sfChargeBaseAmountRoundingmode,
      'sfChargeBaseAmountDecimal': this.loanServicerFeeData.sfChargeBaseAmountDecimal,
      'sfChargeGst': this.loanServicerFeeData.sfChargeGst,
      'sfChargeGstRoundingmode': this.loanServicerFeeData.sfChargeGstRoundingmode,
      'sfChargeGstDecimal': this.loanServicerFeeData.sfChargeGstDecimal,
      'sfChargeRound': this.loanServicerFeeData.sfChargeRound,
      'sfChargeDecimal': this.loanServicerFeeData.sfChargeDecimal,

    });
    if (this.loanProductsTemplate.servicerFeeChargesConfigEnabled.value) {
      this.required = true;
    }
    else {
      this.required = false;
    }
    this.sfCharges = this.loanServicerFeeData.servicerFeeChargeData ? this.loanServicerFeeData.servicerFeeChargeData : [];
    this.dataSource = new MatTableDataSource(this.loanServicerFeeData.servicerFeeChargeData);
    this.loanProductServicerFeeChargeForm = this.formBuilder.group({
    'servicerFeeChargesRatio' :[],
      'charge': [],
      "sfSelfShareCharge": [],
      "sfPartnerShareCharge": [],
      "sfChargeAmtGstLossEnabled": [false],
      "sfChargeAmtGstLoss": [],
      "isActive": [false],
    });

  }

  createLoanProductServiceFeeForm() {
    this.loanProductServicerFeeForm = this.formBuilder.group({
      'servicerFeeInterestConfigEnabled': [false],
      'vclInterestRound': [''],
      'vclInterestDecimal': [''],
      'vclHurdleRate': [''],
      'servicerFeeRound': [''],
      'servicerFeeDecimal': [''],
      'sfBaseAmtGstLossEnabled': [false],
      'sfBaseAmtGstLoss': [''],
      'servicerFeeChargesConfigEnabled': [false],
      'sfGst': [''],
      'sfGstRound': [''],
      'sfGstDecimal': [''],
      'sfChargeGst': [''],
      'sfChargeRound': [''],
      'sfChargeDecimal': [''],
      'sfChargeBaseAmountRoundingmode': [''],
      'sfChargeBaseAmountDecimal': [''],
      'sfChargeGstRoundingmode': [''],
      'sfChargeGstDecimal': ['']
    });
  }

  //   get loanProductServicerFeeEnable() {
  //       return { ...this.loanProductServicerFeeForm.value};
  //     }

  get loanProductServicerFee() {
    return { ...this.loanProductServicerFeeForm.value, servicerFeeCharge: this.sfCharges };
  }



  addDetails() {
    this.displayEdit = false;
      this.servicerFeeChargesRatioOptions = this.loanProductsTemplate.servicerFeeChargesRatioOptions;
    this.loanProductServicerFeeChargeForm = this.formBuilder.group({
    'servicerFeeChargesRatio': [],
      'charge': [],
      "sfSelfShareCharge": [],
      "sfPartnerShareCharge": [],
      "sfChargeAmtGstLossEnabled": [false],
      "sfChargeAmtGstLoss": [],
      "isActive": [false],
    });

    const data = {
      //       title: ' Servicer Fee Details',
    };
    this.addSFDetailsDialogRef = this.dialog.open(this.serviceFeeChargeDialog, { data });
  }


  add() {
    if (this.sfCharges.filter(charge => charge.id === this.loanProductServicerFeeChargeForm.value.charge.id).length === 0) {
      this.sfCharges.push(this.loanProductServicerFeeChargeForm.value);
    }
    this.dataSource.data = this.sfCharges;

    this.addSFDetailsDialogRef.close();
  }

  cancel() {
    this.addSFDetailsDialogRef.close();
  }


  edit(sfCharges) {
    this.displayEdit = true;
    this.charges = sfCharges.charge.name;
    this.servicerFeeChargesRatio = this.loanProductsTemplate.servicerFeeChargesRatioOptions;
    this.loanProductServicerFeeChargeForm.patchValue({

      'servicerFeeChargesRatio': sfCharges.servicerFeeChargesRatio,
      'charge': sfCharges.charge.name && sfCharges.charge,
      "sfSelfShareCharge": sfCharges.sfSelfShareCharge,
      "sfPartnerShareCharge": sfCharges.sfPartnerShareCharge,
      "sfChargeAmtGstLossEnabled": sfCharges.sfChargeAmtGstLossEnabled,
      "sfChargeAmtGstLoss": sfCharges.sfChargeAmtGstLoss,
      "isActive": sfCharges.isActive,
    });

    const data = {
      title: 'Edit Servicer Fee Details',
    };
    this.addSFDetailsDialogRef = this.dialog.open(this.serviceFeeChargeDialog, { data });
  }

  update() {
    const sfcharge = this.loanProductServicerFeeChargeForm.value;
    this.sfCharges.map(sfc => {
      if (sfcharge.charge.id === sfc.charge.id) {
        sfc.servicerFeeChargesRatio = sfcharge.servicerFeeChargesRatio;
        sfc.sfSelfShareCharge = sfcharge.sfSelfShareCharge;
        sfc.sfPartnerShareCharge = sfcharge.sfPartnerShareCharge;
        sfc.sfChargeAmtGstLossEnabled = sfcharge.sfChargeAmtGstLossEnabled;
        sfc.sfChargeAmtGstLoss = sfc.sfChargeAmtGstLossEnabled ? sfcharge.sfChargeAmtGstLoss : 0;
        sfc.isActive = sfcharge.isActive;
      }
      return sfc;
    });
    this.dataSource = new MatTableDataSource(this.sfCharges);
    this.addSFDetailsDialogRef.close();

  }

  refreshData(isChecked) {
    if (!isChecked.checked) {
      this.required = false;
      this.createLoanProductServiceFeeForm();
      //     this.sfCharges = [];
      this.dataSource.data = [];
      //     this.sfCharges = [];
    }
  }

  refreshChargeData(isChecked) {
    //   if(!isChecked.checked){
    //   this.required=false;
    //   console.log("this.sfCharges",this.sfCharges);
    //     this.dataSource.data = null;
    //     console.log(" this.dataSource.data ", this.dataSource.data );
    //   }
  }


}

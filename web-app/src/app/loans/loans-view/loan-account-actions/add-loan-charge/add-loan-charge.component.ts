/** Angular Imports */
import { Component, OnInit } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators, UntypedFormControl } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

/** Custom Services */
import { LoansService } from '../../../loans.service';
import { SettingsService } from 'app/settings/settings.service';
import { Dates } from 'app/core/utils/dates';
import { NotificationService } from '../../../../notification.service';
import * as _moment from 'moment';
import { Moment } from 'moment';

const moment = _moment;
/**
 * Create Add Loan Charge component.
 */

@Component({
  selector: 'mifosx-add-loan-charge',
  templateUrl: './add-loan-charge.component.html',
  styleUrls: ['./add-loan-charge.component.scss']
})
export class AddLoanChargeComponent implements OnInit {
//
//  minDate: Moment;
//   maxDate: Moment;

minDate: Date;
  maxDate: Date;

//   myDateFilter = (m: Moment | null): boolean => {
//     const dateNum = (m || moment()).date();
//     return dateNum >= 10 && dateNum <= 25;
//   }

//   /** Minimum Due Date allowed. */
//   minDate = new Date(2000, 0, 1);
//   /** Maximum Due Date allowed. */
//   maxDate: any;

  /** Add Loan Charge form. */
  loanChargeForm: UntypedFormGroup;
  /** loan charge options. */
  loanChargeOptions: {
    id: number;
    name: string;
    amount: number;
    currency: {
      name: string;
    };
    chargeCalculationType: {
      value: any;
    };
    chargeTimeType: {
      id: number;
      value: any;
    };
  }[];
  /** loan Id of the loan account. */
  loanId: string;

  /**
   * Retrieves the loan charge template data from `resolve`.
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {AccountingService} accountingService Accounting Service.
   * @param {ActivatedRoute} route Activated Route.
   * @param {Router} router Router for navigation.
   * @param {SettingsService} settingsService Settings Service
   */
  constructor(private formBuilder: UntypedFormBuilder,
              private route: ActivatedRoute,
              private router: Router,
              private dateUtils: Dates,
              private loansService: LoansService,
              private settingsService: SettingsService,
               private notifyService : NotificationService) {
    this.route.data.subscribe((data: { actionButtonData: any }) => {
      this.loanChargeOptions = data.actionButtonData.chargeOptions;
    });
    this.loanId = this.route.parent.snapshot.params['loanId'];

  const currentYear=new Date().getFullYear();
  const month=new Date().getMonth();
  const date=new Date().getDate();
  this.minDate= new Date(currentYear - 1,1,1);
  this.maxDate=new Date(currentYear + 0, month+1 ,date);
  }

  /**
   * Creates the Loan Charge form.
   */
  ngOnInit() {
const currentMonth = moment().month();



    this.createLoanChargeForm();
    this.loanChargeForm.controls.chargeId.valueChanges.subscribe(chargeId => {
      const chargeDetails = this.loanChargeOptions.find(option => {
        return option.id === chargeId;
      });
      if (chargeDetails.chargeTimeType.id === 18 || chargeDetails.chargeTimeType.id === 2) {
        this.loanChargeForm.addControl('dueDate', new UntypedFormControl('', Validators.required));
      } else {
        this.loanChargeForm.removeControl('dueDate');
      }
      this.loanChargeForm.patchValue({
        'amount': chargeDetails.amount,
        'chargeCalculation': chargeDetails.chargeCalculationType.value,
        'chargeTime': chargeDetails.chargeTimeType.value
      });
    });
  }

  /**
   * Creates the Loan Charge form.
   */
  createLoanChargeForm() {
    this.loanChargeForm = this.formBuilder.group({
      'chargeId': ['', Validators.required],
      'amount': ['', Validators.required],
      'chargeCalculation': [{ value: '', disabled: true }],
      'chargeTime': [{ value: '', disabled: true }]
    });
  }

  submit() {
    const loanChargeFormData = this.loanChargeForm.value;
    const locale = this.settingsService.language.code;
    const dateFormat = this.settingsService.dateFormat;
    const prevDueDate: Date = this.loanChargeForm.value.dueDate;
    if (loanChargeFormData.dueDate instanceof Date) {
      loanChargeFormData.dueDate = this.dateUtils.formatDate(prevDueDate, dateFormat);
    }
    const data = {
      ...loanChargeFormData,
      dateFormat,
      locale
    };
    this.loansService.createLoanCharge(this.loanId, 'charges', data).subscribe(res => {
      this.router.navigate(['../../general'], { relativeTo: this.route });
       },error=>{
                           console.log(error.error.errors,"response.error]");
                           for(let i=0;i<error.error.errors.length;i++){
                                   this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                                   }
    });
  }
}

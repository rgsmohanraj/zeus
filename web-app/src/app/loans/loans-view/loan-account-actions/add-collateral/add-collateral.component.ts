/** Angular Imports. */
import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

/** Custom Services. */
import { LoansService } from 'app/loans/loans.service';
import { NotificationService } from '../../../../notification.service';

/**
 * Add Collateral component.
 */
@Component({
  selector: 'mifosx-add-collateral',
  templateUrl: './add-collateral.component.html',
  styleUrls: ['./add-collateral.component.scss']
})
export class AddCollateralComponent implements OnInit {

  @Input() dataObject: any;

  /** Collateral form. */
  collateralForm: UntypedFormGroup;
  /** Loan Id. */
  loanId: string;

  /**
   * Retrieve data from `Resolver`.
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {Router} router Router.
   * @param {ActivatedRoute} route Activated Route.
   * @param {LoansService} LoansService loans service.
   */
  constructor(private formBuilder: UntypedFormBuilder,
              private router: Router,
              private route: ActivatedRoute,
              private loanService: LoansService ,
               private notifyService : NotificationService) { }

  ngOnInit() {
    this.createAddCollateralForm();
  }

  /**
   * Set Collateral form.
   */
  createAddCollateralForm() {
    this.collateralForm = this.formBuilder.group({
      'collateralTypeId': ['', Validators.required],
      'value': ['', Validators.required],
      'description': ['']
    });
  }

  /**
   * Submits collateral form.
   */
  submit() {
    const collateralTypeId = this.collateralForm.value.collateralTypeId;
    this.collateralForm.patchValue({
      'collateralTypeId': collateralTypeId
    });
    const loanId = this.route.parent.snapshot.params['loanId'];
    const collateralForm = this.collateralForm.value;
    collateralForm.locale = 'en';
    this.loanService.createLoanCollateral(loanId, collateralForm).subscribe((response: any) => {
      this.router.navigate(['../../general'], { relativeTo: this.route });
      },error=>{
                                 console.log(error.error.errors,"response.error]");
                                 for(let i=0;i<error.error.errors.length;i++){
                                         this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                                         }
    });
  }

}

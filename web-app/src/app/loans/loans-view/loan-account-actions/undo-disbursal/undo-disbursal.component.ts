/** Angular Imports */
import { Component, OnInit } from '@angular/core';
import { UntypedFormControl, UntypedFormBuilder } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

/** Custom Services */
import { LoansService } from '../../../loans.service';
import { NotificationService } from '../../../../notification.service'
/**
 * Undo Disbursal component.
 */
@Component({
  selector: 'mifosx-undo-disbursal',
  templateUrl: './undo-disbursal.component.html',
  styleUrls: ['./undo-disbursal.component.scss']
})
export class UndoDisbursalComponent implements OnInit {

  /** Loan ID. */
  loanId: any;
  /** Undo disbursal form. */
  note: UntypedFormControl;

  /**
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {LoansService} loansService Loans Service.
   * @param {ActivatedRoute} route Activated Route.
   * @param {Router} router Router for navigation.
   */
  constructor(private formBuilder: UntypedFormBuilder,
              private loansService: LoansService,
              private router: Router,
              private route: ActivatedRoute,
               private notifyService : NotificationService) {
    this.loanId = this.route.parent.snapshot.params['loanId'];
  }

  /**
   * Creates the undo disbursal form.
   */
  ngOnInit() {
    this.note = this.formBuilder.control('');
  }

  /**
   * Submits the undo disbursal form.
   */
  submit() {
    this.loansService.loanActionButtons(this.loanId, 'undodisbursal', {'note': this.note.value}).subscribe((response: any) => {
      this.router.navigate(['../../general'], { relativeTo: this.route });
      },error=>{
                           console.log(error.error.errors,"response.error]");
                           for(let i=0;i<error.error.errors.length;i++){
                                   this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                                   }
    });
  }

}

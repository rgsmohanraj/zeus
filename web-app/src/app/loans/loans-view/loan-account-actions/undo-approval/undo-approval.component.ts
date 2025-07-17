/** Angular Imports. */
import { Component, OnInit } from '@angular/core';
import { UntypedFormControl, UntypedFormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

/** Custom Services. */
import { LoansService } from 'app/loans/loans.service';
import { NotificationService } from '../../../../notification.service'
/**
 * Undo Loan component.
 */
@Component({
  selector: 'mifosx-undo-approval',
  templateUrl: './undo-approval.component.html',
  styleUrls: ['./undo-approval.component.scss']
})
export class UndoApprovalComponent implements OnInit {

  /** Form Controller. */
  note: UntypedFormControl;

  /**
   * @param loanService Loan Service.
   * @param route Activated Route.
   * @param router Router.
   */
  constructor(private loanService: LoansService,
              private formBuilder: UntypedFormBuilder,
              private route: ActivatedRoute,
              private router: Router,
               private notifyService : NotificationService) { }

  ngOnInit() {
    this.note = this.formBuilder.control('');
  }

  /**
   * Submits undo approval form.
   */
  submit() {
    const loanId = this.route.parent.snapshot.params['loanId'];
    this.loanService.loanActionButtons(loanId, 'undoapproval', { 'note': this.note.value }).subscribe((response: any) => {
      this.router.navigate(['../../general'], {relativeTo: this.route});
      },error=>{
                           console.log(error.error.errors,"response.error]");
                           for(let i=0;i<error.error.errors.length;i++){
                                   this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                                   }
    });
  }

}

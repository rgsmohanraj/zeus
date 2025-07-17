import { Component, OnInit } from '@angular/core';
import { LoansService } from 'app/loans/loans.service';
import { SettingsService } from 'app/settings/settings.service';
import { Router, ActivatedRoute } from '@angular/router';
import { UntypedFormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { Dates } from 'app/core/utils/dates';
import { NotificationService } from '../../../../notification.service'

@Component({
  selector: 'mifosx-initiate-pennydrop',
  templateUrl: './initiate-pennydrop.component.html',
  styleUrls: ['./initiate-pennydrop.component.scss']
})
export class InitiatePennydropComponent implements OnInit {

 loanId: any;

 /**
						* @param {FormBuilder} formBuilder Form Builder.
						* Retrieves the audit trail search template data from `resolve`.
						* @param {ActivatedRoute} route Activated Route.
						* @param {Dates} dateUtils Dates utils
						*/

  constructor( private route: ActivatedRoute,
              				  private router: Router,
              				  private formBuilder: UntypedFormBuilder,
              				  private dateUtils: Dates,
              			      private settingsService: SettingsService,
              			      private notifyService: NotificationService,
              			      private loansService : LoansService)

              			       {
                           this.loanId = this.route.parent.snapshot.params['loanId'];


              			 }

  ngOnInit() {

    }
//     pennyDropFun(){
//         .subscribe((response: any) => {
//         console.log(response,"response.error]");
//                 if(response.statusCodeValue === 200) {
//                   this.notifyService.showError(response.body, 'Success');
//                   this.router.navigate(['../', response.resourceId], { relativeTo: this.route });
//                 } else {
//                   let errors = response.body.split(',');
//                   errors.forEach(error => {
//                     this.notifyService.showError(error, 'Failed');
//                   })
//
//                 }
//
//                 },error=>{
//                  console.log(error.error.errors,"response.error]");
//                  for(let i=0;i<error.error.errors.length;i++){
//
//                          }
//         });
//     }

}

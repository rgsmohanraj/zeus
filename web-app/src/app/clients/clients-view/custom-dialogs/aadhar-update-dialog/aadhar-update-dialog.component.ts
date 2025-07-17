import { MatLegacyDialog as MatDialog, MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
MatLegacyDialogRef as MatDialogRef, MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';

import { UntypedFormBuilder, UntypedFormGroup, Validators, UntypedFormControl } from '@angular/forms';
import { Component, OnInit, Inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

/** Custom Services */
import { ClientsService } from 'app/clients/clients.service';
import { NotificationService } from '../../../../notification.service';

@Component({
  selector: 'mifosx-aadhar-update-dialog',
  templateUrl: './aadhar-update-dialog.component.html',
  styleUrls: ['./aadhar-update-dialog.component.scss']
})
export class AadharUpdateDialogComponent {

	updateAadhaarDetailForm: UntypedFormGroup;

  aadhaar:any;
  clientId:any;

 /**

   * @param {SavingsService} savingsService Savings Service
   * @param {ActivatedRoute} route ActivatedRoute
   * @param {Router} router Router
   */
  constructor(private formBuilder: UntypedFormBuilder,
              private clientsService: ClientsService,
              private route: ActivatedRoute,
              private router: Router,
              private dialog: MatDialog,
               private notifyService: NotificationService) {

              this.clientId = this.route.parent.snapshot.params['clientId'];
  }

  ngOnInit() {
    this.createUpdateAadhaarForm();
  }

 createUpdateAadhaarForm() {
    this.updateAadhaarDetailForm = this.formBuilder.group({
     'aadhaar':['']
    });
  }

    submit() {
    const updateAadhaarValue = this.updateAadhaarDetailForm.value;
    const data = {
          ...updateAadhaarValue,
        };
        console.log("data",data);
      this.clientsService.updateAadhaar(this.clientId,data)
        .subscribe(() => {
          this.router.navigate(['../'], { relativeTo: this.route });
           },error=>{
               console.log(error.error.errors,"response.error]");
               for(let i=0;i<error.error.errors.length;i++){
                       this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                       }
      });
    }

}

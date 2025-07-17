/** Angular Imports */
import { Component, OnInit,ViewChild } from '@angular/core';
import { FormGroup, UntypedFormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { OfficeBasicDetailsComponent } from '../../offices/office-stepper/office-basic-details/office-basic-details.component';
import { OfficeGstDetailsComponent } from '../../offices/office-stepper/office-gst-details/office-gst-details.component';
import { OfficePreviewComponent } from '../../offices/office-stepper/office-preview/office-preview.component';

/** Custom Services */
import { OrganizationService } from '../../organization.service';
import { SettingsService } from 'app/settings/settings.service';
import { Dates } from 'app/core/utils/dates';
import { NotificationService } from '../../../notification.service';
/**
 * Create Office component.
 */
@Component({
  selector: 'mifosx-create-office',
  templateUrl: './create-office.component.html',
  styleUrls: ['./create-office.component.scss']
})
export class CreateOfficeComponent implements OnInit {

   @ViewChild(OfficeBasicDetailsComponent, { static: true }) officeBasicStep: OfficeBasicDetailsComponent;
   @ViewChild(OfficeGstDetailsComponent, { static: true }) officeGstDetailStep: OfficeGstDetailsComponent;
   @ViewChild(OfficePreviewComponent, { static: true }) officePreviewStep: OfficePreviewComponent;


 officeTemplate: any;

 /**
   * Retrieves the offices data from `resolve`.
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {OrganizationService} organizationService Organization Service.
   * @param {SettingsService} settingsService Settings Service.
   * @param {ActivatedRoute} route Activated Route.
   * @param {Router} router Router for navigation.
   * @param {Dates} dateUtils Date Utils to format date.
    */

  constructor(private formBuilder: UntypedFormBuilder,
              private router: Router,
              private organizationService: OrganizationService,
              private route: ActivatedRoute,
              private settingsService: SettingsService,
              private notifyService: NotificationService) {
               this.route.data.subscribe((data: { officeTemplate: any}) => {
                     this.officeTemplate = data.officeTemplate;
                   });
               }

     ngOnInit(): void {
      }

      get officeBasicForm() {
          return this.officeBasicStep.createOfficeBasicForm;
        }

      get officeGstForm() {
          return this.officeGstDetailStep.createOfficeGstForm;
        }

          get office() {
          console.log(this.officeGstDetailStep.OfficeGstDetails,"this.officeGstDetailStep.OfficeGstDetails")
            if (this.officeTemplate) {
              return {
                ...this.officeBasicStep.OfficeBasicDetails,
                ...this.officeGstDetailStep.OfficeGstDetails,

              };
            } else {
              return {
                ...this.officeBasicStep.OfficeBasicDetails,

              };
            }
          }

     submit() {
         const locale = this.settingsService.language.code;
         const dateFormat = this.settingsService.dateFormat;
         // TODO: Update once language and date settings are setup
         const officeTemplate = {
           ...this.office,
           dateFormat,
           locale
         };
         this.organizationService.createOffice(officeTemplate)
         .subscribe((response: any) => {
                 this.router.navigate(['../', response.resourceId], { relativeTo: this.route });
                 },error=>{
                  for(let i=0;i<error.error.errors.length;i++){
                          this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                          }
         });
       }
    }

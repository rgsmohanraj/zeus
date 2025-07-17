/** Angular Imports */
import { Component, OnInit ,ViewChild} from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Dates } from 'app/core/utils/dates';
import { OfficeBasicDetailsComponent } from '../../offices/office-stepper/office-basic-details/office-basic-details.component';
import { OfficeGstDetailsComponent } from '../../offices/office-stepper/office-gst-details/office-gst-details.component';
import { OfficePreviewComponent } from '../../offices/office-stepper/office-preview/office-preview.component';


/** Custom Services */
import { OrganizationService } from 'app/organization/organization.service';
import { SettingsService } from 'app/settings/settings.service';
import { NotificationService } from '../../../notification.service';
/**
 * Edit Office component.
 */
@Component({
  selector: 'mifosx-edit-office',
  templateUrl: './edit-office.component.html',
  styleUrls: ['./edit-office.component.scss']
})
export class EditOfficeComponent implements OnInit {

 @ViewChild(OfficeBasicDetailsComponent, { static: true }) officeBasicStep: OfficeBasicDetailsComponent;
   @ViewChild(OfficeGstDetailsComponent, { static: true }) officeGstDetailStep: OfficeGstDetailsComponent;
   @ViewChild(OfficePreviewComponent, { static: true }) officePreviewStep: OfficePreviewComponent;


 officeTemplate: any;

  /** Selected Data. */
//   officeTemplate: any;
  /** Office form. */
  officeForm: UntypedFormGroup;
  /** Minimum Date allowed. */
  minDate = new Date(2000, 0, 1);
  /** Maximum Date allowed. */
  maxDate = new Date();
  minDate1 = new Date();
//   sourceData:any;
//   sourceId:any;
//   officerName:any;

    /**
     * Retrieves the charge data from `resolve`.
     * @param {OrganizationService} organizationService Organization Service.
     * @param {SettingsService} settingsService Settings Service.
     * @param {FormBuilder} formBuilder Form Builder.
     * @param {ActivatedRoute} route Activated Route.
     * @param {Router} router Router for navigation.
     * @param {MatDialog} dialog Dialog reference.
     * @param {Dates} dateUtils Date Utils
     */
    constructor(private organizationService: OrganizationService,
                private settingsService: SettingsService,
                private formBuilder: UntypedFormBuilder,
                private route: ActivatedRoute,
                private router: Router,
                private dateUtils: Dates,
                private notifyService: NotificationService) {
      this.route.data.subscribe((data: { officeTemplate: any }) => {
        this.officeTemplate = data.officeTemplate;
      });
    }

  ngOnInit() {

  }

  get officeBasicForm() {
            return this.officeBasicStep.createOfficeBasicForm;
          }

        get officeGstForm() {
            return this.officeGstDetailStep.createOfficeGstForm;
          }

            get office() {
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
  /**
   * Submits the edit office form.
   */
  submit() {
    const officeFormData = this.officeForm.value;
    const locale = this.settingsService.language.code;
    const dateFormat = this.settingsService.dateFormat;
//     const prevOpenedOn: Date = this.officeForm.value.openingDate;
// //     const prevExpiryDate: Date = this.officeForm.value.expiryDate;
//
//     if (officeFormData.openingDate instanceof Date) {
//       officeFormData.openingDate = this.dateUtils.formatDate(prevOpenedOn, dateFormat);
//     }
    const officeTemplate = {
        ...this.office,
      dateFormat,
      locale
    };
    this.organizationService.updateOffice(this.officeTemplate.id, officeTemplate)
    .subscribe((response: any) => {
      this.router.navigate(['../'], { relativeTo: this.route });
       },error=>{
               console.log(error.error.errors,"response.error]");
               for(let i=0;i<error.error.errors.length;i++){
               this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                                                     }
    });
  }

}

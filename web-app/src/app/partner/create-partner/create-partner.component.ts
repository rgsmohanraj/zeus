import { Component, OnInit,ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { PartnerBasicDetailsComponent } from '../partner-stepper/partner-basic-details/partner-basic-details.component';
import { PartnerPartnerDetailsComponent } from '../partner-stepper/partner-partner-details/partner-partner-details.component';
import { PartnerBeneficiaryDetailsComponent } from '../partner-stepper/partner-beneficiary-details/partner-beneficiary-details.component';
import { PartnerPreviewComponent } from '../partner-stepper/partner-preview/partner-preview.component';
import { PartnerLegalDocumentsComponent } from '../partner-stepper/partner-legal-documents/partner-legal-documents.component';
import { SettingsService } from 'app/settings/settings.service';
import { NotificationService } from '../../notification.service';
import { PartnerService } from '../partner.service';

@Component({
  selector: 'mifosx-create-partner',
  templateUrl: './create-partner.component.html',
  styleUrls: ['./create-partner.component.scss']
})
export class CreatePartnerComponent implements OnInit {
   @ViewChild(PartnerBasicDetailsComponent, { static: true }) partnerBasicStep: PartnerBasicDetailsComponent;
   @ViewChild(PartnerPartnerDetailsComponent, { static: true }) partnerDetailStep: PartnerPartnerDetailsComponent;
   @ViewChild(PartnerBeneficiaryDetailsComponent, { static: true }) partnerBeneficiaryStep: PartnerBeneficiaryDetailsComponent;
   @ViewChild(PartnerPreviewComponent, { static: true }) partnerPreviewStep: PartnerPreviewComponent;
   @ViewChild(PartnerLegalDocumentsComponent, { static: true }) partnerLegalDocumentsStep: PartnerLegalDocumentsComponent;

  partnerTemplate: any;
    /** Partner Address Field Config */
//     partnerAddressFieldConfig: any;

  /**
     * Fetches client and address template from `resolve`
     * @param {ActivatedRoute} route Activated Route
     * @param {Router} router Router
     * @param {PartnerService} partnerService Partner Service
     * @param {SettingsService} settingsService Setting service
  */

  constructor(private router: Router,
              private partnerService: PartnerService,
              private route: ActivatedRoute,
              private settingsService: SettingsService,
              private notifyService: NotificationService) {
               this.route.data.subscribe((data: { partnerTemplate: any}) => {
                     this.partnerTemplate = data.partnerTemplate;

                   });
               }

  ngOnInit(): void {
  }

  get partnerBasicForm() {
      return this.partnerBasicStep.createBasicDetailsForm;
    }

  get partnerDetailForm() {
      return this.partnerDetailStep.createPartnerForm;
    }

  get partnerBeneficiaryForm() {
      return this.partnerBeneficiaryStep.createBeneficiaryDetailsForm;
    }

get partnerLegalForm() {
      return this.partnerLegalDocumentsStep.createLegalDocumentsForm;
    }

      get partner() {
        if (this.partnerTemplate) {
          return {
            ...this.partnerBasicStep.partnerBasicDetails,
            ...this.partnerDetailStep.partnerPartnerDetails,
             ... this.partnerBeneficiaryStep.partnerBeneficiaryDetails,
          };
        } else {
          return {
            ...this.partnerBasicStep.partnerBasicDetails,
//              ... this.partnerBeneficiaryStep.partnerBeneficiaryDetails,
//                         ...this.partnerPartnerStep.partnerPartnerDetails,

          };
        }
      }

  submit() {
      const locale = this.settingsService.language.code;
      const dateFormat = this.settingsService.dateFormat;
      // TODO: Update once language and date settings are setup
      const partnerData = {
        ...this.partner,
        dateFormat,
        locale
      };
      this.partnerService.createPartner(partnerData)
      .subscribe((response: any) => {

              this.router.navigate(['../', response.resourceId], { relativeTo: this.route });
              },error=>{
               for(let i=0;i<error.error.errors.length;i++){
                       this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                       }
      });
    }


}

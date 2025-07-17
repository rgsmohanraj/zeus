import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormControl } from '@angular/forms';
import { Dates } from 'app/core/utils/dates';

import { SettingsService } from 'app/settings/settings.service';

@Component({
  selector: 'mifosx-partner-partner-details',
  templateUrl: './partner-partner-details.component.html',
  styleUrls: ['./partner-partner-details.component.scss']
})
export class PartnerPartnerDetailsComponent implements OnInit {
@Input() partnerTemplate: any;
   /** Minimum date allowed. */
  minDate = new Date(1900, 0, 1);
  /** Maximum date allowed. */
  maxDate = new Date();

  createPartnerForm: UntypedFormGroup;

  partnerTypeOptions : any;
  modelLimit : any;
  approvedLimit : any;
  pilotLimit : any;
  partnerFloatLimit : any;
  balanceLimit : any;
  agreementStartDate : any;
  agreementExpiryDate : any;
  underlyingAssetsOptions : any;
  securityOptions : any;
  fldgCalculationOnOptions : any;

  /**
     * @param {FormBuilder} formBuilder Form Builder.
     * @param {Dates} dateUtils Date Utils.
     * @param {SettingsService} settingsService Settings Service.
     */

  constructor(private formBuilder: UntypedFormBuilder,
                    private dateUtils: Dates,
                    private settingsService: SettingsService) {
                          this.setPartnerForm();}

  ngOnInit(): void
   {

   this.maxDate = this.settingsService.businessDate;

   this.partnerTypeOptions = this.partnerTemplate.partnerTypeOptions;
   this.underlyingAssetsOptions = this.partnerTemplate.underlyingAssetsOptions;
   this.securityOptions = this.partnerTemplate.securityOptions;
   this.fldgCalculationOnOptions = this.partnerTemplate.fldgCalculationOnOptions;


   this.createPartnerForm.patchValue({

     'partnerType': this.partnerTemplate.partnerType && this.partnerTemplate.partnerType.id,
     'modelLimit' : this.partnerTemplate.modelLimit,
     'approvedLimit': this.partnerTemplate.approvedLimit,
     'pilotLimit' : this.partnerTemplate.pilotLimit,
     'partnerFloatLimit': this.partnerTemplate.partnerFloatLimit,
     'balanceLimit' : this.partnerTemplate.balanceLimit,
     'agreementStartDate': this.partnerTemplate.agreementStartDate && new Date(this.partnerTemplate.agreementStartDate),
     'agreementExpiryDate': this.partnerTemplate.agreementExpiryDate && new Date(this.partnerTemplate.agreementExpiryDate),
     'underlyingAssets': this.partnerTemplate.underlyingAssets && this.partnerTemplate.underlyingAssets.id,
     'security': this.partnerTemplate.security && this.partnerTemplate.security.id,
     'fldgCalculationOn': this.partnerTemplate.fldgCalculationOn && this.partnerTemplate.fldgCalculationOn.id,
     });
    }
     setPartnerForm() {
        this.createPartnerForm = this.formBuilder.group({
          'partnerType': [''],
          'modelLimit': [''],
          'approvedLimit': ['',Validators.required],
          'pilotLimit':  [''],
          'partnerFloatLimit': [''],
          'balanceLimit': [''],
          'agreementStartDate': [''],
          'agreementExpiryDate': [''],
          'underlyingAssets': [''],
          'security': [''],
          'fldgCalculationOn': ['']
        });
  }

  get partnerPartnerDetails() {
          const partnerPartnerDetails = this.createPartnerForm.value;
           const agreementStartDate = this.createPartnerForm.value.agreementStartDate;
           const agreementExpiryDate = this.createPartnerForm.value.agreementExpiryDate;
           const dateFormat = this.settingsService.dateFormat;
             const locale = this.settingsService.language.code;

             if (partnerPartnerDetails.agreementStartDate instanceof Date) {
              partnerPartnerDetails.agreementStartDate = this.dateUtils.formatDate(agreementStartDate, dateFormat) || '';
                 }

             if (partnerPartnerDetails.agreementExpiryDate instanceof Date) {
              partnerPartnerDetails.agreementExpiryDate = this.dateUtils.formatDate(agreementExpiryDate, dateFormat) || '';
               }
        return partnerPartnerDetails;
        }

}

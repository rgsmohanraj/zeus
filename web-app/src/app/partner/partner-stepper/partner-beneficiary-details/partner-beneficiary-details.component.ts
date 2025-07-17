import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormControl } from '@angular/forms';
import { Dates } from 'app/core/utils/dates';

import { SettingsService } from 'app/settings/settings.service';

@Component({
  selector: 'mifosx-partner-beneficiary-details',
  templateUrl: './partner-beneficiary-details.component.html',
  styleUrls: ['./partner-beneficiary-details.component.scss']
})
export class PartnerBeneficiaryDetailsComponent implements OnInit {
@Input() partnerTemplate: any;

 createBeneficiaryDetailsForm: UntypedFormGroup;

 beneficiaryName : any;
 beneficiaryAccountNumber : any;
 ifscCode : any;
 micrCode : any;
 swiftCode : any;
 branch : any;

  /**
      * @param {FormBuilder} formBuilder Form Builder.
      * @param {Dates} dateUtils Date Utils.
      * @param {SettingsService} settingsService Settings Service.
      */

  constructor(private formBuilder: UntypedFormBuilder,
              private settingsService: SettingsService) {
               this.setBeneficiaryDetailsForm();
               }

         allowAlphabetAndNums(e){
                let k;
                document.all ? k = e.keyCode : k = e.which;
                return((k > 64 && k < 91) || (k > 96 && k < 123) || k == 8 || k == 32 || (k >= 48 && k <= 57));
                }


  ngOnInit(): void {
  //this.maxDate = this.settingsService.businessDate;

  this.beneficiaryName = this.partnerTemplate.beneficiaryName;
  this.beneficiaryAccountNumber = this.partnerTemplate.beneficiaryAccountNumber;
  this.ifscCode = this.partnerTemplate.ifscCode;
  this.micrCode = this.partnerTemplate.micrCode;
  this.swiftCode = this.partnerTemplate.swiftCode;
  this.branch = this.partnerTemplate.branch;


    this.createBeneficiaryDetailsForm.patchValue({

    'beneficiaryName': this.partnerTemplate.beneficiaryName,
     'beneficiaryAccountNumber' :this.partnerTemplate.beneficiaryAccountNumber,
     'ifscCode': this.partnerTemplate.ifscCode,
     'micrCode':this.partnerTemplate.micrCode,
     'swiftCode':this.partnerTemplate.swiftCode,
     'branch': this.partnerTemplate.branch,

  });
    }
     setBeneficiaryDetailsForm() {
        this.createBeneficiaryDetailsForm = this.formBuilder.group({

          'beneficiaryName': [''],
          'beneficiaryAccountNumber': [''],
          'ifscCode':  [''],
          'micrCode': [''],
          'swiftCode': [''],
          'branch': [''],
           });
  }

get partnerBeneficiaryDetails() {
        const partnerBeneficiaryDetails = this.createBeneficiaryDetailsForm.value;
      return partnerBeneficiaryDetails;
      }

      numberOnly(event): boolean {
                const charCode = (event.which) ? event.which : event.keyCode;
                if (charCode > 31 && (charCode < 48 || charCode > 57)) {
                  return false;
                }
                return true;
            }
}

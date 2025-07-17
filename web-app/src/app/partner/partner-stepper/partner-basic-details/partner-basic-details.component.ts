import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormControl } from '@angular/forms';
import { Dates } from 'app/core/utils/dates';
import { SettingsService } from 'app/settings/settings.service';

@Component({
  selector: 'mifosx-partner-basic-details',
  templateUrl: './partner-basic-details.component.html',
  styleUrls: ['./partner-basic-details.component.scss']
})
export class PartnerBasicDetailsComponent implements OnInit {
@Input() partnerTemplate: any;

/** Minimum date allowed. */
  minDate = new Date(2000, 0, 1);
    maxDate = new Date(new Date().setFullYear(new Date().getFullYear() + 10));

  createBasicDetailsForm: UntypedFormGroup;

  partnerName: any;
  partnerCompanyRegistrationDate: any;
  sourceOptions: any;
  panCard: any;
  cinNumber: any;
  address1: any;
  address2 : any;
  stateOptions : any;
  pincode: any;
  countryOptions : any;
  constitutionOptions : any;
  keyPersons : any;
  industryOptions : any;
  sectorOptions : any;
  subSectorOptions : any;
  gstNumber : any;
  gstRegistrationOptions : any;

  /**
     * @param {FormBuilder} formBuilder Form Builder.
     * @param {Dates} dateUtils Date Utils.
     * @param {SettingsService} settingsService Settings Service.
     */

  constructor(private formBuilder: UntypedFormBuilder,
              private dateUtils: Dates,
              private settingsService: SettingsService) {
             this.setBasicForm();
                             }

 allowAlphabetAndNums(e){
 let k;
 document.all ? k = e.keyCode : k = e.which;
 return((k > 64 && k < 91) || (k > 96 && k < 123) || k == 8 || k == 32 || (k >= 48 && k <= 57));
 }

  ngOnInit(): void
  {
  this.maxDate = this.settingsService.businessDate;

  this.sourceOptions = this.partnerTemplate.sourceOptions;
  this.stateOptions = this.partnerTemplate.stateOptions;
  this.countryOptions = this.partnerTemplate.countryOptions;
  this.constitutionOptions = this.partnerTemplate.constitutionOptions;
  this.industryOptions = this.partnerTemplate.industryOptions;
  this.sectorOptions = this.partnerTemplate.sectorOptions;
  this.subSectorOptions = this.partnerTemplate.subSectorOptions;
  this.gstRegistrationOptions = this.partnerTemplate.gstRegistrationOptions;


  this.createBasicDetailsForm.patchValue({

  'partnerName': this.partnerTemplate.partnerName,
  'partnerCompanyRegistrationDate': this.partnerTemplate.partnerCompanyRegistrationDate && new Date(this.partnerTemplate.partnerCompanyRegistrationDate),
  'source': this.partnerTemplate.source && this.partnerTemplate.source.id,
   'panCard': this.partnerTemplate.panCard,
  'cinNumber': this.partnerTemplate.cinNumber,
  'address1': this.partnerTemplate.address1,
  'address2': this.partnerTemplate.address2,
  'city':this.partnerTemplate.city,
  'state':this.partnerTemplate.state && this.partnerTemplate.state.id,
  'pincode': this.partnerTemplate.pincode,
  'country':this.partnerTemplate.country && this.partnerTemplate.country.id,
  'constitution':this.partnerTemplate.constitution && this.partnerTemplate.constitution.id,
  'keyPersons': this.partnerTemplate.keyPersons,
  'industry':this.partnerTemplate.industry && this.partnerTemplate.industry.id,
  'sector':this.partnerTemplate.sector && this.partnerTemplate.sector.id,
  'subSector':this.partnerTemplate.subSector && this.partnerTemplate.subSector.id,
  'gstNumber': this.partnerTemplate.gstNumber,
  'gstRegistration':this.partnerTemplate.gstRegistration && this.partnerTemplate.gstRegistration.id,

  });
  }
   setBasicForm() {
      this.createBasicDetailsForm = this.formBuilder.group({
        'partnerName': [''],
       // 'fullname': [''],
        'partnerCompanyRegistrationDate': [''],
//         'registrationDate':  [''],
        'source': [''],
        'panCard': [''],
        'cinNumber': [''],
        'address1': [''],
        'address2': [''],
        'city': [''],
        'state': [''],
        'pincode': [''],
        'country': [''],
        'constitution': [''],
        'keyPersons': [''],
        'industry': [''],
        'sector': [''],
        'subSector': [''],
        'gstNumber': [''],
        'gstRegistration': ['']
      });
    }

     get partnerBasicDetails() {
        const generalDetails = this.createBasicDetailsForm.value;
//         const partnerCompanyRegistrationDate = this.createBasicDetailsForm.value.partnerCompanyRegistrationDate;
        const dateFormat = this.settingsService.dateFormat;
        const locale = this.settingsService.language.code;

if (generalDetails.partnerCompanyRegistrationDate instanceof Date) {
     generalDetails.partnerCompanyRegistrationDate = this.dateUtils.formatDate(generalDetails.partnerCompanyRegistrationDate, dateFormat);
    }
     return generalDetails;
      }

      numberOnly(event): boolean {
          const charCode = (event.which) ? event.which : event.keyCode;
          if (charCode > 31 && (charCode < 48 || charCode > 57)) {
            return false;
          }
          return true;
      }
}

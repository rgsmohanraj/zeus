/** Angular Imports */
import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, UntypedFormControl } from '@angular/forms';
import { Dates } from 'app/core/utils/dates';

/** Custom Services */
import { SettingsService } from 'app/settings/settings.service';

/**
 * Create Client Component
 */
@Component({
  selector: 'mifosx-client-general-step',
  templateUrl: './client-general-step.component.html',
  styleUrls: ['./client-general-step.component.scss']
})
export class ClientGeneralStepComponent implements OnInit {

 /** Minimum date allowed. */
  minDate = new Date(2000, 0, 1);
  /** Maximum date allowed. */
  maxDate = new Date(2100, 0, 1);

//   minDate1 = new Date();

  /** Client Template */
  @Input() clientTemplate: any;
  /** Create Client Form */
  createClientForm: UntypedFormGroup;

  /** Office Options */
  officeOptions: any;
//   /** Staff Options */
//   staffOptions: any;
  /** Legal Form Options */
  legalFormOptions: any;
  /** Client Type Options */
  clientTypeOptions: any;
  /** Client Classification Options */
  clientClassificationTypeOptions: any;
  /** Business Line Options */
  businessLineOptions: any;
  /** Constitution Options */
  constitutionOptions: any;
  /** Gender Options */
  genderOptions: any;
  /** State Options */
  stateOptions: any;
  /** Saving Product Options */
  savingProductOptions: any;

 // repaymentModeOptions:any;

  transactionTypePreferenceOptions:any;





  /**
   * @param {FormBuilder} formBuilder Form Builder
   * @param {Dates} dateUtils Date Utils
   * @param {SettingsService} settingsService Setting service
   */
  constructor(private formBuilder: UntypedFormBuilder,
              private dateUtils: Dates,
              private settingsService: SettingsService) {
              this.setClientForm();
  }

  ngOnInit() {

    this.minDate = this.settingsService.businessDate;
    this.maxDate = this.settingsService.businessDate;
    this.setOptions();
    this.buildDependencies();

//     this.createClientForm.patchValue({
//
//     'officeId':this.clientTemplate.officeId,
// //      'legalFormId':this.clientTemplate.legalFormId && this.clientTemplate.legalFormId.id,
//     'active':this.clientTemplate.active,
//     'fullname':this.clientTemplate.fullname,
//     'firstname':this.clientTemplate.firstname,
//     'lastname':this.clientTemplate.lastname,
//     'dateOfBirth':this.clientTemplate.dateOfBirth,
//     'age':this.clientTemplate.age,
//     'externalId':this.clientTemplate.externalId,
//     'genderId':this.clientTemplate.genderId && this.clientTemplate.genderId.id,
//     'mobileNo':this.clientTemplate.mobileNo,
//     'emailAddress':this.clientTemplate.emailAddress,
//     'cityId':this.clientTemplate.cityId && this.clientTemplate.cityId.id,
//     'address':this.clientTemplate.address,
//     'clientTypeId':this.clientTemplate.clientTypeId && this.clientTemplate.clientTypeId.id,
//     'clientClassificationId':this.clientTemplate.clientClassificationId && this.clientTemplate.clientClassificationId.id,
// });

  }
  numberOnly(event): boolean {
        const charCode = (event.which) ? event.which : event.keyCode;
        if (charCode > 31 && (charCode < 48 || charCode > 57)) {
          return false;
        }
        return true;
      }

  /**
   * Creates the client form.
   */
  setClientForm() {
    this.createClientForm = this.formBuilder.group({
      'officeId': ['', Validators.required],
//       'staffId': [''],
      'legalFormId': [''],
//       'isStaff': [false],
      'active': [false],
      'addSavings': [false],
      'accountNo': [''],
      'externalId': [''],
      'genderId': ['',Validators.required],
       'age':['',Validators.required],
      'stateId': ['',Validators.required],
      'city':['',Validators.required],
//       'repaymentModeId':[''],
//       'transactionTypePreferenceId':[''],
      'address':['',Validators.required],
      'mobileNo': ['',Validators.required],
      'emailAddress': [''],
      'dateOfBirth': ['',Validators.required],
      'clientTypeId': [''],
      'clientClassificationId': [''],
      'activationDate':['',Validators.required],
      'submittedOnDate': ['',Validators.required],
      'pan':[''],
      'aadhaar':['',Validators.required],
      'voterId':[''],
      'passportNumber':[''],
      'drivingLicense':[''],
      'pincode':['',Validators.required],
      'rationCardNumber':['']
    });
  }

  /**
   * Sets select dropdown options.
   */
  setOptions() {
    this.officeOptions = this.clientTemplate.officeOptions;
//     this.staffOptions = this.clientTemplate.staffOptions;
    this.legalFormOptions = this.clientTemplate.clientLegalFormOptions;
    this.clientTypeOptions = this.clientTemplate.clientTypeOptions;
    this.clientClassificationTypeOptions = this.clientTemplate.clientClassificationOptions;
    this.businessLineOptions = this.clientTemplate.clientNonPersonMainBusinessLineOptions;
    this.constitutionOptions = this.clientTemplate.clientNonPersonConstitutionOptions;
    this.genderOptions = this.clientTemplate.genderOptions;
    this.stateOptions = this.clientTemplate.stateOptions;
    this.savingProductOptions = this.clientTemplate.savingProductOptions;
}





  /**
   * Adds controls conditionally.
   */
  buildDependencies() {
    this.createClientForm.get('legalFormId').valueChanges.subscribe((legalFormId: any) => {
      if (legalFormId === 1) {
        this.createClientForm.removeControl('fullname');
        this.createClientForm.removeControl('clientNonPersonDetails');
        this.createClientForm.addControl('firstname', new UntypedFormControl('', [Validators.required]));
        this.createClientForm.addControl('middlename', new UntypedFormControl('', ));
        this.createClientForm.addControl('lastname', new UntypedFormControl('', [Validators.required]));
      } else {
        this.createClientForm.removeControl('firstname');
        this.createClientForm.removeControl('middlename');
        this.createClientForm.removeControl('lastname');
        this.createClientForm.addControl('fullname', new UntypedFormControl('', [Validators.required]));
        this.createClientForm.addControl('clientNonPersonDetails', this.formBuilder.group({
          'constitutionId': [''],
          'incorpValidityTillDate': [''],
          'incorpNumber': [''],
          'mainBusinessLineId': [''],
          'remarks': ['']
        }));
      }
    });
    this.createClientForm.get('legalFormId').patchValue(1);
    this.createClientForm.get('active').valueChanges
                    .subscribe((active: any) => {
                      if (active) {
                        this.createClientForm.addControl('activationDate', new UntypedFormControl('', Validators.required));
                        }
                        else{
                        this.createClientForm.removeControl('activationDate');
                        }
                        });

    this.createClientForm.get('addSavings').valueChanges.subscribe((active: boolean) => {
      if (active) {
        this.createClientForm.addControl('savingsProductId', new UntypedFormControl('', Validators.required));
      } else {
        this.createClientForm.removeControl('savingsProductId');
      }
    });
  }

  /**
   * Client General Details
   */
  get clientGeneralDetails() {
    const generalDetails = this.createClientForm.value;
    const dateFormat = this.settingsService.dateFormat;
    const locale = this.settingsService.language.code;
    for (const key in generalDetails) {
      if (generalDetails[key] === '' || key === 'addSavings') {
        delete generalDetails[key];
      }
    }
    if (generalDetails.submittedOnDate instanceof Date) {
      generalDetails.submittedOnDate = this.dateUtils.formatDate(generalDetails.submittedOnDate, dateFormat);
    }
    if (generalDetails.activationDate instanceof Date) {
      generalDetails.activationDate = this.dateUtils.formatDate(generalDetails.activationDate, dateFormat);
    }
    if (generalDetails.dateOfBirth instanceof Date) {
      generalDetails.dateOfBirth = this.dateUtils.formatDate(generalDetails.dateOfBirth, dateFormat);
    }
//     if (generalDetails.incorpValidityTillDate instanceof Date) {
//           generalDetails.incorpValidityTillDate = this.dateUtils.formatDate(generalDetails.incorpValidityTillDate, dateFormat);
//         }


    if (generalDetails.clientNonPersonDetails && generalDetails.clientNonPersonDetails.incorpValidityTillDate) {
      generalDetails.clientNonPersonDetails = {
        ...generalDetails.clientNonPersonDetails,
        incorpValidityTillDate: this.dateUtils.formatDate(generalDetails.clientNonPersonDetails.incorpValidityTillDate, dateFormat),
        dateFormat,
        locale
      };
    }
    return generalDetails;
  }



// change();
//  const currentYear=new Date(this.createClientForm.value.dateOfBirth).getFullYear();
//     const month=new Date(this.createClientForm.value.dateOfBirth).getMonth();
//     const date=new Date(this.createClientForm.value.dateOfBirth).getDate();
//     this.minDate1 = new Date(currentYear + 0,0,date+1);
//     console.log(this.minDate1,"mindate1")
}

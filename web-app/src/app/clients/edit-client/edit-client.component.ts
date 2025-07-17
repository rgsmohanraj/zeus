/** Angular Imports */

import { Component, OnInit,ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UntypedFormBuilder, UntypedFormGroup, Validators, UntypedFormControl } from '@angular/forms';
import { ClientGeneralStepComponent } from '../client-stepper/client-general-step/client-general-step.component';
// import { ClientFamilyMembersStepComponent } from '../client-stepper/client-family-members-step/client-family-members-step.component';
import { ClientPreviewStepComponent } from '../client-stepper/client-preview-step/client-preview-step.component';
 import { ClientAddressStepComponent } from '../client-stepper/client-address-step/client-address-step.component';
// import { ClientFamilyMemberDialogComponent } from '../client-stepper/client-family-members-step/client-family-member-dialog/client-family-member-dialog.component';
import { ClientBeneficiaryStepperComponent } from '../client-stepper/client-beneficiary-stepper/client-beneficiary-stepper.component';

/** Custom Services */
import { ClientsService } from '../clients.service';
import { SettingsService } from 'app/settings/settings.service';
import { Dates } from 'app/core/utils/dates';
import { NotificationService } from '../../notification.service';

/**
 * Edit Client Component
 */
@Component({
  selector: 'mifosx-edit-client',
  templateUrl: './edit-client.component.html',
  styleUrls: ['./edit-client.component.scss']
})
export class EditClientComponent implements OnInit {

 @ViewChild(ClientGeneralStepComponent, { static: true }) clientGeneralStep: ClientGeneralStepComponent;
  /** Client Family Members Step */

//   @ViewChild(ClientAddressStepComponent, { static: true }) clientAddressStep: ClientAddressStepComponent;

  @ViewChild(ClientBeneficiaryStepperComponent, { static: true }) clientBeneficiaryStep: ClientBeneficiaryStepperComponent;

   @ViewChild(ClientPreviewStepComponent, { static: true }) clientPreviewStep: ClientPreviewStepComponent;

  /** Minimum date allowed. */
  minDate = new Date(2000, 0, 1);
  /** Maximum date allowed. */
  maxDate = new Date();

  /** Client Data and Template */
  clientDataAndTemplate: any;
  /** Edit Client Form */
   editClientForm: UntypedFormGroup;

   clientAddressFieldConfig : any;

  /** Office Options */
  officeOptions: any;
  /** Staff Options */
  staffOptions: any;
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
//
  //repaymentModeOptions : any;
  accountTypeOptions : any;

  maskedAadhaar: any;
  aadhaar:any;

  clientTemplate : any;

  /**
   * Fetches client template data from `resolve`
   * @param {FormBuilder} formBuilder Form Builder
   * @param {ActivatedRoute} route ActivatedRoute
   * @param {Router} router Router
   * @param {ClientsService} clientsService Clients Service
   * @param {Dates} dateUtils Date Utils
   * @param {SettingsService} settingsService Settings Service
   */
  constructor(private formBuilder: UntypedFormBuilder,
              private route: ActivatedRoute,
              private router: Router,
              private clientsService: ClientsService,
              private dateUtils: Dates,
              private settingsService: SettingsService,
              private notifyService: NotificationService) {
    this.route.data.subscribe((data: { clientDataAndTemplate: any ,clientAddressFieldConfig : any}) => {
      this.clientDataAndTemplate = data.clientDataAndTemplate;
      this.clientAddressFieldConfig = data.clientAddressFieldConfig;
    });
  }

  ngOnInit() : void {
    this.minDate = this.settingsService.businessDate;
    this.maxDate = this.settingsService.businessDate;
    this.maskedAadhaar = this.clientDataAndTemplate.aadhaar;
    this.aadhaar= this.clientDataAndTemplate.aadhaar;
    console.log("this.aadhaar",this.aadhaar);
    this.createEditClientForm();
    this.setOption();
    this.buildDependencies();
    this.editClientForm.patchValue({
      'officeId': this.clientDataAndTemplate.officeId,
      'staffId': this.clientDataAndTemplate.staffId,
      'legalFormId': this.clientDataAndTemplate.legalForm && this.clientDataAndTemplate.legalForm.id,
      'accountNo': this.clientDataAndTemplate.accountNo,
      'externalId': this.clientDataAndTemplate.externalId,
      'genderId': this.clientDataAndTemplate.gender && this.clientDataAndTemplate.gender.id,
      'age':this.clientDataAndTemplate.age,
      'stateId': this.clientDataAndTemplate.state && this.clientDataAndTemplate.state.id,
      'isStaff': this.clientDataAndTemplate.isStaff,
      'active': this.clientDataAndTemplate.active,
      'mobileNo': this.clientDataAndTemplate.mobileNo,
      'city':this.clientDataAndTemplate.city,
      'emailAddress': this.clientDataAndTemplate.emailAddress,
      'dateOfBirth': this.clientDataAndTemplate.dateOfBirth && new Date(this.clientDataAndTemplate.dateOfBirth),
      'clientTypeId': this.clientDataAndTemplate.chargeTypeSelected && this.clientDataAndTemplate.chargeTypeSelected.id,
      'clientClassificationId': this.clientDataAndTemplate.clientClassification && this.clientDataAndTemplate.clientClassification.id,
      'submittedOnDate': this.clientDataAndTemplate.timeline.submittedOnDate && new Date(this.clientDataAndTemplate.timeline.submittedOnDate),
      'activationDate': this.clientDataAndTemplate.timeline.activatedOnDate && new Date(this.clientDataAndTemplate.timeline.activatedOnDate),
    //  'repaymentModeId':this.clientDataAndTemplate.repaymentMode && this.clientDataAndTemplate.repaymentMode.id,
//       'transactionTypePreferenceId':this.clientDataAndTemplate.transactionTypePreference && this.clientDataAndTemplate.transactionTypePreference.id,
      'beneficiaryName':this.clientDataAndTemplate.beneficiaryName,
      'beneficiaryAccountNumber':this.clientDataAndTemplate.beneficiaryAccountNumber,
      'ifscCode':this.clientDataAndTemplate.ifscCode,
      'micrCode':this.clientDataAndTemplate.micrCode,
      'swiftCode':this.clientDataAndTemplate.swiftCode,
      'branch':this.clientDataAndTemplate.branch,
      'pan':this.clientDataAndTemplate.pan,
//        'aadhaar':this.clientDataAndTemplate.aadhaar,
       'voterId':this.clientDataAndTemplate.voterId,
       'passportNumber':this.clientDataAndTemplate.passportNumber,
       'drivingLicense':this.clientDataAndTemplate.drivingLicense,
       'accountTypeId': this.clientDataAndTemplate.accountType && this.clientDataAndTemplate.accountType.id,
       'address': this.clientDataAndTemplate.address,
       'pincode':this.clientDataAndTemplate.pincode,
       'rationCardNumber':this.clientDataAndTemplate.rationCardNumber


    });
  }

  get clientGeneralForm() {
      return this.clientGeneralStep.createClientForm;
    }

     get clientBeneficiaryForm() {
        return this.clientBeneficiaryStep.beneficiaryDetailsForm;
      }

      get client()
      {
      if (this.clientTemplate) {
                return {
                  ...this.clientGeneralStep.clientGeneralDetails,
                        ...this.clientBeneficiaryStep.beneficiaryDetails,
                  };
                  }

                  else{
                  return{
                  ...this.clientGeneralStep.clientGeneralDetails,
                  };

                  }

      }

  /**
   * Creates the edit client form.
   */
  createEditClientForm() {
    this.editClientForm = this.formBuilder.group({
      'officeId': [{ value: '', disabled: true }],
      'staffId': [''],
      'legalFormId': [''],
      'isStaff': [false],
      'active': [false],
      'accountNo': [{ value: '', disabled: true }],
      'externalId': [''],
      'genderId': ['',Validators.required],
      'age':['',Validators.required],
      'city':['',Validators.required],
      'stateId': ['', Validators.required],
      'mobileNo': ['',Validators.required],
      'emailAddress': ['', Validators.email],
      'dateOfBirth': ['',Validators.required],
      'clientTypeId': [''],
      'clientClassificationId': [''],
      'submittedOnDate': ['',Validators.required],
      'activationDate': ['',Validators.required],
     // 'repaymentModeId':[''],
//       'transactionTypePreferenceId':[''],
      'beneficiaryName':[''],
      'beneficiaryAccountNumber':['',Validators.required],
      'ifscCode':['',Validators.required],
      'micrCode':[''],
      'swiftCode':[''],
      'branch':[''],
      'pan':[''],
//       'aadhaar':[''],
      'address':['',Validators.required],
      'accountTypeId':['',Validators.required],
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
  setOption() {
    this.officeOptions = this.clientDataAndTemplate.officeOptions;
    this.staffOptions = this.clientDataAndTemplate.staffOptions;
    this.legalFormOptions = this.clientDataAndTemplate.clientLegalFormOptions;
    this.clientTypeOptions = this.clientDataAndTemplate.clientTypeOptions;
    this.clientClassificationTypeOptions = this.clientDataAndTemplate.clientClassificationOptions;
    this.businessLineOptions = this.clientDataAndTemplate.clientNonPersonMainBusinessLineOptions;
    this.constitutionOptions = this.clientDataAndTemplate.clientNonPersonConstitutionOptions;
    this.genderOptions = this.clientDataAndTemplate.genderOptions;
    this.stateOptions = this.clientDataAndTemplate.stateOptions;
    //this.repaymentModeOptions = this.clientDataAndTemplate.repaymentModeOptions;
//     this.transactionTypePreferenceOptions = this.clientDataAndTemplate.transactionTypePreferenceOptions;
    this.accountTypeOptions = this.clientDataAndTemplate.accountTypeOptions;

  }

  /**
   * Adds controls conditionally.
   */
  buildDependencies() {
    this.editClientForm.get('legalFormId').valueChanges.subscribe((legalFormId: any) => {
      if (legalFormId === 1) {
        this.editClientForm.removeControl('fullname');
        this.editClientForm.removeControl('clientNonPersonDetails');
        this.editClientForm.addControl('firstname', new UntypedFormControl(this.clientDataAndTemplate.firstname, Validators.required));
        this.editClientForm.addControl('middlename', new UntypedFormControl(this.clientDataAndTemplate.middlename));
        this.editClientForm.addControl('lastname', new UntypedFormControl(this.clientDataAndTemplate.lastname, Validators.required));
      } else {
        this.editClientForm.removeControl('firstname');
        this.editClientForm.removeControl('middlename');
        this.editClientForm.removeControl('lastname');
        this.editClientForm.addControl('fullname', new UntypedFormControl(this.clientDataAndTemplate.fullname, Validators.required));
        this.editClientForm.addControl('clientNonPersonDetails', this.formBuilder.group({
          'constitutionId': [this.clientDataAndTemplate.clientNonPersonDetails.constitution && this.clientDataAndTemplate.clientNonPersonDetails.constitution.id],
          'incorpValidityTillDate': [this.clientDataAndTemplate.clientNonPersonDetails.incorpValidityTillDate && new Date(this.clientDataAndTemplate.clientNonPersonDetails.incorpValidityTillDate)],
          'incorpNumber': [this.clientDataAndTemplate.clientNonPersonDetails.incorpNumber],
          'mainBusinessLineId': [this.clientDataAndTemplate.clientNonPersonDetails.mainBusinessLine && this.clientDataAndTemplate.clientNonPersonDetails.mainBusinessLine.id],
          'remarks': [this.clientDataAndTemplate.clientNonPersonDetails.remarks]
        }));
      }
    });
  }

  /**
   * Submits the edit client form.
   */
  submit() {
    const locale = this.settingsService.language.code;
    const dateFormat = this.settingsService.dateFormat;
    const editClientFormValue: any = this.editClientForm.getRawValue();
    const clientData = {
      ...editClientFormValue,
      dateOfBirth: editClientFormValue.dateOfBirth && this.dateUtils.formatDate(editClientFormValue.dateOfBirth, dateFormat),
      submittedOnDate: editClientFormValue.submittedOnDate && this.dateUtils.formatDate(editClientFormValue.submittedOnDate, dateFormat),
      activationDate: this.dateUtils.formatDate(editClientFormValue.activationDate, dateFormat),
      dateFormat,
      locale
    };
    delete clientData.officeId;
    if (editClientFormValue.clientNonPersonDetails) {
      clientData.clientNonPersonDetails = {
        ...editClientFormValue.clientNonPersonDetails,
        incorpValidityTillDate: editClientFormValue.clientNonPersonDetails.incorpValidityTillDate && this.dateUtils.formatDate(editClientFormValue.clientNonPersonDetails.incorpValidityTillDate, dateFormat),
        dateFormat,
        locale
      };
    } else {
      clientData.clientNonPersonDetails = {};
    }

    if (this.maskedAadhaar === clientData.aadhaar) {
      delete clientData.aadhaar;
    }
    this.clientsService.updateClient(this.clientDataAndTemplate.id, clientData).subscribe(() => {
      this.router.navigate(['../'], { relativeTo: this.route });
      },error=>{
                   console.log(error.error.errors,"response.error]");
                   for(let i=0;i<error.error.errors.length;i++){
                           this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
                           }
    });
  }

}

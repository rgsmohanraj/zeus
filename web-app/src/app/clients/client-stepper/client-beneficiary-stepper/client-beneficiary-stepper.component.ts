import { Component, OnInit,Input } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormControl } from '@angular/forms';
import { Dates } from 'app/core/utils/dates';
import { SettingsService } from 'app/settings/settings.service';


@Component({
  selector: 'mifosx-client-beneficiary-stepper',
  templateUrl: './client-beneficiary-stepper.component.html',
  styleUrls: ['./client-beneficiary-stepper.component.scss']
})
export class ClientBeneficiaryStepperComponent implements OnInit {

@Input() clientTemplate: any
beneficiaryDetailsForm : UntypedFormGroup;


//repaymentModeOptions : any;
accountTypeOptions:any;
 beneficiaryName : any;
 beneficiaryAccountNumber : any;
 ifscCode : any;
 micrCode : any;
 swiftCode : any;
 branch : any;

  /**
   * @param {FormBuilder} formBuilder Form Builder
   * @param {Dates} dateUtils Date Utils
   * @param {SettingsService} settingsService Setting service
   */

constructor(private formBuilder: UntypedFormBuilder,
                          private dateUtils: Dates,
                          private settingsService: SettingsService) {


                       this.setBeneficiaryform();

                           }


ngOnInit()  {
  console.log(this.clientTemplate,"this.clientTemplate");
   this.beneficiaryName = this.clientTemplate.beneficiaryName;
    this.beneficiaryAccountNumber = this.clientTemplate.beneficiaryAccountNumber;
    this.ifscCode = this.clientTemplate.ifscCode;
    this.micrCode = this.clientTemplate.micrCode;
    this.swiftCode = this.clientTemplate.swiftCode;
    this.branch = this.clientTemplate.branch;
  this.setOption();

   this.beneficiaryDetailsForm.patchValue({

      'beneficiaryName': this.clientTemplate.beneficiaryName,
       'beneficiaryAccountNumber' :this.clientTemplate.beneficiaryAccountNumber,
       'ifscCode': this.clientTemplate.ifscCode,
       'micrCode':this.clientTemplate.micrCode,
       'swiftCode':this.clientTemplate.swiftCode,
       'branch': this.clientTemplate.branch,
     //  'repaymentModeId':this.clientTemplate.repaymentModeId && this.clientTemplate.repaymentModeId.id,
//        'transactionTypePreferenceId' : this.clientTemplate.transactionTypePreferenceId && this.clientTemplate.transactionTypePreferenceId.id,
       'accountTypeId' : this.clientTemplate.accountTypeId && this.clientTemplate.accountTypeId.id

    });
      }



   allowAlphabetAndNums(e)
   {
              let k;
                document.all ? k = e.keyCode : k = e.which;
                return((k > 64 && k < 91) || (k > 96 && k < 123) || k == 8 || k == 32 || (k >= 48 && k <= 57));
                       }

                        numberOnly(event): boolean {
                                          const charCode = (event.which) ? event.which : event.keyCode;
                                                 if (charCode > 31 && (charCode < 48 || charCode > 57)) {
                                                              return false;
                                                                       }
                                                                     return true;
                                                                          }

  setBeneficiaryform(){
  this.beneficiaryDetailsForm = this.formBuilder.group({

       //  'repaymentModeId': [''],
//          'transactionTypePreferenceId': [''],
         'beneficiaryName': [''],
         'beneficiaryAccountNumber': ['',Validators.required],
         'ifscCode':  ['',Validators.required],
         'micrCode': [''],
         'swiftCode': [''],
         'branch': [''],
         'accountTypeId':['',Validators.required],
            });
  }

  setOption()
  {
 // this.repaymentModeOptions = this.clientTemplate.repaymentModeOptions;
//    this.transactionTypePreferenceOptions = this.clientTemplate.transactionTypePreferenceOptions;
   this.accountTypeOptions = this.clientTemplate.accountTypeOptions;

  }

  get beneficiaryDetails() {
  const beneficiaryDetails = this.beneficiaryDetailsForm.value;
  return beneficiaryDetails;
  }


}

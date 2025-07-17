import { Component, OnInit, Input} from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormControl } from '@angular/forms';
import { Dates } from 'app/core/utils/dates';
import { SettingsService } from 'app/settings/settings.service';

@Component({
  selector: 'mifosx-office-basic-details',
  templateUrl: './office-basic-details.component.html',
  styleUrls: ['./office-basic-details.component.scss']
})
export class OfficeBasicDetailsComponent implements OnInit {

@Input() officeTemplate: any;
// @Input() office: any;
/** Minimum date allowed. */
  minDate = new Date(2000, 0, 1);
    maxDate = new Date(new Date().setFullYear(new Date().getFullYear() + 10));

  createOfficeBasicForm: UntypedFormGroup;

  name : any;
  allowedParentsOptions : any;
  openingDate : any;
  externalId : any;

  /**
       * @param {FormBuilder} formBuilder Form Builder.
       * @param {Dates} dateUtils Date Utils.
       * @param {SettingsService} settingsService Settings Service.
       */

  constructor(private formBuilder: UntypedFormBuilder,
              private dateUtils: Dates,
              private settingsService: SettingsService)
              {
               this.setOfficeBasicForm();
              }

  ngOnInit(): void
  {

   this.name = this.officeTemplate.name;
   this.allowedParentsOptions = this.officeTemplate.allowedParents;
   this.openingDate = this.officeTemplate.openingDate;
   this.externalId = this.officeTemplate.externalId;

   console.log(this.officeTemplate,"officeTemplate")

   this.createOfficeBasicForm.patchValue({

   'name' :this.officeTemplate.name,
   'parentId' : this.officeTemplate.parentId,
   'openingDate' :this.officeTemplate.openingDate && new Date(this.officeTemplate.openingDate),
   'externalId' :this.officeTemplate.externalId,

   });
   }
   setOfficeBasicForm() {
        this.createOfficeBasicForm = this.formBuilder.group({
          'name': [''],
          'parentId': [''],
          'openingDate': [''],
          'externalId': [''],
        });
        }

     get OfficeBasicDetails() {
        const generalDetails = this.createOfficeBasicForm.value;
        const dateFormat = this.settingsService.dateFormat;
        const locale = this.settingsService.language.code;

        if (generalDetails.openingDate instanceof Date) {
             generalDetails.openingDate = this.dateUtils.formatDate(generalDetails.openingDate, dateFormat);
            }
      return generalDetails;
     }
}

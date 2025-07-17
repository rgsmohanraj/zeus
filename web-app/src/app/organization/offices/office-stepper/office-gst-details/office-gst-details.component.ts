import { Component, OnInit, Input} from '@angular/core';
import { UntypedFormGroup, UntypedFormBuilder, Validators, UntypedFormArray, FormControl } from '@angular/forms';
import { Dates } from 'app/core/utils/dates';
import { SettingsService } from 'app/settings/settings.service';

@Component({
  selector: 'mifosx-office-gst-details',
  templateUrl: './office-gst-details.component.html',
  styleUrls: ['./office-gst-details.component.scss']
})
export class OfficeGstDetailsComponent implements OnInit {


@Input() officeTemplate: any;

createOfficeGstForm: UntypedFormGroup;

  gstNumber :any;
  stateOptions :any;
  cgst : any;
  sgst : any;
  igst :any;

      /**
       * @param {FormBuilder} formBuilder Form Builder.
       * @param {Dates} dateUtils Date Utils.
       * @param {SettingsService} settingsService Settings Service.
       */

  constructor( private formBuilder: UntypedFormBuilder,
                 private dateUtils: Dates,
              private settingsService: SettingsService)
                    {
//                      this.setOfficeGstForm();
                          }

  ngOnInit(): void {

    this.stateOptions = this.officeTemplate.stateOptions;
this.createOfficeGstForm = this.formBuilder.group({
      officeGsts: this.formBuilder.array([])
    });
    if(this.officeTemplate.officeGsts){
    for(let i=0; i < this.officeTemplate.officeGsts.length; i++) {
    this.officeGsts.push(
                this.formBuilder.group({
                  gstNumber: [this.officeTemplate.officeGsts[i].gstNumber],
                  state: [this.officeTemplate.officeGsts[i].state.id],
                  cgst: [this.officeTemplate.officeGsts[i].cgst],
                  sgst: [this.officeTemplate.officeGsts[i].sgst],
                  igst: [this.officeTemplate.officeGsts[i].igst]
                })
            )
          }
          }
  }
get officeGsts(): UntypedFormArray {
    return this.createOfficeGstForm.get('officeGsts') as UntypedFormArray;
  }
addAffectedGLEntry() {
  this.officeGsts.push(this.formBuilder.group({
    gstNumber: '',
    state: '',
    cgst: '',
    sgst: '',
    igst: ''
  }));
}
removeAffectedGLEntry(index: number) {
  this.officeGsts.removeAt(index);
}
 get OfficeGstDetails() {
        const gstDetails = this.createOfficeGstForm.value;
        const dateFormat = this.settingsService.dateFormat;
        const locale = this.settingsService.language.code;
      return gstDetails;
     }
}

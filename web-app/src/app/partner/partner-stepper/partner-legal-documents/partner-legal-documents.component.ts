import { Component, OnInit, Input } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormControl } from '@angular/forms';

import { SettingsService } from 'app/settings/settings.service';

@Component({
  selector: 'mifosx-partner-legal-documents',
  templateUrl: './partner-legal-documents.component.html',
  styleUrls: ['./partner-legal-documents.component.scss']
})
export class PartnerLegalDocumentsComponent implements OnInit {
@Input() partnerTemplate: any;
createLegalDocumentsForm: UntypedFormGroup;
  constructor(
  private formBuilder: UntypedFormBuilder,
  private settingsService: SettingsService) {
   this.setLegalDocumentsForm();
   }

  ngOnInit(): void {
   }
   setLegalDocumentsForm() {
          this.createLegalDocumentsForm = this.formBuilder.group({

          });
}
get partnerLegalDocuments() {
        const partnerLegalDocuments = this.createLegalDocumentsForm.value;

      return partnerLegalDocuments;
      }
}

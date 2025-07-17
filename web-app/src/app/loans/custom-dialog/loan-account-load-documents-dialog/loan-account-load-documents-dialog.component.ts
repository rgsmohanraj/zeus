import { Component, OnInit, Inject } from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef, MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import { UntypedFormGroup, UntypedFormBuilder, Validators, FormControl } from '@angular/forms';

@Component({
  selector: 'mifosx-loan-account-load-documents-dialog',
  templateUrl: './loan-account-load-documents-dialog.component.html',
  styleUrls: ['./loan-account-load-documents-dialog.component.scss']
})
export class LoanAccountLoadDocumentsDialogComponent implements OnInit {

  /** Upload Document form. */
  uploadDocumentForm: UntypedFormGroup;
  uploadDocumentData: any = [];
 documentIdentifier = false;
  /**
   * @param {MatDialogRef} dialogRef Dialog reference element
   * @param {FormBuilder} formBuilder Form Builder
   * @param {any} data Dialog Data
   */
   constructor(public dialogRef: MatDialogRef<LoanAccountLoadDocumentsDialogComponent>,
                 private formBuilder: UntypedFormBuilder,
                 @Inject(MAT_DIALOG_DATA) public data: any) {
       this.documentIdentifier = data.documentIdentifier;
     }


  ngOnInit() {
    this.createUploadDocumentForm();
  }

  /**
   * Creates the upload Document form.
   */
  createUploadDocumentForm() {
    this.uploadDocumentForm = this.formBuilder.group({
      'name': ['', Validators.required],
      'description': [''],
      'file': ['', Validators.required]
    });
  }

  /**
   * Sets file form control value.
   * @param {any} $event file change event.
   */
  onFileSelect($event: any) {
    if ($event.target.files.length > 0) {
      const file = $event.target.files[0];
      console.log(file,"file");
      this.uploadDocumentForm.get('file').setValue(file);
    }
  }

}

/** Angular Imports */
import { Component, OnInit, Inject } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { UntypedFormGroup, UntypedFormBuilder, Validators } from '@angular/forms';

/**
 * Report Parameter Dialog Component.
 */
@Component({
  selector: 'mifosx-add-report-parameter-dialog',
  templateUrl: './report-parameter-dialog.component.html',
  styleUrls: ['./report-parameter-dialog.component.scss']
})
export class ReportParameterDialogComponent implements OnInit {

  /** Report Parameter Form. */
  reportParameterForm: UntypedFormGroup;

  /**
   * @param {MatDialogRef} dialogRef Component reference to dialog.
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {any} data Provides the allowed parameters and values for the form (if available).
   */
  constructor(public dialogRef: MatDialogRef<ReportParameterDialogComponent>,
              public formBuilder: UntypedFormBuilder,
              @Inject(MAT_DIALOG_DATA) public data: any) {
  }

  /**
   * Creates the add report parameter form.
   */
  ngOnInit() {
    this.reportParameterForm = this.formBuilder.group({
      'parameterName': [this.data.parameterName, Validators.required],
      'reportParameterName': [this.data.reportParameterName]
    });
  }

  /**
   * Closes the dialog and returns value of the form.
   */
  submit() {
    this.dialogRef.close(this.reportParameterForm.value);
  }

}

/** Angular Imports */
import { Component, OnInit, Inject } from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef, MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';
import { UntypedFormControl } from '@angular/forms';

/**
 * Revert transaction dialog component.
 */
@Component({
  selector: 'mifosx-revert-transaction',
  templateUrl: './revert-transaction.component.html',
  styleUrls: ['./revert-transaction.component.scss']
})
export class RevertTransactionComponent implements OnInit {

  /** Comments input form control. */
  comments = new UntypedFormControl('');

  /**
   * @param {MatDialogRef} dialogRef Component reference to dialog.
   * @param {any} data Provides comments or reverted transaction ID.
   */
  constructor(public dialogRef: MatDialogRef<RevertTransactionComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit() {
  }

}

/** Angular Imports */
import { Component, OnInit, Inject } from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef, MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA } from '@angular/material/legacy-dialog';

/**
 * Enable dialog component.
 */
@Component({
  selector: 'mifosx-enable-dialog',
  templateUrl: './enable-dialog.component.html',
  styleUrls: ['./enable-dialog.component.scss']
})
export class EnableDialogComponent implements OnInit {

  /**
   * @param {MatDialogRef} dialogRef Component reference to dialog.
   * @param {any} data Provides a enableContext.
   */
  constructor(public dialogRef: MatDialogRef<EnableDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit() {
  }

}

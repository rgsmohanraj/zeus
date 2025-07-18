/** Angular Imports */
import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';

/** Custom Components */
import { FormDialogComponent } from 'app/shared/form-dialog/form-dialog.component';
import { DeleteDialogComponent } from 'app/shared/delete-dialog/delete-dialog.component';

/** Custom Models */
import { FormfieldBase } from 'app/shared/form-dialog/formfield/model/formfield-base';

/** Custom Services */
import { OrganizationService } from '../../../../organization.service';
import { SettingsService } from 'app/settings/settings.service';
import { Dates } from 'app/core/utils/dates';
import { Datatables } from 'app/core/utils/datatables';

/**
 * Offices Single Row Data Tables
 */
@Component({
  selector: 'mifosx-single-row',
  templateUrl: './single-row.component.html',
  styleUrls: ['./single-row.component.scss']
})
export class SingleRowComponent implements OnInit {

  /** Data Object */
  @Input() dataObject: any;

  /** Data Table Name */
  datatableName: string;
  /** Office Id */
  officeId: string;

  /**
   * Fetches office Id from parent route params.
   * @param {ActivatedRoute} route Activated Route.
   * @param {Dates} dateUtils Date Utils.
   * @param {OrganizationService} organizationService Organization Service.
   * @param {SettingsService} settingsService Settings Service.
   * @param {MatDialog} dialog Mat Dialog.
   */
  constructor(private route: ActivatedRoute,
              private dateUtils: Dates,
              private dialog: MatDialog,
              private organizationService: OrganizationService,
              private settingsService: SettingsService,
              private datatables: Datatables) {
    this.officeId = this.route.parent.parent.snapshot.paramMap.get('id');
  }

  /**
   * Fetches data table name from route params.
   * subscription is required due to asynchronicity.
   */
  ngOnInit() {
    this.route.params.subscribe((routeParams: any) => {
      this.datatableName = routeParams.datatableName;
    });
  }

  /**
   * Creates a new instance of the given single row data table.
   */
  add() {
    let dataTableEntryObject: any = { locale: this.settingsService.language.code };
    const dateTransformColumns: string[] = [];
    const columns = this.dataObject.columnHeaders.filter((column: any) => {
      return ((column.columnName !== 'id') && (column.columnName !== 'office_id') && (column.columnName !== 'created_at') && (column.columnName !== 'updated_at'));
    });
    const formfields: FormfieldBase[] = this.datatables.getFormfields(columns, dateTransformColumns, dataTableEntryObject);
    const data = {
      title: 'Add ' + this.datatableName,
      formfields: formfields
    };
    const addDialogRef = this.dialog.open(FormDialogComponent, { data });
    addDialogRef.afterClosed().subscribe((response: any) => {
      if (response.data) {
        dateTransformColumns.forEach((column) => {
          response.data.value[column] = this.dateUtils.formatDate(response.data.value[column], dataTableEntryObject.dateFormat);
        });
        dataTableEntryObject = { ...response.data.value, ...dataTableEntryObject };
        this.organizationService.addOfficeDatatableEntry(this.officeId, this.datatableName, dataTableEntryObject).subscribe(() => {
          this.organizationService.getOfficeDatatable(this.officeId, this.datatableName).subscribe((dataObject: any) => {
            this.dataObject = dataObject;
          });
        });
      }
    });
  }

  /**
   * Edits the current instance of single row data table.
   */
  edit() {
    let dataTableEntryObject: any = { locale: this.settingsService.language.code };
    const dateTransformColumns: string[] = [];
    const columns = this.dataObject.columnHeaders.filter((column: any) => {
      return ((column.columnName !== 'id') && (column.columnName !== 'office_id'));
    });
    let formfields: FormfieldBase[] = this.datatables.getFormfields(columns, dateTransformColumns, dataTableEntryObject);
    formfields = formfields.map((formfield: FormfieldBase, index: number) => {
      formfield.value = (this.dataObject.data[0].row[index + 1]) ? this.dataObject.data[0].row[index + 1] : '';
      return formfield;
    });
    const data = {
      title: 'Edit ' + this.datatableName,
      layout: { addButtonText: 'Confirm' },
      formfields: formfields
    };
    const editDialogRef = this.dialog.open(FormDialogComponent, { data });
    editDialogRef.afterClosed().subscribe((response: any) => {
      if (response.data) {
        dateTransformColumns.forEach((column) => {
          response.data.value[column] = this.dateUtils.formatDate(response.data.value[column], dataTableEntryObject.dateFormat);
        });
        dataTableEntryObject = { ...response.data.value, ...dataTableEntryObject };
        this.organizationService.editOfficeDatatableEntry(this.officeId, this.datatableName, dataTableEntryObject).subscribe(() => {
          this.organizationService.getOfficeDatatable(this.officeId, this.datatableName).subscribe((dataObject: any) => {
            this.dataObject = dataObject;
          });
        });
      }
    });
  }

  /**
   * Deletes the current instance of single row data table.
   */
  delete() {
    const deleteDataTableDialogRef = this.dialog.open(DeleteDialogComponent, {
      data: { deleteContext: `the contents of ${this.datatableName}` }
    });
    deleteDataTableDialogRef.afterClosed().subscribe((response: any) => {
      if (response.delete) {
        this.organizationService.deleteDatatableContent(this.officeId, this.datatableName)
          .subscribe(() => {
            this.organizationService.getOfficeDatatable(this.officeId, this.datatableName).subscribe((dataObject: any) => {
              this.dataObject = dataObject;
            });
          });
      }
    });
  }

}

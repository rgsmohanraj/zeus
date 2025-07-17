/** Angular Imports */
import { Component, OnChanges, OnInit, Input, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatLegacyTable as MatTable } from '@angular/material/legacy-table';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';

/** Custom Dialogs */
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
 * Office Multi Row Data Tables
 */
@Component({
  selector: 'mifosx-multi-row',
  templateUrl: './multi-row.component.html',
  styleUrls: ['./multi-row.component.scss']
})
export class MultiRowComponent implements OnInit, OnChanges {

  /** Data Object */
  @Input() dataObject: any;

  /** Data Table Name */
  datatableName: string;
  /** Data Table Columns */
  datatableColumns: string[] = [];
  /** Data Table Data */
  datatableData: any;
  /** Office Id */
  officeId: string;
  /** Toggle button visibility */
  showDeleteBotton: boolean;

  /** Data Table Reference */
  @ViewChild('dataTable', { static: true }) dataTableRef: MatTable<Element>;

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
              private organizationService: OrganizationService,
              private settingsService: SettingsService,
              private dialog: MatDialog,
              private datatables: Datatables) {
    this.officeId = this.route.parent.parent.snapshot.paramMap.get('id');
  }

  /**
   * Updates related variables on changes to dataObject.
   */
  ngOnChanges() {
    this.datatableColumns = this.dataObject.columnHeaders.map((columnHeader: any) => {
      return columnHeader.columnName;
    });
    this.datatableData = this.dataObject.data;
    this.showDeleteBotton = this.datatableData[0] ? true : false;
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
   * Adds a new row to the given multi row data table.
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
            this.datatableData = dataObject.data;
            this.dataTableRef.renderRows();
          });
        });
      }
    });
  }

  /**
   * Deletes all rows of the given multi row data table.
   */
  delete() {
    const deleteDataTableDialogRef = this.dialog.open(DeleteDialogComponent, {
      data: { deleteContext: `the contents of ${this.datatableName}` }
    });
    deleteDataTableDialogRef.afterClosed().subscribe((response: any) => {
      if (response.delete) {
        this.organizationService.deleteDatatableContent(this.officeId, this.datatableName).subscribe(() => {
          this.organizationService.getOfficeDatatable(this.officeId, this.datatableName).subscribe((dataObject: any) => {
            this.datatableData = dataObject.data;
            this.showDeleteBotton = false;
            this.dataTableRef.renderRows();
           });
        });
      }
    });
  }

}

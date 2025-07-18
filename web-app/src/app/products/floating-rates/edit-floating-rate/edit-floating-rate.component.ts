/** Angular Imports */
import { Component, OnInit, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormArray } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacyPaginator as MatPaginator } from '@angular/material/legacy-paginator';
import { MatSort } from '@angular/material/sort';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';

/** Custom Services */
import { ProductsService } from '../../products.service';
import { SettingsService } from 'app/settings/settings.service';

/** Custom Components */
import { FloatingRatePeriodDialogComponent } from '../floating-rate-period-dialog/floating-rate-period-dialog.component';
import { DeleteDialogComponent } from 'app/shared/delete-dialog/delete-dialog.component';
import { Dates } from 'app/core/utils/dates';

/**
 * Edit Floating Rate Component.
 */
@Component({
  selector: 'mifosx-edit-floating-rate',
  templateUrl: './edit-floating-rate.component.html',
  styleUrls: ['./edit-floating-rate.component.scss']
})
export class EditFloatingRateComponent implements OnInit {

  /** Floating Rate Form. */
  floatingRateForm: UntypedFormGroup;
  /** Floating Rate Data. */
  floatingRateData: any;
  /** Minimum floating rate period date allowed. */
  minDate = new Date();
  /** Form Pristine Status. */
  isFloatingRateFormPristine = true;
  /** Columns to be displayed in floating rate periods table. */
  displayedColumns: string[] = ['fromDate', 'interestRate', 'isDifferential', 'actions'];
  /** Data source for floating rate periods table. */
  dataSource: MatTableDataSource<any>;
  /** Date Format. */
  dateFormat = this.settingsService.dateFormat;
  /** Floating Rate Period Data. */
  floatingRatePeriodsData: any[] = [];

  /** Paginator for floating rate periods table. */
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  /** Sorter for floating rate periods table. */
  @ViewChild(MatSort, { static: true }) sort: MatSort;

  /**
   * Retrieves the floating rate data from `resolve`.
   * @param {Router} router Router for navigation.
   * @param {FormBuilder} formBuilder Form Builder.
   * @param {ProductsService} productsService Product Service.
   * @param {ActivatedRoute} route Activated Route.
   * @param {Dates} dateUtils Date Utils.
   * @param {MatDialog} dialog Dialog reference.
   * @param {SettingsService} settingsService Settings Service.
   */
  constructor(private router: Router,
              private formBuilder: UntypedFormBuilder,
              private productsService: ProductsService,
              private route: ActivatedRoute,
              private dateUtils: Dates,
              private dialog: MatDialog,
              private settingsService: SettingsService) {
    this.route.data.subscribe((data: { floatingRate: any }) => {
      this.floatingRateData = data.floatingRate;
      this.floatingRatePeriodsData = data.floatingRate.ratePeriods ? data.floatingRate.ratePeriods : [];
    });
  }

  /**
   * Sets the floating rate periods table.
   */
  ngOnInit() {
    this.createFloatingRateForm();
    this.setFloatingRates();
  }

  /**
   * Creates and sets the Floating Rate Form.
   */
  createFloatingRateForm() {
    this.floatingRateForm = this.formBuilder.group({
      'name': [this.floatingRateData.name, Validators.required],
      'isBaseLendingRate': [this.floatingRateData.isBaseLendingRate],
      'isActive': [this.floatingRateData.isActive]
    });
  }

  /**
   * Initializes the data source, paginator and sorter for floating rate periods table.
   */
  setFloatingRates() {
    this.dataSource = new MatTableDataSource(this.floatingRatePeriodsData);
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  /**
   * Adds a new floating rate period.
   */
  addFloatingRatePeriod() {
    const floatingRatePeriodDialogRef = this.dialog.open(FloatingRatePeriodDialogComponent, {
      data: {}
    });
    floatingRatePeriodDialogRef.afterClosed().subscribe((response: any) => {
      if (response) {
        this.floatingRatePeriodsData.push({
          fromDate: this.dateUtils.formatDate(response.fromDate, this.dateFormat),
          interestRate: response.interestRate,
          isDifferentialToBaseLendingRate: response.isDifferentialToBaseLendingRate,
          locale: this.settingsService.language.code,
          dateFormat: this.dateFormat
        });
        this.dataSource.connect().next(this.floatingRatePeriodsData);
        this.isFloatingRateFormPristine = false;
      }
    });
  }

  /**
   * Edits floating rate period.
   * @param {any} ratePeriod Floating Rate Period.
   */
  editFloatingRatePeriod(ratePeriod: any) {
    const editFloatingRatePeriodDialogRef = this.dialog.open(FloatingRatePeriodDialogComponent, {
      data: {
        fromDate: ratePeriod.fromDate,
        interestRate: ratePeriod.interestRate,
        isDifferentialToBaseLendingRate: ratePeriod.isDifferentialToBaseLendingRate
      }
    });
    editFloatingRatePeriodDialogRef.afterClosed().subscribe((response: any) => {
      if (response) {
        this.floatingRatePeriodsData[this.floatingRatePeriodsData.indexOf(ratePeriod)] = {
          fromDate: this.dateUtils.formatDate(response.fromDate, this.dateFormat),
          interestRate: response.interestRate,
          isDifferentialToBaseLendingRate: response.isDifferentialToBaseLendingRate,
          locale: this.settingsService.language.code,
          dateFormat: this.dateFormat
        };
        this.dataSource.connect().next(this.floatingRatePeriodsData);
        this.isFloatingRateFormPristine = false;
      }
    });
  }

  /**
   * Deletes the floating rate period.
   * @param {any} ratePeriod Floating Rate Period.
   */
  deleteFloatingRatePeriod(ratePeriod: any) {
    const deleteFloatingRatePeriodRef = this.dialog.open(DeleteDialogComponent, {
      data: { deleteContext: `floating rate period with from date as ${ratePeriod.fromDate}` }
    });
    deleteFloatingRatePeriodRef.afterClosed().subscribe((response: any) => {
      if (response.delete) {
        this.floatingRatePeriodsData.splice(this.floatingRatePeriodsData.indexOf(ratePeriod), 1);
        this.dataSource.connect().next(this.floatingRatePeriodsData);
        this.isFloatingRateFormPristine = false;
      }
    });
  }

  /**
   * Submits the floating rate form and creates floating rate,
   * if successful redirects to view created floating rate.
   */
  submit() {
    this.floatingRatePeriodsData.map(floatingRatePeriod => {
      floatingRatePeriod.modifiedOn = undefined;
      floatingRatePeriod.createdOn = undefined;
      floatingRatePeriod.id = undefined;
      floatingRatePeriod.modifiedBy = undefined;
      floatingRatePeriod.createdBy = undefined;
      floatingRatePeriod.isActive = undefined;
      floatingRatePeriod.locale = this.settingsService.language.code;
      floatingRatePeriod.dateFormat = this.dateFormat;
      floatingRatePeriod.fromDate = this.dateUtils.formatDate(floatingRatePeriod.fromDate, this.dateFormat);
    });
    this.floatingRateForm.value.ratePeriods = this.floatingRatePeriodsData.length > 0 ? this.floatingRatePeriodsData : undefined;
    this.productsService.updateFloatingRate(this.route.snapshot.paramMap.get('id'), this.floatingRateForm.value)
      .subscribe((response: any) => {
        this.router.navigate(['../../', response.resourceId], { relativeTo: this.route });
      });
  }

}

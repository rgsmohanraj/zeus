/** Angular Imports */
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { MatLegacyPaginator as MatPaginator } from '@angular/material/legacy-paginator';
import { MatLegacyTableDataSource as MatTableDataSource, MatLegacyTable as MatTable } from '@angular/material/legacy-table';
import { MatSort } from '@angular/material/sort';
import { Router, ActivatedRoute } from '@angular/router';
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormControl } from '@angular/forms';
import { DatePipe } from '@angular/common'
import { SettingsService } from 'app/settings/settings.service';
/** rxjs Imports */
import { merge } from 'rxjs';
import { tap, debounceTime, distinctUntilChanged, startWith, map } from 'rxjs/operators';
import { Dates } from 'app/core/utils/dates';
import { ViewAllLoansService } from './view-all-loans.service';
import { ViewAllLoansRoutingModule } from './view-all-loans-routing.module';
import { NotificationService } from '../notification.service';
import * as XLSX from 'xlsx';

@Component({
  selector: 'mifosx-view-all-loans',
  templateUrl: './view-all-loans.component.html',
  styleUrls: ['./view-all-loans.component.scss']
  })

export class ViewAllLoansComponent implements OnInit {
	@ViewChild('TABLE') table: ElementRef;
	/** Minimum date allowed. */
	minDate = new Date(1900, 0, 1);
	/** Maximum date allowed. */
	maxDate = new Date();
	dateFormat:any;
	/** Audit Trails Data */
	partnerData: any;
	productData: any;
	importsData: any;
	loans: any;
	csvDetails: any = [];
	displayedColumns: string[] = ['accountNo', 'externalId','name', 'amount',
    'status', 'disbursedate','pennyDrop','disbursement'];

	excelColumnHeaders: string[] = ['Loan Account No','External Id','Customer Name','Loan Amount','Status',
	'Disbursement Date','Penny Drop', 'Disbursement'];

	dataSource = new MatTableDataSource<any>();
	createViewAllLoansForm: UntypedFormGroup;

// 						 queryParamPartnerId: any;
         partnerId: any;
         productId: any;
         fromDate: any;
         toDate: any;

						 /** Paginator for audit trails table. */
						 @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
						 /** Sorter for audit trails table. */
						 @ViewChild(MatSort, { static: true }) sort: MatSort;


					 /**
						* @param {FormBuilder} formBuilde r Form Builder.
						* Retrieves the audit trail search template data from `resolve`.
						* @param {ActivatedRoute} route Activated Route.
						* @param {Dates} dateUtils Dates utils
						*/

  constructor (
				  private route: ActivatedRoute,
				  private router: Router,
				  private formBuilder: UntypedFormBuilder,
				  private dateUtils: Dates,
			    private settingsService: SettingsService,
				  private viewAllLoansService:ViewAllLoansService,
				  private notifyService: NotificationService )
                  {
							this.setBasicForm();
							this.route.data.subscribe((data: {viewAllLoansTemplate: any }) =>
                        {
					      this.partnerData = data.viewAllLoansTemplate;
						}
                 );
                  }

							ngOnInit(): void { }


						setBasicForm()
                             {
							      this.createViewAllLoansForm = this.formBuilder.group
                               ({
								   'partnerId': [''],
								  'productId': [''],
								  'fromDate': [''],
								  'toDate': [''],
							  });
                              }

							 someMethod(value)
                                {
// 								  this.queryParamPartnerId = value.partnerId;
								  this.productData = value.productInfoDataList;
								}


    search()
    {
    const generalDetails = this.createViewAllLoansForm.value;
               const data = this.createViewAllLoansForm.value;
               const dateFormat = this.settingsService.dateFormat;
               const locale = this.settingsService.language.code;
               const fromDate =this.dateUtils.formatDate(this.createViewAllLoansForm.value.fromDate, 'yyyy-MM-dd');
                const toDate = this.dateUtils.formatDate(this.createViewAllLoansForm.value.toDate,'yyyy-MM-dd');
//                 const fromDate = this.dateUtils.formatDate(this.createViewAllLoansForm.value.fromDate,dateFormat);
//                 const toDate = this.dateUtils.formatDate(this.createViewAllLoansForm.value.toDate,dateFormat);

		this.viewAllLoansService.getViewAllLoans(this.createViewAllLoansForm.value.partnerId.partnerId,
			this.createViewAllLoansForm.value.productId, fromDate, toDate)
			.subscribe((response: any) => {
				this.loans = response;
				this.dataSource.data = response;
				this.paginator._changePageSize(response.length);
				this.dataSource.paginator = this.paginator;
				this.dataSource.sort = this.sort;
			}, error => {
				for (let i = 0; i < error.error.errors.length; i++) {
					this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
				}
			}
			);

    }


   applyFilter(filterValue: string) {
		this.dataSource.filter = filterValue.trim().toLowerCase();
	}

	downloadCSV() {
	console.log("dataSource",this.dataSource);
		if(this.dataSource.data.length === 0 || this.dataSource.filteredData.length === 0) {
			this.notifyService.showWarning('No Data to download', 'Download')
			return;
		  }
		const ws: XLSX.WorkSheet=XLSX.utils.table_to_sheet(this.table.nativeElement);//converts a DOM TABLE element to a worksheet
        const wb: XLSX.WorkBook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Response');
        XLSX.writeFile(wb, 'Loans-Export.xlsx');
	}

}

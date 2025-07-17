/** Angular Imports */
import { Component,ElementRef,Input, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatLegacyPaginator as MatPaginator } from '@angular/material/legacy-paginator';
import { MatSort } from '@angular/material/sort';
import { MatLegacyTableDataSource as MatTableDataSource, MatLegacyTable as MatTable } from '@angular/material/legacy-table';
import { UntypedFormGroup, UntypedFormBuilder} from '@angular/forms';
import { Dates } from 'app/core/utils/dates';
import { SettingsService } from 'app/settings/settings.service';
/** Custom Imports */
import { OrganizationService } from '../../organization.service';
import { BulkImports } from './bulk-imports';
import { NotificationService } from '../../../notification.service';
import * as XLSX from 'xlsx';

/**
 * View Bulk Imports Component
 */
@Component({
  selector: 'mifosx-view-bulk-import',
  templateUrl: './view-bulk-import.component.html',
  styleUrls: ['./view-bulk-import.component.scss']
})
export class ViewBulkImportComponent implements OnInit {

 @ViewChild('TABLE') table: ElementRef;
 @Input() dataObject: any;
//   /** offices Data */
//   officeData: any;
//   /** staff Data */
//   staffData: any;
min
 maxDate = new Date();

  uploadShow=true;
  partnerData:any;
  productData:any;
  bulkLoans : any;
  bulkReportsLoan:any;
  enumType:any;
  reportsData:any;
  bulkDetails:any = [];
  bulkCollection: any;
  csv:any;
  csvDetails: any = [];
  csvDocumentDetails: any = [];

/** Maps column name to type */
  columnTypes: any[] = [];
  /** Entity Template */
  template: File;
  /** imports Data */
  importsData: any;
  /** bulk-import form. */
  bulkImportForm: UntypedFormGroup;
  /** array of deined bulk-imports */
  bulkImportsArray = BulkImports;
  /** bulk-import which user navigated to */
  bulkImport: any = {};
  /** Data source for imports table. */
  dataSource = new MatTableDataSource();
  /** Columns to be displayed in imports table. */
  // uploadDocumentDetails: any = {}
  // uploadedLoans: any =[]
  fileName: string;


  displayedColumns :string[]=
            [
              'name',
              'importTime',
              'endTime',
              'completed',
              'totalRecords',
              'successCount',
              'failureCount',
//               'download'
            ];

   displayedDetailsColumns: string[] =
    [
      'serialNo',
//       'uploadId',
      'externalId',
      'loanAccountNo',
      'status',
      'date',
      'reason',
//       'download'
    ];
 bulkColumns:string[]=
 [
   'serialNo',
   'name',
   'importTime',
   'totalRecords',
   'successCount',
   'failureCount',
   'download'
 ];

// displayedColumns:string[]=[];
excelColumnHeaders: string[] =
    [
      'Serial No',
//       'Upload Id',
      'External Id',
      'Loan Account No',
      'Status',
      'Reason',
      'Date',
//       'download'
    ];
    downloadExcelColumnHeaders: string[] =
        [
          'Serial No',
          'External Id',
          'Loan Account No',
          'Date',
          'Status',
          'Reason',

        ];

  /** Paginator for imports table. */
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  /** Sorter for imports table. */
  @ViewChild(MatSort, { static: true }) sort: MatSort;
  /** Imports table reference */
  @ViewChild('importsTable', { static: true }) importsTableRef: MatTable<Element>;

  /**
   * fetches offices and imports data from resolve
   * @param {ActivatedRoute} route ActivatedRoute
   * @param {FormBuilder} formBuilder FormBuilder
   * @param {OrganizationService} organizationService OrganizationService
   	* @param {Dates} dateUtils Dates utils
   */
  constructor(private route: ActivatedRoute,
              private formBuilder: UntypedFormBuilder,
              private organizationService: OrganizationService,
                private dateUtils: Dates,
              private settingsService: SettingsService,
               private notifyService: NotificationService
              ) {
    this.bulkImport.name = this.route.snapshot.params['import-name'];
    this.route.data.subscribe( (data: any) => {
        this.partnerData = data.officeData;
        this.enumType = data.officeData[0].enumOptionData;
        console.log("data",this.enumType);

//       this.officeData = data.offices;
//       this.importsData = data.imports;
    });
  }

  /**
   * Gets bulk import's properties.
   */
  ngOnInit() {
    this.bulkImport = this.bulkImportsArray.find( (entry) => entry.name === this.bulkImport.name);
    this.createBulkImportForm();
//     this.buildDependencies();
    this.setImports();

  }

  /**
   * Creates the bulk import form.
   */
  createBulkImportForm() {
    this.bulkImportForm = this.formBuilder.group({
      'partnerId': [''],
      'productId': [''],
      'staffId':[''],
      'legalForm': [''],
      'fromDate':[''],
      'toDate':[''],
      'typeId':[''],
    });
  }

  /**
   * Subscribe to value changes and fetches select options accordingly.
   */
  buildDependencies() {
    this.bulkImportForm.get('partnerId').valueChanges.subscribe((value: any) => {
      if (this.bulkImport.formFields >= 2) {
       this.organizationService.getProduct(value).subscribe( (data: any) =>
         {
//           this.staffData = data;
//           this.productData = data;
        });
      }
    });
  }

  /**
   * Initializes the data source, paginator and sorter for imports table.
   */
  setImports() {
    this.dataSource = new MatTableDataSource(this.importsData);
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  /**
   * Gets bulk import's downloadable template from API.
   */
  downloadTemplate() {
    const partnerId = this.bulkImportForm.get('partnerId').value.partnerId;
     const productId = this.bulkImportForm.get('productId').value;
    const staffId = this.bulkImportForm.get('staffId').value;
    let legalFormType = '';
    /** Only for Client Bulk Imports */
    switch (this.bulkImportForm.get('legalForm').value) {
      case 'Person':
          legalFormType = 'CLIENTS_PERSON';
        break;
      case 'Entity':
          legalFormType = 'CLIENTS_ENTTTY';
        break;
    }
    this.organizationService.getImportTemplate(this.bulkImport.urlSuffix, partnerId, productId, staffId, legalFormType).subscribe( (res: any) => {
      const contentType = res.headers.get('Content-Type');
      const blob = new Blob([res.body], { type: contentType });
      var url = window.URL.createObjectURL(blob);
      var anchor = document.createElement("a");
      anchor.download = 'Client_Loan_Template_' + new Date().toISOString() +".xlsx";
      anchor.href = url;
      anchor.click();
    });
  }
   /**
     * Gets bulk Collection downloadable template from API.
     */
    downloadCollectionTemplate() {
         const productId = this.bulkImportForm.get('productId').value;
      this.organizationService.getCollectionImportTemplate(this.bulkImport.urlSuffix,productId).subscribe( (res: any) => {
        const contentType = res.headers.get('Content-Type');
        const blob = new Blob([res.body], { type: contentType });
        var url = window.URL.createObjectURL(blob);
        var anchor = document.createElement("a");
        anchor.download = 'Collection_Template_' + new Date().toISOString() +".xlsx";
        anchor.href = url;
        anchor.click();
      });
    }

        code:any;
        typeCode(event)
        {
                this.code= event.code
        }
   searchReportsData()
       {
          const dateFormat = this.settingsService.dateFormat;
          const locale = this.settingsService.language.code;
          const fromDate =this.dateUtils.formatDate(this.bulkImportForm.value.fromDate, 'yyyy-MM-dd');
           const toDate = this.dateUtils.formatDate(this.bulkImportForm.value.toDate,'yyyy-MM-dd');
   		  this.organizationService.getReportsData(this.bulkImport.urlSuffix,fromDate, toDate,this.code)
   			.subscribe( (response: any) => {
   				this.bulkReportsLoan = response;
   				console.log(this.bulkReportsLoan , "response");
   				this.paginator._changePageSize(response.length);
   				this.dataSource.paginator = this.paginator;
   				this.dataSource.sort = this.sort;
   			},
   			 error => {
   				for (let i = 0; i < error.error.length; i++) {
   					this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
   				}
   			}
   			);
       }
    /**
    BulksReports Download call
    */
   downloadBulkReportDocument(documentDetails) {

       const sheet = this.convertJsonObjectToArray(documentDetails.importDetailsResponseData);
       const headers = this.downloadExcelColumnHeaders;
       console.log("headers", headers)
       let csv = sheet.map((object: any) => object.join());
       csv.unshift(`data:text/csv;charset=utf-8,${headers.join()}`);
       csv = csv.join('\r\n');
       const link = document.createElement('a');
       link.setAttribute('href', encodeURI(csv));
       const filename = documentDetails.name.substring(0,documentDetails.name.length-5);
       link.setAttribute('download', filename + '_response.csv');
       document.body.appendChild(link);
       link.click();
       document.body.removeChild(link);
   }

   convertJsonObjectToArray(documentDetails) {
    let sheet: any = [];
    let serialNo = 1;
    documentDetails.forEach(detail => {
      console.log("LoanId", ('loanId' in detail) ? detail.loanId : 0);
      let arr = [];
      arr.push(serialNo++);
      arr.push(detail.externalId);
      arr.push(detail.loanAccountNo);
//       arr.push(('loanId' in detail) ? detail.loanId : 0);
      arr.push(detail.date);
      arr.push(detail.status);
      arr.push(detail.reason);
      sheet.push(arr);
    });
    return sheet;
   }

  /**
   * Sets file form control value.
   * @param {any} $event file change event.
   */
  onFileSelect($event: any) {
    if ($event.target.files.length > 0) {
      this.uploadShow = false;
      this.template = $event.target.files[0];
      this.fileName = this.template.name.substring(0,this.template.name.length - 5);
    }
  }

  someMethod(value) {
      console.log("value", value)
		  this.productData = value.productInfoDataList;
	}

//   /**
//    * Upload excel file containing bulk import data(Old Function).
//    */
//   uploadTemplate() {
//
//     let legalFormType = '';
//     /** Only for Client Bulk Imports */
//     if (this.bulkImport.name === 'Clients') {
//       if (this.template.name.toLowerCase().includes('entity')) {
//         legalFormType = 'CLIENTS_ENTTTY';
//       } else if (this.template.name.toLowerCase().includes('person')) {
//         legalFormType = 'CLIENTS_PERSON';
//       }
//     }
//     this.organizationService.uploadImportDocument(this.template, this.bulkImport.urlSuffix, legalFormType).subscribe(() => {});
//   }

   /**
     * Upload excel file New containing bulk import data.
     */
    upload() {
     this.uploadShow = true;
     this.bulkLoans = [];
     this.bulkDetails = [];
     this.csvDetails = [];
     const productId = this.bulkImportForm.get('productId').value;
     console.log("productId-ts", productId);
      let legalFormType = '';
      /** Only for Client Bulk Imports */
      if (this.bulkImport.name === 'Clients') {
        if (this.template.name.toLowerCase().includes('entity')) {
          legalFormType = 'CLIENTS_ENTTTY';
        } else if (this.template.name.toLowerCase().includes('person')) {
          legalFormType = 'CLIENTS_PERSON';
        }
      }
      this.organizationService.uploadImportDocument(this.template, this.bulkImport.urlSuffix,productId, legalFormType)
      .subscribe((response: any) => {
        if(response.statusCodeValue === 200) {
          this.bulkLoans = [response.body];
          console.log(this.bulkLoans);
          this.bulkLoans.map(element => {
            element.completed = element.completed === true ? 'SUCCESS' : 'FAILED';
            return element;
          });
          this.bulkDetails = response.body.importDocumentDetailsList
          this.bulkDetails.map(element => {
            element.status = element.status === true ? 'SUCCESS' : 'FAILED';
            return element;
          });

          this.bulkDetails.forEach(e => {
              let arr = [];
              arr.push(e.id);
              arr.push(e.importId);
              arr.push(e.externalId);
              arr.push(e.loanId);
              arr.push(e.status);
              arr.push(e.reason);
              arr.push(e.date);
              this.csvDetails.push(arr);
          })
          this.uploadShow = false;
        } else {
          this.notifyService.showError(response.body, 'Error')
          this.uploadShow = false;
        }
      							  // this.router.navigate(['../', response.resourceId], { relativeTo: this.route });
      							  },
                      error=>
      							  {
                        for(let i=0;i<error.error.errors.length;i++) {
      									   this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
      									   }
                           this.uploadShow = false;
      });
    }

      /**
         * Upload  Collection excel file New containing bulk import data.
         */
        uploadCollection() {
          this.bulkCollection = [];
          this.organizationService.uploadCollectionImportDocument(this.template, this.bulkImport.urlSuffix)
          .subscribe((response: any) => {
            if(response.statusCodeValue === 200) {
              this.bulkCollection = response;
            } else {
              this.notifyService.showError(response.body, 'Error')
              this.uploadShow = false;
            }
         	  // this.router.navigate(['../', response.resourceId], { relativeTo: this.route });
          							  },error=>
          							  {
          										for(let i=0;i<error.error.errors.length;i++)
          									  {
          									   this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
          									   }
          });
        }

  /**
   * Reloads imports data table.
   */
  refreshDocuments() {
    this.organizationService.getImports(this.bulkImport.entityType).subscribe( (data: any) => {
      this.dataSource =  new MatTableDataSource(data);
      this.importsTableRef.renderRows();
    });
  }

  /**
   * Download import document.
   * @param {string} name Import Name
   * @param {any} id ImportID
   */
//   downloadDocument(name: string, id: any) {
//     this.organizationService.getImportDocument(id).subscribe( (res: any) => {
//       const contentType = res.headers.get('Content-Type');
//       const blob = new Blob([res.body], { type: contentType });
//       const fileOfBlob = new File([blob], name, { type: contentType });
//       window.open(window.URL.createObjectURL(fileOfBlob));
//     });
//   }

//     downloadDetails(serialNo: string, id: any) {
//       this.organizationService.getImportDocument(id).subscribe( (res: any) => {
//         const contentType = res.headers.get('Content-Type');
//         const blob = new Blob([res.body], { type: contentType });
//         const fileOfBlob = new File([blob], serialNo, { type: contentType });
//         window.open(window.URL.createObjectURL(fileOfBlob));
//       });
//     }

      /**
        * Generates the CSV file dynamically for run report data.
        */
       downloadCSV() {
        if(this.bulkDetails.length === 0) {
          this.notifyService.showWarning('No Data to download', 'Download')
          return;
        }
        const ws: XLSX.WorkSheet=XLSX.utils.table_to_sheet(this.table.nativeElement);//converts a DOM TABLE element to a worksheet
        const wb: XLSX.WorkBook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, 'Response');
        XLSX.writeFile(wb, this.fileName + '-response.xlsx');
       }
}

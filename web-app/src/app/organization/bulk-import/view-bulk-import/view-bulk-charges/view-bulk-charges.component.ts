import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource, MatTable } from '@angular/material/table';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Dates } from 'app/core/utils/dates';
import { SettingsService } from 'app/settings/settings.service';
/** Custom Imports */
import { OrganizationService } from '../../../organization.service';
import { BulkImports } from '../bulk-imports';
import { NotificationService } from '../../../../notification.service';
import * as XLSX from 'xlsx';

@Component({
  selector: 'mifosx-view-bulk-charges',
  templateUrl: './view-bulk-charges.component.html',
  styleUrls: ['./view-bulk-charges.component.scss']
})

export class ViewBulkChargesComponent implements OnInit {

  @ViewChild('TABLE') table: ElementRef;
  //    @ViewChild('myInput');
  @Input() dataObject: any;
  //   /** offices Data */
  //   officeData: any;
  //   /** staff Data */
  //   staffData: any;
  min
  maxDate = new Date();

  partnerData: any;
  productData: any;
  bulkLoans: any;
  enumType: any;
  bulkDetails: any = [];
  csv: any;
  csvDetails: any = [];
  bulkChargeImportForm: FormGroup;
  InputVar: ElementRef;
  columnTypes: any[] = [];
  /** Entity Template */
  template: File;
  /** imports Data */
  importsData: any;
  /** bulk-import form. */
  /** array of deined bulk-imports */
   bulkImportsArray = BulkImports;
  /** bulk-import which user navigated to */
  bulkImport: any = {};
    uploadShow=true;
    fileName:string;

  displayedColumns: string[] =
    [
      'name',
      'importTime',
      'endTime',
      'completed',
      'totalRecords',
      'successCount',
      'failureCount',
    ];

  displayedDetailsColumns: string[] =
    [
      'serialNo',
      'externalId',
      'loanAccountNo',
      'status',
      'reason',
      'date',
    ];


  excelColumnHeaders: string[] =
    [
      'Serial No',
      'External Id',
      'Loan Account No',
      'Status',
      'Reason',
      'Date',
    ];

/**
   * fetches offices and imports data from resolve
   * @param {ActivatedRoute} route ActivatedRoute
   * @param {FormBuilder} formBuilder FormBuilder
   * @param {OrganizationService} organizationService OrganizationService
   	* @param {Dates} dateUtils Dates utils
   */
  constructor(private route: ActivatedRoute,
              private formBuilder: FormBuilder,
              private organizationService: OrganizationService,
                private dateUtils: Dates,
              private settingsService: SettingsService,
               private notifyService: NotificationService
              ) {
    this.bulkImport.name = this.route.snapshot.params['import-chargeCollection'];
    this.route.data.subscribe( (data: any) => {
        this.partnerData = data.officeData;
//         console.log("this.partnerData",this.partnerData);
    });
  }

  /**
   * Gets bulk import's properties.
   */
  ngOnInit() {
    this.bulkImport = this.bulkImportsArray.find( (entry) => entry.name === this.bulkImport.name);
    this.createChargeBulkImportForm();
  }

  /**
     * Creates the bulk import form.
     */
    createChargeBulkImportForm() {
      this.bulkChargeImportForm = this.formBuilder.group({
        'partnerId': [''],
        'productId': ['']
      });
    }

    productFilter(value) {
//           console.log("value", value);
    		  this.productData = value.productInfoDataList;
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

    	/**
           * Gets bulk Charge Collection downloadable template from API.
           */

       downloadChargeTemplate() {

         const partnerId = this.bulkChargeImportForm.get('partnerId').value.partnerId;
          const productId = this.bulkChargeImportForm.get('productId').value;

         this.organizationService.getChargeImportTemplate(partnerId, productId).subscribe( (res: any) => {
           const contentType = res.headers.get('Content-Type');
           const blob = new Blob([res.body], { type: contentType });
           var url = window.URL.createObjectURL(blob);
           var anchor = document.createElement("a");
           anchor.download = 'Charge_Collection_Template_' + new Date().toISOString() +".xlsx";
           anchor.href = url;
           anchor.click();
         });
       }



       /**
            * Upload Charge excel file New containing bulk import data.
            */
           uploadChargeCollection() {
            this.uploadShow = true;
            this.bulkLoans = [];
            this.bulkDetails = [];
            this.csvDetails = [];
             const partnerId = this.bulkChargeImportForm.get('partnerId').value.partnerId;
            const productId = this.bulkChargeImportForm.get('productId').value;

             this.organizationService.uploadChargeDocument(this.template,partnerId,productId )
             .subscribe((response: any) => {
               if(response.statusCodeValue === 200) {
                 this.bulkLoans = [response.body];
                 console.log("Document",this.bulkLoans);
                 this.bulkLoans.map(element => {
                   element.completed = element.completed === true ? 'SUCCESS' : 'FAILED';
                   return element;
                 });
                 this.bulkDetails = response.body.importDocumentDetailsList;
                 console.log("details",this.bulkDetails);
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


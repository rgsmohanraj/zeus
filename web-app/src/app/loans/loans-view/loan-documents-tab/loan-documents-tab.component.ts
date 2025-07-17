/** Angular Imports */
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacyPaginator as MatPaginator } from '@angular/material/legacy-paginator';
import { MatSort } from '@angular/material/sort';
import { MatLegacyTableDataSource as MatTableDataSource, MatLegacyTable as MatTable } from '@angular/material/legacy-table';
import { ActivatedRoute } from '@angular/router';

/** Custom Services */
import { environment } from 'environments/environment';
import { LoansService } from 'app/loans/loans.service';

/** Dialog Components */
import { LoanAccountLoadDocumentsDialogComponent } from 'app/loans/custom-dialog/loan-account-load-documents-dialog/loan-account-load-documents-dialog.component';
import { DeleteDialogComponent } from 'app/shared/delete-dialog/delete-dialog.component';
import { SettingsService } from 'app/settings/settings.service';

/**
 * Overdue charges tab component
 */
@Component({
  selector: 'mifosx-loan-documents-tab',
  templateUrl: './loan-documents-tab.component.html',
  styleUrls: ['./loan-documents-tab.component.scss']
})
export class LoanDocumentsTabComponent implements OnInit {
  @ViewChild('documentsTable', { static: true }) documentsTable: MatTable<Element>;

  /** Stores the resolved loan documents data */
  loanDocuments: any;
  /** Stores the resolved loan details data */
  loanDetailsData: any;
  /** Status of the loan account */
  status: any;
  /** Choice */
  choice: boolean;

  /** Columns to be displayed in loan documents table. */
  displayedColumns: string[] = ['name', 'description', 'filename', 'actions'];
  /** Data source for loan documents table. */
  dataSource: MatTableDataSource<any>;

  /** Paginator for codes table. */
  @ViewChild(MatPaginator) paginator: MatPaginator;
  /** Sorter for codes table. */
  @ViewChild(MatSort) sort: MatSort;

  /**
   * Retrieves the loans data from `resolve`.
   * @param {ActivatedRoute} route Activated Route.
   */
  constructor(private route: ActivatedRoute,
    private loansService: LoansService,
    private settingsService: SettingsService,
    public dialog: MatDialog) {
    this.route.data.subscribe((data: { loanDocuments: any }) => {
      this.getLoanDocumentsData(data.loanDocuments);
    });
    this.route.parent.data.subscribe((data: { loanDetailsData: any }) => {
      this.loanDetailsData = data.loanDetailsData;
    });
  }

  ngOnInit() {
    this.status = this.loanDetailsData.status.value;
    if (this.status === 'Submitted and pending approval' || this.status === 'Active' || this.status === 'Approved') {
      this.choice = true;
    }
    this.dataSource = new MatTableDataSource(this.loanDocuments);
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  getLoanDocumentsData(data: any) {
    data.forEach((ele: any) => {
      let loandocs = {};
      loandocs = this.settingsService.serverUrl + '/loans/' + ele.parentEntityId + '/documents/' + ele.id + '/attachment?tenantIdentifier=' + environment.zeusPlatformTenantId;
      ele.docUrl = loandocs;
      if (ele.fileName) {
        if (ele.fileName.toLowerCase().indexOf('.jpg') !== -1 || ele.fileName.toLowerCase().indexOf('.jpeg') !== -1 || ele.fileName.toLowerCase().indexOf('.png') !== -1) {
          ele.fileIsImage = true;
        }
      }
      if (ele.type) {
        if (ele.type.toLowerCase().indexOf('image') !== -1) {
          ele.fileIsImage = true;
        }
      }
    });
    this.loanDocuments = data;
  }

  uploadDocument() {
    const uploadLoanDocumentDialogRef = this.dialog.open(LoanAccountLoadDocumentsDialogComponent, {
                data: { documentIdentifier: false }
              });
    uploadLoanDocumentDialogRef.afterClosed().subscribe((dialogResponse: any) => {
    console.log(dialogResponse,"dialogResponse");
      if (dialogResponse) {
      const formData: FormData = new FormData;
              formData.append('name', dialogResponse.data.name);
              formData.append('file', dialogResponse.data.file);
              formData.append('description', dialogResponse.data.description);
              this.loansService.loadLoanDocument(this.loanDetailsData.id, formData).subscribe((res: any) => {
              this.loanDocuments.push({
              id: res.resourceId,
              parentEntityType: 'loans',
              parentEntityId: this.loanDetailsData.id,
              name: dialogResponse.data.name,
              description: dialogResponse.data.description,
              fileName: dialogResponse.data.file.name
              });
//               this.documentsTable.renderRows();
//                         console.log('document Uploaded');
               });
//         this.loansService.loadLoanDocument(this.loanDetailsData.id, data)
//           .subscribe(() => {});
      }
    });
  }
  download(loanId: string, documentId: string) {
          this.loansService.downloadLoanDocument(loanId, documentId).subscribe(res => {
            const url = window.URL.createObjectURL(res);
            window.open(url);
          });
        }

  deleteDocument(documentId: any, index: any) {
    const deleteDocumentDialogRef = this.dialog.open(DeleteDialogComponent, {
      data: { deleteContext: `document id:${documentId}` }
    });
    deleteDocumentDialogRef.afterClosed().subscribe((response: any) => {
      if (response.delete) {
        this.loansService.deleteLoanDocument(this.loanDetailsData.id, documentId).subscribe((res: any) => {
          this.loanDocuments.splice(index, 1);
          this.documentsTable.renderRows();
        });
      }
    });
  }

}

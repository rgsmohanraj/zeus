/** Angular Imports */
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatLegacyPaginator as MatPaginator } from '@angular/material/legacy-paginator';
import { MatSort } from '@angular/material/sort';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { ActivatedRoute } from '@angular/router';

/**
 * Overdue charges tab component
 */
@Component({
  selector: 'mifosx-overdue-charges-tab',
  templateUrl: './overdue-charges-tab.component.html',
  styleUrls: ['./overdue-charges-tab.component.scss']
})
export class OverdueChargesTabComponent implements OnInit {

  /** Stores the resolved loan data */
  loanDetails: any;
  /** Stores the overdue data */
  overdueCharges: any;
  overDueShare:any;

  /** Columns to be displayed in overdue charges table. */
  displayedColumns: string[] = ['name', 'type', 'amount','self','partner', 'collectedon'];
  /** Data source for codes table. */
  dataSource: MatTableDataSource<any>;

  /** Paginator for codes table. */
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  /** Sorter for codes table. */
  @ViewChild(MatSort, { static: true }) sort: MatSort;

  /**
   * Retrieves the loans data from `resolve`.
   * @param {ActivatedRoute} route Activated Route.
   */
  constructor(private route: ActivatedRoute) {
    this.route.parent.data.subscribe(( data: { loanDetailsData: any }) => {
      this.loanDetails = data.loanDetailsData;
        this.overDueShare=this.loanDetails.loanProductFeesChargesData[0].partnerOverDue;
//          console.log("overDueShare" ,this.overDueShare);
    });
  }

  ngOnInit() {
    this.overdueCharges = this.loanDetails.overdueCharges;
//     console.log("this.overdueCharges",this.overdueCharges);
    this.dataSource = new MatTableDataSource(this.overdueCharges);
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

}

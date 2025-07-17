/** Angular Imports. */
import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatLegacyCheckbox as MatCheckbox } from '@angular/material/legacy-checkbox';
import { MatLegacyPaginator as MatPaginator } from '@angular/material/legacy-paginator';
import { MatSort } from '@angular/material/sort';
import { ClientsDataSource } from './clients.datasource';

/** rxjs Imports */
import { merge } from 'rxjs';
import { tap } from 'rxjs/operators';

/** Custom Services */
import { ClientsService } from './clients.service';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';

@Component({
  selector: 'mifosx-clients',
  templateUrl: './clients.component.html',
  styleUrls: ['./clients.component.scss'],
})
export class ClientsComponent implements OnInit, AfterViewInit {
  @ViewChild('showClosedAccounts', { static: true }) showClosedAccounts: MatCheckbox;

  displayedColumns = ['name', 'clientno', 'externalid', 'status', 'mobileNo', 'office'];
  dataSource= new MatTableDataSource<any>();
  /** Get the required filter value. */
  searchValue = '';
  length = 0;

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  @ViewChild(MatSort, { static: true }) sort: MatSort;

  constructor(private clientsService: ClientsService) {

  }

  ngOnInit() {
    this.getClients();
  }

  ngAfterViewInit() {
    this.sort.sortChange.subscribe(() => this.paginator.pageIndex = 0);
//     merge(this.sort.sortChange, this.paginator.page, this.showClosedAccounts.change)
//       .pipe(
//         tap(() => this.loadClientsPage())
//       )
//       .subscribe();
  }

  /**
   * Loads a page of journal entries.
   */
  loadClientsPage() {
    if (!this.sort.direction) {
      delete this.sort.active;
    }

    if (this.searchValue !== '') {
      this.applyFilter(this.searchValue);
    } else {
//       this.dataSource.getClients(this.sort.active, this.sort.direction, this.paginator.pageIndex, this.paginator.pageSize, this.showClosedAccounts.checked);
    }
  }

  /**
   * Initializes the data source for clients table and loads the first page.
   */
  getClients(showClosedAccounts: boolean = true)
  {
  this.clientsService.getAllClients()
  .subscribe((clients: any) => {
  console.log(clients);
  this.dataSource.data = clients.pageItems;
  this.dataSource.paginator = this.paginator;
  this.dataSource.sort = this.sort;
  this.length = clients.pageItems.length;
  });
  }

  /**
   * Filter Client Data
   * @param {string} filterValue Value to filter data.
   */
      applyFilter(filterValue: string = '') {
      this.dataSource.filter = filterValue.trim().toLowerCase();
      }

}

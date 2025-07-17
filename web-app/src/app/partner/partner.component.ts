import { Component, OnInit, ViewChild ,AfterViewInit} from '@angular/core';
import { MatLegacyPaginator as MatPaginator } from '@angular/material/legacy-paginator';
import { MatSort } from '@angular/material/sort';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { ActivatedRoute } from '@angular/router';

import { PartnerService } from './partner.service';

@Component({
  selector: 'mifosx-partner',
  templateUrl: './partner.component.html',
  styleUrls: ['./partner.component.scss']
})
export class PartnerComponent implements OnInit, AfterViewInit {

partnerViewData: any;
displayedColumns: string[] = ['partnerName', 'approvedLimit' ,'agreementStartDate' ,'agreementExpiryDate'];
  dataSource: MatTableDataSource<any>;

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
    @ViewChild(MatSort, { static: true }) sort: MatSort;

   constructor(private route: ActivatedRoute,
                private PartnerService: PartnerService) {
                  this.route.data.subscribe((data: { partnerViewData: any }) => {
                     this.partnerViewData = data.partnerViewData;
                   });

    }

    ngOnInit() {
     this.dataSource = new MatTableDataSource(this.partnerViewData);
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    }


     ngAfterViewInit() {
//         this.sort.sortChange.subscribe(() => this.paginator.pageIndex = 0);
//         merge(this.sort.sortChange, this.paginator.page)
//           .pipe(
//             tap(() => this.loadPartnerPage())
//           )
//           .subscribe();
      }

//        loadPartnerPage() {
//           if (!this.sort.direction) {
//             delete this.sort.active;
//           }
//
//           if (this.searchValue !== '') {
//             this.applyFilter(this.searchValue);
//           } else {
//             this.dataSource.getPartner(this.sort.active, this.sort.direction, this.paginator.pageIndex, this.paginator.pageSize);
//           }
//         }

//         getPartner() {
//           //  this.dataSource = new ClientsDataSource(this.clientsService);
//             this.dataSource.getPartner(this.sort.active, this.sort.direction, this.paginator.pageIndex, this.paginator.pageSize);
//           }

           /**
            * Filter Partner Data
            * @param {string} filterValue Value to filter data.
             */

  applyFilter(filterValue: string) {
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }


}

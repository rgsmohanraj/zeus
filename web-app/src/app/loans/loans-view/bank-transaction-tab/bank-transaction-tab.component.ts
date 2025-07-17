import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatLegacyTableDataSource as MatTableDataSource, MatLegacyTable as MatTable } from '@angular/material/legacy-table';
import { Dates } from 'app/core/utils/dates';

@Component({
  selector: 'mifosx-bank-transaction-tab',
  templateUrl: './bank-transaction-tab.component.html',
  styleUrls: ['./bank-transaction-tab.component.scss']
})
export class BankTransactionTabComponent implements OnInit {
  loanDetails : any;
  bankDetails : any;
  loanDetailsData:any;
  pennyDropData:any;
  disbursement:any;

  displayedColumns: string[] = ['pennyAction','pennyReason'];

  disbursementColumns : string[] = ['transactionType','utr','transactionAmount','transactionDate','disbursementAction','disbursementReason'];

  constructor(private route: ActivatedRoute, private dateUtils: Dates) {
  this.route.parent.data.subscribe((data: { loanDetailsData: any, }) => {

      this.loanDetails = data.loanDetailsData;

     this.pennyDropData = data.loanDetailsData.bankTranscationData.pennyDropTransaction;

     this.disbursement = data.loanDetailsData.bankTranscationData.disbursementTransaction;

  });

   }

  ngOnInit(): void {

  }

}

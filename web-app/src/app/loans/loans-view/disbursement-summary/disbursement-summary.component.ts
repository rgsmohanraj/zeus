import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatLegacyTableDataSource as MatTableDataSource, MatLegacyTable as MatTable } from '@angular/material/legacy-table';


@Component({
  selector: 'mifosx-disbursement-summary',
  templateUrl: './disbursement-summary.component.html',
  styleUrls: ['./disbursement-summary.component.scss']
})
export class DisbursementSummaryComponent implements OnInit {
loanDetails: any;
disbursementData:any;
disbursementDatas:any;
selfDisbursementDatas:any;
partnerDisbursementDatas:any;
loansAccountTemplate:any;
overAllTotal:any;
displayedColumns: string[] = ['feeList','feeAmount','igst','cgst','sgst','total'];
status: any;
  loanSummaryColumns: string[] = ['Empty', 'Original', 'Paid', 'Reversal', 'Waived', 'Written Off', 'Outstanding', 'Over Due'];
  loanDetailsColumns: string[] = ['Key', 'Value'];
  loanSummaryTableData: {
    'property': string,
    'original': string,
    'paid': string,
    'reversal' : string,
    'waived': string,
    'writtenOff': string,
    'outstanding': string,
    'overdue': string
  }[];
  loanDetailsTableData: {
    'key': string,
    'value'?: string
  }[];

  /** Data source for loans summary table. */
  dataSource: MatTableDataSource<any>;
  detailsDataSource: MatTableDataSource<any>;
// dataSource: MatTableDataSource<any>;

/**
   * Retrieves the loans with associations data from `resolve`.
   * @param {ActivatedRoute} route Activated Route.
   */

 constructor(private route: ActivatedRoute) {
  this.route.parent.data.subscribe((data: { loanDetailsData: any, }) => {
    this.loanDetails=data.loanDetailsData;
  this.disbursementData=data.loanDetailsData.disbursementSummaries;
  this.disbursementDatas=data.loanDetailsData.netDisbursalAmount;
  this.selfDisbursementDatas=data.loanDetailsData.netSelfDisbursalAmount;
  this.partnerDisbursementDatas=data.loanDetailsData.netPartnerDisbursalAmount;
  console.log(this.loanDetails,"this.loanDetails");
});

}



  ngOnInit() {
 this.status = this.loanDetails.value;
    if (this.loanDetails.summary) {
      this.setloanSummaryTableData();
      this.setloanDetailsTableData();
    } else {
      this.setloanNonDetailsTableData();
    }
  }

  setloanSummaryTableData() {
    this.loanSummaryTableData = [
      {
        'property': 'Broken Period Interest',
        'original': this.loanDetails.brokenInterestDerived,
        'paid': this.loanDetails.brokenInterestPaid,
        'reversal': 0,
        'waived': 0,
        'writtenOff':0,
        'outstanding': 0,
        'overdue': 0,
},
      {
        'property': 'Principal',
          'original': this.loanDetails.summary.principalDisbursed,
        'paid': this.loanDetails.summary.principalPaid,
        'reversal': this.loanDetails.coolingOffReversedChargeAmount,
        'waived': this.loanDetails.summary.principalWaived,
        'writtenOff': this.loanDetails.summary.principalWrittenOff,
        'outstanding': this.loanDetails.summary.principalOutstanding,
        'overdue': this.loanDetails.summary.principalOverdue,

    },
    {
        'property': 'Interest',
        'original': this.loanDetails.summary.interestCharged,
        'paid': this.loanDetails.summary.interestPaid,
        'reversal': this.loanDetails.coolingOffReversedChargeAmount,
        'waived': this.loanDetails.summary.interestWaived,
        'writtenOff': this.loanDetails.summary.interestWrittenOff,
        'outstanding': this.loanDetails.summary.interestOutstanding,
        'overdue': this.loanDetails.summary.interestOverdue,
    },
    {
        'property': 'Fees',
        'original': this.loanDetails.summary.feeChargesCharged,
        'paid': this.loanDetails.summary.feeChargesPaid,
        'reversal': this.loanDetails.summary.coolingOffReversedChargeAmount,
        'waived': this.loanDetails.summary.feeChargesWaived,
        'writtenOff': this.loanDetails.summary.feeChargesWrittenOff,
        'outstanding': this.loanDetails.summary.feeChargesOutstanding,
        'overdue': this.loanDetails.summary.feeChargesOverdue,
    },
    {
        'property': 'Penalties',
        'original': this.loanDetails.summary.penaltyChargesCharged,
        'paid': this.loanDetails.summary.penaltyChargesPaid,
        'reversal': this.loanDetails.coolingOffReversedChargeAmount,
        'waived': this.loanDetails.summary.penaltyChargesWaived,
        'writtenOff': this.loanDetails.summary.penaltyChargesWrittenOff,
        'outstanding': this.loanDetails.summary.penaltyChargesOutstanding,
        'overdue': this.loanDetails.summary.penaltyChargesOverdue,
    },
    {
      'property': 'Bounce',
      'original': this.loanDetails.summary.bounceChargesCharged,
      'paid': this.loanDetails.summary.bounceChargesPaid,
      'reversal': this.loanDetails.coolingOffReversedChargeAmount,
      'waived': this.loanDetails.summary.bounceChargesWaived,
      'writtenOff': this.loanDetails.summary.bounceChargesWrittenOff,
      'outstanding': this.loanDetails.summary.bounceChargesOutstanding,
      'overdue': "0",
  },

    {
        'property': 'Total',
        'original': this.loanDetails.summary.totalExpectedRepayment,
        'paid': this.loanDetails.summary.totalRepayment,
        'reversal': this.loanDetails.summary.coolingOffReversedChargeAmount,
        'waived': this.loanDetails.summary.totalWaived,
        'writtenOff': this.loanDetails.summary.totalWrittenOff,
        'outstanding': this.loanDetails.summary.totalOutstanding,
        'overdue': this.loanDetails.summary.totalOverdue,
    }
    ];
    this.dataSource = new MatTableDataSource(this.loanSummaryTableData);
    console.log(this.dataSource,"this.dataSource");
  }

  setloanDetailsTableData() {

    this.loanDetailsTableData = [
      {
        'key': 'Disbursement Date'
      },
      {
        'key': 'Loan Purpose'
      },
      {
        'key': 'Loan Officer'
      },
      {
        'key': 'Currency'
      },
      {
        'key': 'External Id'
      },
      {
        'key': 'Proposed Amount',
        'value': this.loanDetails.proposedPrincipal,
      },
      {
        'key': 'Approved Amount',
        'value': this.loanDetails.approvedPrincipal,
      },
      {
        'key': 'Disburse Amount',
        'value': this.loanDetails.principal,
      },
    ];
    this.detailsDataSource = new MatTableDataSource(this.loanDetailsTableData);

  }

  setloanNonDetailsTableData() {
    this.loanDetailsTableData = [
      {
        'key': 'Disbursement Date'
      },
      {
        'key': 'Currency'
      },
      {
        'key': 'Loan Officer'
      },
      {
        'key': 'External Id'
      }
    ];
    this.detailsDataSource = new MatTableDataSource(this.loanDetailsTableData);
  }

  showApprovedAmountBasedOnStatus() {
    if (this.status === 'Submitted and pending approval' || this.status === 'Withdrawn by applicant' || this.status === 'Rejected') {
        return false;
    }
    return true;
  }

  showDisbursedAmountBasedOnStatus = function() {
    if (this.status === 'Submitted and pending approval' || this.status === 'Withdrawn by applicant' || this.status === 'Rejected' ||
        this.status === 'Approved') {
        return false;
    }
    return true;
  };
  }



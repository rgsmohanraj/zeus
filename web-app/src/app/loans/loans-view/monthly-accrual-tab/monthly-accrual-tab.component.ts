import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'mifosx-repayment-schedule-tab',
  templateUrl: './monthly-accrual-tab.component.html',
  styleUrls: ['./monthly-accrual-tab.component.scss']
})
export class MonthlyAccrualTabComponent implements OnInit {

  /** Loan Month Accrual Data */
  loanAccruals: any;
  /** Columns to be displayed in Month Accrual table. */
  displayedColumns: string[] = ['installment','accrualType', 'fromDate', 'toDate' ,'accruedAmount',
    'selfAccruedAmount', 'partnerAccruedAmount'];

  /**
   * Retrieves the loans with associations data from `resolve`.
   * @param {ActivatedRoute} route Activated Route.
   */
  constructor(private route: ActivatedRoute) {
    this.route.parent.data.subscribe((data: { loanDetailsData: any }) => {
      this.loanAccruals = data.loanDetailsData.loanAccruals;
      console.log(this.loanAccruals);
    });
  }

  ngOnInit() {

  }

}

import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot } from '@angular/router';

/** rxjs Imports */
import { Observable } from 'rxjs';

/** Custom Services */
import { LoansService } from '../loans.service';

@Injectable()
export class BankTransactionResolver implements Resolve<Object> {

       /**
           * @param {LoansService} LoansService Loans service.
           */
          constructor(private loansService: LoansService) { }

          /**

         /**

          * @returns {Observable<any>}
          */
  resolve(route: ActivatedRouteSnapshot): Observable<any> {
         const loanId = route.parent.parent.paramMap.get('loanId');
         return this.loansService.getBankTransaction(loanId);
}
}

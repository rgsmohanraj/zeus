
import { Injectable } from '@angular/core';
import {
  Router, Resolve,
  RouterStateSnapshot,
  ActivatedRouteSnapshot
} from '@angular/router';
import { Observable, of } from 'rxjs';

/** Custom Services */
import { ViewAllLoansService } from './view-all-loans.service';

/**
 * Audit Trail data resolver.
 */
@Injectable({
  providedIn: 'root'
})
export class ViewAllLoansResolver implements Resolve<Object> {

  /**
   * @param {ViewAllLoansService} viewAllLoansService System service.
   */
  constructor(private viewAllLoansService: ViewAllLoansService) {}

  /**
   * Returns the Audit Trail data.
   * @returns {Observable<any>}
   */
  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    const partnerId = route.paramMap.get('partnerId');
     const productId = route.paramMap.get('productId');
      const fromDate = route.paramMap.get('fromDate');
       const toDate = route.paramMap.get('toDate');

    return this.viewAllLoansService.getViewAllLoans(partnerId,productId,fromDate,toDate);
  }

}


























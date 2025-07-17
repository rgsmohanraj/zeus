import { Injectable } from '@angular/core';
import {
  Router, Resolve,
  RouterStateSnapshot,
  ActivatedRouteSnapshot
} from '@angular/router';
import { Observable, of } from 'rxjs';

/** Custom Services */
import { ViewAllLoansService } from './view-all-loans.service';

@Injectable({
  providedIn: 'root'
})
export class ViewAllLoansSearchTemplateResolver implements Resolve<Object> {
 /**
    * @param {ViewAllLoansService} viewAllLoansService System service.
    */
   constructor(private viewAllLoansService: ViewAllLoansService) {}

   /**
    * Returns the Audit Trail data.
    * @returns {Observable<any>}
    */
   resolve(): Observable<any> {
      return this.viewAllLoansService.getViewAllLoansSearchTemplate();
    }

}

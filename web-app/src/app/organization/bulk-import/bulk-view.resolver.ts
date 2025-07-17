import { Injectable } from '@angular/core';
import {
  Router, Resolve,
  RouterStateSnapshot,
  ActivatedRouteSnapshot
} from '@angular/router';


/** Custom Services */
import { OrganizationService } from '../organization.service';

import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BulkViewResolver implements Resolve<Object> {
  /**
    * @param {OrganizationService} organizationService Organization service.
    */
   constructor(private organizationService: OrganizationService) {
   }

  /**
    * Returns the user data.
    * @returns {Observable<any>}
    */
   resolve(): Observable<any> {
     return this.organizationService.getBulkLoans();
   }

}

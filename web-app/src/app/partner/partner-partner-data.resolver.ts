import { Injectable } from '@angular/core';
import {
  Router, Resolve,
  RouterStateSnapshot,
  ActivatedRouteSnapshot
} from '@angular/router';
import { Observable, of } from 'rxjs';

import { PartnerService } from './partner.service';

@Injectable({
  providedIn: 'root'
})
export class PartnerPartnerDataResolver implements Resolve<Object> {
   /**
         * @param {PartnerService} PartnerService Partner service.
         */
        constructor(private partnerService: PartnerService) { }

  /**
      * Returns the Partners data.
      * @returns {Observable<any>}
      */
     resolve(route: ActivatedRouteSnapshot): Observable<any> {
          const partnerId = route.paramMap.get('id');
         return this.partnerService.getPartner(partnerId);

  }
}

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
export class PartnerResolver implements Resolve<Object> {

 /**
     * @param {PartnerService} PartnerService Partner service.
     */
    constructor(private partnerService: PartnerService) { }

     /**
          * Returns the Partners data.
          * @returns {Observable<any>}
          */

   resolve(): Observable<any> {
          return this.partnerService.getPartnerTemplate();
      }


}

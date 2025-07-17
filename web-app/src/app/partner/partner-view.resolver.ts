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
export class PartnerViewResolver implements Resolve<Object> {
    /**
        * @param {PartnerService} PartnerService Partner service.
        */
       constructor(private partnerService: PartnerService) { }

 /**
     * Returns the Partners data.
     * @returns {Observable<any>}
     */
    resolve(): Observable<any> {
//         const partnerId = route.paramMap.get('id');
        return this.partnerService.getAllPartner();
    }
}

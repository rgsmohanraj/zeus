import { Injectable } from '@angular/core';
import {
  Router, Resolve,
  RouterStateSnapshot,
  ActivatedRouteSnapshot
} from '@angular/router';
import { Observable } from 'rxjs';
/** Custom Services */
import { OrganizationService } from '../../organization.service';


@Injectable()
export class OfficeViewResolver implements Resolve<Object> {
   /**
     * @param {OrganizationService} organizationService Organization service.
     */
    constructor(private organizationService: OrganizationService) {}

    /**
     * Returns the offices data.
     * @returns {Observable<any>}
     */
    resolve(): Observable<any> {
      return this.organizationService.getOffices();
    }
}

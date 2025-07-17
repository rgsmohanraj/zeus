import { Injectable } from '@angular/core';
import {
  Router, Resolve,
  RouterStateSnapshot,
  ActivatedRouteSnapshot
} from '@angular/router';
import { Observable, of } from 'rxjs';

import { OrganizationService } from '../../organization.service';

@Injectable({
  providedIn: 'root'
})
export class CreateResolver implements Resolve<Object> {

/**
    * @param {OrganizationService} organizationService Organization service.
    */
   constructor(private organizationService: OrganizationService) {}

/**
   * Returns the office data.
   * @returns {Observable<any>}
   */
  resolve(route: ActivatedRouteSnapshot): Observable<any> {
     const office = route.paramMap.get('office');
    return this.organizationService.createOffice(office);
  }

}

import { Injectable } from '@angular/core';
import {Router, Resolve,RouterStateSnapshot,ActivatedRouteSnapshot} from '@angular/router';
import { Observable, of } from 'rxjs';
import { ClientsService } from '../clients.service';

@Injectable({
  providedIn: 'root'
})
export class ClientDetailsResolver implements Resolve<boolean> {

    /**
     * @param {ClientsService} ClientsService Clients service.
     */
    constructor(private clientsService: ClientsService) { }

    /**
     * Returns the Client Address data.
     * @returns {Observable<any>}
     */
    resolve(route: ActivatedRouteSnapshot): Observable<any> {
        const clientId = route.parent.paramMap.get('clientId');
        return this.clientsService.getClientDataAndTemplate(clientId);
  }
}

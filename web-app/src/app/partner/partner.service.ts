import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PartnerService {

/**
   * @param {HttpPartner} http Http Partner to send requests.
   */
  constructor(private http: HttpClient) { }

  getPartnerTemplate(): Observable<any> {
      return this.http.get('/partners/template');//partner resolver
    }

 getPartnerData(partnerId: string) {
    return this.http.get(`/partners/${partnerId}?template=true`);//
  }

   getAllPartner() {
      return this.http.get(`/partners`);//view
    }

    getPartner(partnerId: string) {
          return this.http.get(`/partners/${partnerId}?template=false`);//partner-date
        }


  createPartner(partner: any) {
      return this.http.post(`/partners`, partner);//create
    }

//  updateDataPartner(partnerId: string, partner: any) {
//     return this.http.put(`/partners/${partnerId}`, partner);//update
//   }

   updateDataPartner(partnerId: string, partner: any): Observable<any> {
      return this.http.put(`/partners/${partnerId}`, partner);
    }

}



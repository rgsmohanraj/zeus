import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

/** rxjs Imports */
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class ViewAllLoansService {

/**
   * @param {HttpClient} http Http Client to send requests.
   */

  constructor(private http: HttpClient) { }

 /**
   * @param {string} viewAllLoansId Audit Trail ID.
   * @returns {Observable<any>}
   */

 getViewAllLoans(partnerId: any, productId: any, fromDate: any, toDate: any): Observable<any> {
    let httpParams = new HttpParams().set('partnerId', partnerId)
                                       .set('productId', productId)
                                       .set('fromDate', fromDate)
                                       .set('toDate', toDate);
    return this.http.get(`/loans/loansFilter`, { params: httpParams });
  }
     /**
        * @returns {Observable<any>} View Loans  Search Template.
        */
       getViewAllLoansSearchTemplate(): Observable<any> {
         return this.http.get('/loans/partnerandproducttemplate');
       }



  }

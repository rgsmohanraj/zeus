// import { Injectable } from '@angular/core';
// import {
//   Router, Resolve,
//   RouterStateSnapshot,
//   ActivatedRouteSnapshot
// } from '@angular/router';
// import { Observable, of } from 'rxjs';
//
// /** Custom Services */
// import { ProductsService } from '../products.service';
//
//
// @Injectable()
// export class LoanProductServicerFeeConfigResolver implements Resolve<Object> {
//
//  /**
//     * @param {ProductsService} productsService Products service.
//     */
//    constructor(private productsService: ProductsService) { }
//
//    /**
//     * Returns the loan product data.
//     * @returns {Observable<any>}
//     */
//
//   resolve(route: ActivatedRouteSnapshot): Observable<any> {
//      const id = route.paramMap.get('id');
//      return this.productsService.getServicerFeeConfigId(id);
//
//    }
//
// }

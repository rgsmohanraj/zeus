/** Angular Imports */
import { Injectable } from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse} from '@angular/common/http';

/** rxjs Imports */
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

/** Custom Services */
import { ProgressBarService } from './progress-bar.service';
import { NgxSpinnerService } from 'ngx-spinner';
/**
 * Http Request interceptor to start/stop loading the progress bar.
 */
@Injectable()
export class ProgressInterceptor implements HttpInterceptor {

  /**
   * @param {ProgressBarService} progressBarService Progress Bar Service.
   */
  constructor(private spinner: NgxSpinnerService) { }

  /**
   * Intercepts a Http request to start loading the progress bar for a pending request
   * and stop when a response or error is received.
   */
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let isNotificationRequest = request.url.includes('/notifications');
    if(!isNotificationRequest) this.spinner.show();
    return next.handle(request)
      .pipe(
        tap(event => {
          if (event instanceof HttpResponse) {
            if(!isNotificationRequest) this.spinner.hide();
          }
        })
      )
      .pipe(
        catchError(error => {
          if(!isNotificationRequest) this.spinner.hide();
          throw error;
        })
      );
  }

}

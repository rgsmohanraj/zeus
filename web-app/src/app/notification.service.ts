import { Injectable } from '@angular/core';

import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(private toastr: ToastrService) { }

  showSuccess(message, title){
      this.toastr.success(message, title)
  }

  showError(message, title){
      this.toastr.error(message, title,{timeOut:10000})
  }

  showInfo(message, title){
      this.toastr.info(message, title,{timeOut:10000})
  }

  showWarning(message, title){
      this.toastr.warning(message, title,{timeOut:10000})
  }

  showHTMLMessage(message, title){
  	this.toastr.success(message, title, {
  		enableHtml :  true
  	})
  }

}

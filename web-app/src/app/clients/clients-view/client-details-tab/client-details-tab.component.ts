import { Component, Output, EventEmitter, Input,OnInit } from '@angular/core';
import { FormGroup, UntypedFormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ClientsService } from '../../clients.service';

@Component({
  selector: 'mifosx-client-details-tab',
  templateUrl: './client-details-tab.component.html',
  styleUrls: ['./client-details-tab.component.scss']
})
export class ClientDetailsTabComponent implements OnInit {

clientId: string;
clientDetails: any

@Input() clientTemplate: any;

 @Input() client: any;

  constructor(private route: ActivatedRoute,
                            private formBuilder: UntypedFormBuilder,
                            private clientsService: ClientsService) {

          this.clientId = this.route.parent.snapshot.params['clientId'];
             this.route.data.subscribe((data: { clientDetails: any }) => {
                this.clientDetails = data.clientDetails;
               console.log(data,"clientData");
               console.log(this.clientDetails.clientType,"clientType");
             });
           }




  ngOnInit(): void {
  }

}

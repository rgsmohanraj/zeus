import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'mifosx-partner-view',
  templateUrl: './partner-view.component.html',
  styleUrls: ['./partner-view.component.scss']
})
export class PartnerViewComponent implements OnInit {

partnerTemplate: any;
 partnerViewData: any;
 partner: any;

  constructor(private route: ActivatedRoute) {
 }

 ngOnInit() {

   this.route.data.subscribe((data) => {
         this.partnerViewData = data.partnerTemplate;
        });
         this.partnerViewData.allowAttributeConfiguration = Object.values(this.partnerViewData.allowAttributeOverrides).some((attribute: boolean) => attribute);
   }

}

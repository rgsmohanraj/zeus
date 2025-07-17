import { Component, Output, EventEmitter, Input } from '@angular/core';

@Component({
  selector: 'mifosx-partner-preview',
  templateUrl: './partner-preview.component.html',
  styleUrls: ['./partner-preview.component.scss']
})
export class PartnerPreviewComponent {

/** Partner Template */
   @Input() partnerTemplate: any;
//    @Input() client: any;
   @Input() partner: any;

   @Output() submit = new EventEmitter();

  constructor() { }

  ngOnInit(): void {
  }

}

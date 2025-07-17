import { Component, Output, EventEmitter, Input } from '@angular/core';

@Component({
  selector: 'mifosx-office-preview',
  templateUrl: './office-preview.component.html',
  styleUrls: ['./office-preview.component.scss']
})
export class OfficePreviewComponent {

/** Office Template */
   @Input() officeTemplate: any;

   @Input() office: any;

   @Output() submit = new EventEmitter();
  constructor() {}

  ngOnInit(): void {

  console.log(this.office,"office")
  }

}

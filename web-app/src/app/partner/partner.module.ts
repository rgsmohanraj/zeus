import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { PartnerRoutingModule } from './partner-routing.module';
import { SharedModule } from 'app/shared/shared.module';
import { PipesModule } from '../pipes/pipes.module';
import { DirectivesModule } from '../directives/directives.module';
import { PartnerComponent } from './partner.component';
import { CreatePartnerComponent } from './create-partner/create-partner.component';
import { EditPartnerComponent } from './edit-partner/edit-partner.component';
import { PartnerViewComponent } from './partner-view/partner-view.component';
import { PartnerBasicDetailsComponent } from './partner-stepper/partner-basic-details/partner-basic-details.component';
import { PartnerPartnerDetailsComponent } from './partner-stepper/partner-partner-details/partner-partner-details.component';
import { PartnerBeneficiaryDetailsComponent } from './partner-stepper/partner-beneficiary-details/partner-beneficiary-details.component';
import { PartnerPreviewComponent } from './partner-stepper/partner-preview/partner-preview.component';
import { PartnerLegalDocumentsComponent } from './partner-stepper/partner-legal-documents/partner-legal-documents.component';


@NgModule({
  declarations: [
    PartnerComponent,
    CreatePartnerComponent,
    EditPartnerComponent,
    PartnerViewComponent,
    PartnerBasicDetailsComponent,
    PartnerPartnerDetailsComponent,
    PartnerBeneficiaryDetailsComponent,
    PartnerPreviewComponent,
    PartnerLegalDocumentsComponent
  ],
  imports: [
    CommonModule,
    PartnerRoutingModule,
    SharedModule,
    PipesModule,
    DirectivesModule
  ],
  providers: [ ]
})
export class PartnerModule { }

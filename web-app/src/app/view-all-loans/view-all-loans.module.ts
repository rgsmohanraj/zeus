
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { SharedModule } from 'app/shared/shared.module';
import { PipesModule } from '../pipes/pipes.module';
import { DirectivesModule } from '../directives/directives.module';

import { ViewAllLoansRoutingModule } from './view-all-loans-routing.module';
import { ViewAllLoansComponent } from './view-all-loans.component';


@NgModule({
  declarations: [
    ViewAllLoansComponent
  ],
  imports: [
    CommonModule,
    ViewAllLoansRoutingModule,
    SharedModule,
        PipesModule,
        DirectivesModule
  ], providers: [ ]
    })
export class ViewAllLoansModule { }

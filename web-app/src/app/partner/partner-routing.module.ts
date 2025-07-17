import { NgModule, Component } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CreatePartnerComponent } from './create-partner/create-partner.component';
import { EditPartnerComponent } from './edit-partner/edit-partner.component';
import { PartnerViewComponent } from './partner-view/partner-view.component';
/** Routing Imports */
import { Route } from '../core/route/route.service';

/** Translation Imports */
import { extract } from '../core/i18n/i18n.service';
import { PartnerComponent } from './partner.component';

import { PartnerResolver } from './partner.resolver';

import { PartnerViewResolver } from './partner-view.resolver';

import { PartnerUpdateResolver } from './partner-update.resolver';

import { PartnerPartnerDataResolver } from './partner-partner-data.resolver';

const routes: Routes = [
Route.withShell([
{
    path: 'partner',
    data: { title: extract('Partner'), breadcrumb: 'Partner',routePermission:'READ_PARTNER'},
    children: [
      {
        path: '',
        component: PartnerComponent,
        resolve: {
          partnerViewData: PartnerViewResolver
        },
      },
      {
        path: 'create',
        component: CreatePartnerComponent,
        data: { title: extract('Create Partner'), breadcrumb: 'Create',routePermission:'CREATE_PARTNER' },
        resolve: {
           partnerTemplate: PartnerResolver
        }
      },
      {
        path: ':id',
        data: { title: extract('View Partner'), routeParamBreadcrumb: 'id' },
        resolve: {
          partnerTemplate: PartnerPartnerDataResolver
        },
        children: [
          {
            path: '',
            component: PartnerViewComponent,
            resolve: {
              partnerTemplate: PartnerPartnerDataResolver
            }
          },
          {
            path: 'edit',
            component: EditPartnerComponent,
            data: { title: extract('Edit Partner'), breadcrumb: 'Edit', routeParamBreadcrumb: false,routePermission:'UPDATE_PARTNER' },
            resolve: {
              partnerTemplate: PartnerUpdateResolver
            }
          },
        ]
      },
    ]
   }])
 ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
   providers: [
   PartnerResolver,
   PartnerPartnerDataResolver,
   PartnerUpdateResolver
   ]
})
export class PartnerRoutingModule { }

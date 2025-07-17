import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Route } from '../core/route/route.service';
/** Translation Imports */
import { extract } from '../core/i18n/i18n.service';

import { ViewAllLoansSearchTemplateResolver } from './view-all-loans-search-template.resolver';

import { ViewAllLoansResolver } from './view-all-loans.resolver';

import { ViewAllLoansComponent } from './view-all-loans.component';

const routes: Routes = [
Route.withShell([
{
          path: 'loans',
          data: { title: extract('View All Loans'), breadcrumb: 'View All Loans',routePermission:'READ_LOAN' },
          children: [
            {
              path: '',
              component: ViewAllLoansComponent,
              resolve: {
                viewAllLoansTemplate: ViewAllLoansSearchTemplateResolver
              }
            },

            {
                        path: 'loans-accounts',
                        loadChildren: () => import('../loans/loans.module').then(m => m.LoansModule)
                      },
//             {
//               path: ':id',
//               component: ViewAllLoansComponent,
//               data: { title: extract('View Audit'), routeParamBreadcrumb: 'id' },
//               resolve: {
//                 viewAllLoansTemplate: ViewAllLoansResolver
//               }
//             },
          ]
  }])
 ];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
    providers: [
    ViewAllLoansSearchTemplateResolver,
    ViewAllLoansResolver
    ]
})
export class ViewAllLoansRoutingModule { }

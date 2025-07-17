/** Angular Imports */
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

// Not Found Component
import {NotFoundComponent} from './not-found/not-found.component';

/**
 * Fallback to this route when no prior route is matched.
 */
const routes: Routes = [
// { path: 'view-all-loans', loadChildren: () => import('./view-all-loans/view-all-loans.module').then(m => m.ViewAllLoansModule) },
//   { path: 'partner', loadChildren: () => import('./partner/partner.module').then(m => m.PartnerModule) },
  {
    path: '**',
    component: NotFoundComponent
  }
];

/**
 * App Routing Module.
 *
 * Configures the fallback route.
 */
@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule],
  providers: []
})
export class AppRoutingModule { }

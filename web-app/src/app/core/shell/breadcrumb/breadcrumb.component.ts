/** Angular Imports */
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, NavigationEnd, Data } from '@angular/router';

/** rxjs Imports */
import { filter } from 'rxjs/operators';

/** Custom Model */
import { Breadcrumb } from './breadcrumb.model';

/**
 * Route data property to generate breadcrumb using a static string.
 *
 * Example- breadcrumb: 'Home'
 */
const routeDataBreadcrumb = 'breadcrumb';
/**
 * Route data property to generate breadcrumb using given route parameter name.
 *
 * Example- routeParamBreadcrumb: 'id'
 */
const routeParamBreadcrumb = 'routeParamBreadcrumb';
/**
 * Route data property to generate breadcrumb using resolved data property name.
 *
 * Use array to specify name for a nested object property.
 *
 * Example- routeResolveBreadcrumb: ['user', 'username']
 */
const routeResolveBreadcrumb = 'routeResolveBreadcrumb';
/**
 * Route data property to specify whether generated breadcrumb should have a link.
 *
 * True by default. Specify false if a link is not required.
 *
 * Example- addBreadcrumbLink: false
 */
const routeAddBreadcrumbLink = 'addBreadcrumbLink';

/**
 * Generate breadcrumbs dynamically via route configuration.
 */
@Component({
  selector: 'mifosx-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss']
})
export class BreadcrumbComponent implements OnInit {

  /** Array of breadcrumbs. */
  breadcrumbs: Breadcrumb[];

  /**
   * Generates the breadcrumbs.
   * @param {ActivatedRoute} activatedRoute Activated Route.
   * @param {Router} router Router for navigation.
   */
  constructor(private activatedRoute: ActivatedRoute,
              private router: Router) {
    this.generateBreadcrumbs();
  }

  ngOnInit() {
  }

  /**
   * Generates the array of breadcrumbs for the visited route.
   */
  generateBreadcrumbs() {
    const onNavigationEnd = this.router.events.pipe(filter(event => event instanceof NavigationEnd));

    onNavigationEnd.subscribe(() => {
      this.breadcrumbs = [];
      let currentRoute = this.activatedRoute.root;
      let currentUrl = '';

      while (currentRoute.children.length > 0) {
        const childrenRoutes = currentRoute.children;
        let breadcrumbLabel: any;
        let url: any;

        childrenRoutes.forEach(route => {
          currentRoute = route;
          breadcrumbLabel = false;

          if (route.outlet !== 'primary') {
            return;
          }

          const routeURL = route.snapshot.url.map(segment => segment.path).join('/');
          currentUrl += `/${routeURL}`;

          if (currentUrl === '/') {
            breadcrumbLabel = 'Home';
          }

          const hasData = (route.routeConfig && route.routeConfig.data);

          if (hasData) {
            if (route.snapshot.data.hasOwnProperty(routeResolveBreadcrumb) && route.snapshot.data[routeResolveBreadcrumb]) {
              breadcrumbLabel = route.snapshot.data;
              route.snapshot.data[routeResolveBreadcrumb].forEach((property: any) => {
                breadcrumbLabel = breadcrumbLabel[property];
              });
            } else if (route.snapshot.data.hasOwnProperty(routeParamBreadcrumb) && route.snapshot.paramMap.get(route.snapshot.data[routeParamBreadcrumb])) {
              breadcrumbLabel = route.snapshot.paramMap.get(route.snapshot.data[routeParamBreadcrumb]);
              const routeData: Data = route.snapshot.data;
              if (routeData.breadcrumb === 'Partner') {
                              breadcrumbLabel = routeData.partnerViewData.displayName;
                            }
              else if (routeData.breadcrumb === 'Clients') {
                breadcrumbLabel = routeData.clientViewData.displayName;
              } else if (routeData.breadcrumb === 'Groups') {
                breadcrumbLabel = routeData.groupViewData.name;
              } else if (routeData.breadcrumb === 'Centers') {
                breadcrumbLabel = routeData.centerViewData.name;
              } else if (routeData.breadcrumb === 'Loans') {
                breadcrumbLabel = routeData.loanDetailsData.loanProductName + '(' + routeData.loanDetailsData.accountNo + ')';
              } else if (routeData.breadcrumb === 'Savings') {
                breadcrumbLabel = routeData.savingsAccountData.savingsProductName + '(' + routeData.savingsAccountData.accountNo + ')';
              } else if (routeData.breadcrumb === 'Fixed Deposits') {
                breadcrumbLabel = routeData.fixedDepositsAccountData.depositProductName + '(' + routeData.fixedDepositsAccountData.accountNo + ')';
              } else if (routeData.breadcrumb === 'Loan Products') {
                breadcrumbLabel = routeData.loanProduct.name;
              } else if (routeData.breadcrumb === 'Charges') {
                breadcrumbLabel = routeData.charge.name;
              } else if (routeData.breadcrumb === 'Saving Products') {
                breadcrumbLabel = routeData.savingProduct.name;
              } else if (routeData.breadcrumb === 'Share Products') {
                breadcrumbLabel = routeData.shareProduct.name;
              } else if (routeData.breadcrumb === 'Fixed Deposit Products') {
                breadcrumbLabel = routeData.fixedDepositProduct.name;
              } else if (routeData.breadcrumb === 'Recurring Deposit Products') {
                breadcrumbLabel = routeData.recurringDepositProduct.name;
              } else if (routeData.breadcrumb === 'Floating Rates') {
                breadcrumbLabel = routeData.floatingRate.name;
              } else if (routeData.breadcrumb === 'Tax Components') {
                breadcrumbLabel = routeData.taxComponent.name;
              } else if (routeData.breadcrumb === 'Tax Groups') {
                breadcrumbLabel = routeData.taxGroup.name;
              }
            } else if (route.snapshot.data.hasOwnProperty(routeDataBreadcrumb)) {
              breadcrumbLabel = route.snapshot.data[routeDataBreadcrumb];
            }

            if (route.snapshot.data.hasOwnProperty(routeAddBreadcrumbLink)) {
              url = route.snapshot.data[routeAddBreadcrumbLink];
            } else {
              url = currentUrl;
            }
          }

          const breadcrumb: Breadcrumb = {
            label: breadcrumbLabel,
            url: url
          };

          if (breadcrumbLabel) {
            this.breadcrumbs.push(breadcrumb);
          }
        });
      }
    });
  }

}

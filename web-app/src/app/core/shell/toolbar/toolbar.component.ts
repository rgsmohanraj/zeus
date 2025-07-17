/** Angular Imports */
import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatSidenav } from '@angular/material/sidenav';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Router } from '@angular/router';

/** rxjs Imports */
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

/** Custom Services */
import { AuthenticationService } from '../../authentication/authentication.service';
import { environment } from 'environments/environment';

/**
 * Toolbar component.
 */
@Component({
  selector: 'mifosx-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent implements OnInit {

  /** Subscription to breakpoint observer for handset. */
  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches)
    );

  /** Sets the initial state of sidenav as collapsed. Not collapsed if false. */
  sidenavCollapsed = true;

  /** Instance of sidenav. */
  @Input() sidenav: MatSidenav;
  /** Sidenav collapse event. */
  @Output() collapse = new EventEmitter<boolean>();

  menus = [];
  /**
   * @param {BreakpointObserver} breakpointObserver Breakpoint observer to detect screen size.
   * @param {Router} router Router for navigation.
   * @param {AuthenticationService} authenticationService Authentication service.
   */
  constructor(private breakpointObserver: BreakpointObserver,
              private router: Router,
              private authenticationService: AuthenticationService,
              private dialog: MatDialog) { }

  /**
   * Subscribes to breakpoint for handset.
   */
  ngOnInit() {
    this.isHandset$.subscribe(isHandset => {
      if (isHandset && this.sidenavCollapsed) {
        this.toggleSidenavCollapse(false);
      }
    });

    this.menus.push({ link: '/home', title: 'Home', icon: 'home' });
    this.menus.push({ link: '/partner', title: 'Partner', icon: 'user' });
    this.menus.push({ link: '/clients', title: 'Client', icon: 'users' });
    this.menus.push({ link: '/loans', title: 'Loans', icon: 'money-bill-alt' });

    if(this.checkAccessPermission('READ_REPORT')) {
      this.menus.push({ link: '/reports', title: 'Reports', icon: 'chart-bar' });
    }
    if(this.checkAccessPermission('VIEW_BULK_IMPORT')) {
      this.menus.push({ link: '/organization/bulk-import', title: 'Bulk Upload', icon: 'upload' });
    }

  }

  checkAccessPermission(action: string) {
    return (this.authenticationService.getCredentials().permissions.includes('ALL_FUNCTIONS')
    || this.authenticationService.getCredentials().permissions.includes(action))
  }
  /**
   * Toggles the current state of sidenav.
   */
  toggleSidenav() {
    this.sidenav.toggle();
  }

  /**
   * Toggles the current collapsed state of sidenav.
   */
  toggleSidenavCollapse(sidenavCollapsed?: boolean) {
    this.sidenavCollapsed = sidenavCollapsed || !this.sidenavCollapsed;
    this.collapse.emit(this.sidenavCollapsed);
  }

  /**
   * Logs out the authenticated user and redirects to login page.
   */
  logout() {
    this.authenticationService.logout()
      .subscribe(() => this.router.navigate(['/login'], { replaceUrl: true }));
  }
  keyCloaklogout() {
    if(environment.oauth.enabled)
    {
    this.authenticationService.keycloaklogout()
      .subscribe(() => this.router.navigate(['/login'], { replaceUrl: true }));
    }else{
      this.logout();
    }
  }

  /**
   * Opens Mifos JIRA Wiki page.
   */
  help() {
    window.open('https://mifosforge.jira.com/wiki/spaces/docs/pages/52035622/User+Manual', '_blank');
  }

}

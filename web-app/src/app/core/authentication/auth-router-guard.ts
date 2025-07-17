import { Router, CanActivate, CanActivateChild, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthenticationService } from './authentication.service';
import { Injectable } from '@angular/core';

@Injectable()
export class AuthRouterGuard implements CanActivateChild, CanActivate{

  constructor(private router: Router,
    private authenticationService: AuthenticationService) { }
  
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.permissionChecking(route,state);
  }
  
  canActivateChild(childRoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.permissionChecking(childRoute,state);
  }

  private permissionChecking(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean{
    let routePermission = route?.data?.routePermission;
    console.log(route);
    let hasAccessPermission = true;
    if (routePermission && typeof routePermission === "string" && this.authenticationService.checkAccessPermission(routePermission) === false){
        hasAccessPermission = false;
    }else if(routePermission && typeof routePermission === "object"){
      let paramName = route?.data?.pathParam ?? "";
      routePermission = routePermission?.[route?.params?.[paramName]];
      hasAccessPermission = this.authenticationService.checkAccessPermission(routePermission) || routePermission === undefined;
    }
    if(hasAccessPermission === false)
      this.authenticationService.logoutAndNavigateToLoginPage();
    console.log("Persmission:"+hasAccessPermission);
    return hasAccessPermission;
  }
}

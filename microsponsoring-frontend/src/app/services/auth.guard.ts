import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, UrlTree } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const userType = localStorage.getItem('userType');
    const allowedTypes = route.data['allowedTypes'] as string[] | undefined;

    if (userType && allowedTypes && allowedTypes.includes(userType)) {
      return true;
    }

    if (userType) {
      switch (userType) {
        case 'ORGANISATION_NONPROFIT':
          return this.router.parseUrl('/dashboard/organisation');
        case 'SPONSOR':
          return this.router.parseUrl('/dashboard/sponsor');
        case 'ADMIN':
          return this.router.parseUrl('/dashboard/admin');
        default:
          return this.router.parseUrl('/login');
      }
    }
    return this.router.parseUrl('/login');
  }
} 
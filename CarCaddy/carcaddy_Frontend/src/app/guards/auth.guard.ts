import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return false;
    }

    const requiredRoles: string[] = route.data['roles'] || [];
    if (requiredRoles.length > 0) {
      const userRole = this.authService.getRole();
      if (!requiredRoles.includes(userRole)) {
        this.router.navigate(['/dashboard']);
        return false;
      }
    }

    return true;
  }
}

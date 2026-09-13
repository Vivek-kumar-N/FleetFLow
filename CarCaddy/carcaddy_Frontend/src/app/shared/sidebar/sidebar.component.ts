import { Component, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SidebarComponent {
  constructor(public authService: AuthService, public router: Router) {}

  get userInitial(): string {
    return this.authService.getUsername().charAt(0).toUpperCase();
  }

  get roleLabel(): string {
    const role = this.authService.getRole();
    if (role === 'ROLE_ADMIN') return 'Administrator';
    if (role === 'ROLE_EMPLOYEE') return 'Employee';
    if (role === 'ROLE_CUSTOMER') return 'Customer';
    return role;
  }

  logout(): void {
    this.authService.logout();
  }
}

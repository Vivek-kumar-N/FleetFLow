import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';

@Component({ selector: 'app-maintenance', templateUrl: './maintenance.component.html', styleUrls: ['./maintenance.component.css'] })
export class MaintenanceComponent {
  constructor(public authService: AuthService) {}
}

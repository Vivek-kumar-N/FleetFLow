import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';

@Component({ selector: 'app-rentals', templateUrl: './rentals.component.html', styleUrls: ['./rentals.component.css'] })
export class RentalsComponent {
  constructor(public authService: AuthService) {}
}

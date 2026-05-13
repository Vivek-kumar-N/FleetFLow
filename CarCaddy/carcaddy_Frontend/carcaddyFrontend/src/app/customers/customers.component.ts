import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';

@Component({ selector: 'app-customers', templateUrl: './customers.component.html', styleUrls: ['./customers.component.css'] })
export class CustomersComponent {
  constructor(public authService: AuthService) {}
}

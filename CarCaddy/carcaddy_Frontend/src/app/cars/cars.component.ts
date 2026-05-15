import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';

@Component({ selector: 'app-cars', templateUrl: './cars.component.html', styleUrls: ['./cars.component.css'] })
export class CarsComponent {
  constructor(public authService: AuthService) {}
}

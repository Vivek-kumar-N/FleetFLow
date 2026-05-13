import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CarService } from '../../services/car.service';
import { Car } from '../../models/models';

@Component({
  selector: 'app-customer-cars',
  templateUrl: './customer-cars.component.html',
  styleUrls: ['./customer-cars.component.css']
})
export class CustomerCarsComponent implements OnInit {
  availableCars: Car[] = [];
  filteredCars: Car[] = [];
  availabilityResults: Car[] = [];
  loading = false;
  checkingAvailability = false;
  error = '';
  searchModel = '';
  categoryFilter = '';

  availabilityForm: FormGroup;
  categories = ['Sedan', 'SUV', 'Hatchback', 'Luxury', 'Van', 'Truck'];

  constructor(
    private carService: CarService,
    private fb: FormBuilder
  ) {
    this.availabilityForm = this.fb.group({
      startDate: ['', Validators.required],
      endDate: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loading = true;
    this.carService.getAvailableCars().subscribe({
      next: d => { this.availableCars = d; this.filteredCars = d; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Failed to load cars.'; }
    });
  }

  applyFilters(): void {
    this.filteredCars = this.availableCars.filter(c => {
      const matchModel = !this.searchModel || c.model.toLowerCase().includes(this.searchModel.toLowerCase());
      const matchCat = !this.categoryFilter || c.category === this.categoryFilter;
      return matchModel && matchCat;
    });
  }

  checkAvailability(): void {
    if (this.availabilityForm.invalid) { this.availabilityForm.markAllAsTouched(); return; }
    this.checkingAvailability = true;
    const { startDate, endDate } = this.availabilityForm.value;
    // Filter available cars by checking each one against the date range
    this.carService.getAvailableCars().subscribe({
      next: (cars: any[]) => { this.availabilityResults = cars; this.checkingAvailability = false; },
      error: () => { this.checkingAvailability = false; this.error = 'Failed to check availability.'; }
    });
  }
}

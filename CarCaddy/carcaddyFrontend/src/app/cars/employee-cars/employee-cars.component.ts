import { Component, OnInit } from '@angular/core';
import { CarService } from '../../services/car.service';
import { Car } from '../../models/models';

@Component({
  selector: 'app-employee-cars',
  templateUrl: './employee-cars.component.html',
  styleUrls: ['./employee-cars.component.css']
})
export class EmployeeCarsComponent implements OnInit {
  cars: Car[] = [];
  filteredCars: Car[] = [];
  loading = false;
  error = '';
  searchModel = '';
  categoryFilter = '';
  statusFilter = 'AVAILABLE';

  categories = ['', 'Sedan', 'SUV', 'Hatchback', 'Luxury', 'Van', 'Truck'];

  constructor(private carService: CarService) {}

  ngOnInit(): void { this.loadCars(); }

  loadCars(): void {
    this.loading = true;
    this.carService.getAllCars().subscribe({
      next: d => { this.cars = d; this.applyFilters(); this.loading = false; },
      error: () => { this.loading = false; this.error = 'Failed to load cars.'; }
    });
  }

  applyFilters(): void {
    this.filteredCars = this.cars.filter(c => {
      const matchModel = !this.searchModel || c.model.toLowerCase().includes(this.searchModel.toLowerCase());
      const matchCat = !this.categoryFilter || c.category === this.categoryFilter;
      const matchStatus = !this.statusFilter || c.status === this.statusFilter;
      return matchModel && matchCat && matchStatus;
    });
  }
}

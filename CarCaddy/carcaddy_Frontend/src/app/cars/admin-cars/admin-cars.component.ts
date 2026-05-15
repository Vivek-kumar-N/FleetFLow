import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CarService } from '../../services/car.service';
import { AuthService } from '../../services/auth.service';
import { Car } from '../../models/models';

@Component({
  selector: 'app-admin-cars',
  templateUrl: './admin-cars.component.html',
  styleUrls: ['./admin-cars.component.css']
})
export class AdminCarsComponent implements OnInit {
  cars: Car[] = [];
  filteredCars: Car[] = [];
  loading = false;
  error = '';
  success = '';
  searchTerm = '';
  statusFilter = '';
  categoryFilter = '';

  showAddModal = false;
  showEditModal = false;
  showStatusModal = false;
  showMileageModal = false;
  selectedCar: Car | null = null;
  submitting = false;

  carForm: FormGroup;
  statusForm: FormGroup;
  mileageForm: FormGroup;

  categories = ['Sedan', 'SUV', 'Hatchback', 'Luxury', 'Van', 'Truck'];
  conditions = ['Excellent', 'Good', 'Fair', 'Poor'];
  statuses = ['AVAILABLE', 'RENTED', 'MAINTENANCE'];

  constructor(private carService: CarService, private fb: FormBuilder) {
    this.carForm = this.fb.group({
      registrationNumber: ['', Validators.required],
      model: ['', Validators.required],
      category: ['Sedan', Validators.required],
      color: ['', Validators.required],
      carCondition: ['Excellent', Validators.required],
      insuranceNumber: ['', Validators.required],
      status: ['AVAILABLE'],
      mileage: [0, [Validators.required, Validators.min(0)]],
      rentalRatePerDay: [50, [Validators.required, Validators.min(1)]]
    });
    this.statusForm = this.fb.group({ status: ['', Validators.required] });
    this.mileageForm = this.fb.group({ mileage: [0, [Validators.required, Validators.min(0)]] });
  }

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
      const s = this.searchTerm.toLowerCase();
      const matchSearch = !s || c.registrationNumber.toLowerCase().includes(s) || c.model.toLowerCase().includes(s);
      const matchStatus = !this.statusFilter || c.status === this.statusFilter;
      const matchCat = !this.categoryFilter || c.category === this.categoryFilter;
      return matchSearch && matchStatus && matchCat;
    });
  }

  openAddModal(): void { this.carForm.reset({ category: 'Sedan', carCondition: 'Excellent', status: 'AVAILABLE', mileage: 0, rentalRatePerDay: 50 }); this.carForm.get('registrationNumber')?.enable(); this.showAddModal = true; this.error = ''; this.success = ''; }
  openEditModal(car: Car): void { this.selectedCar = car; this.carForm.patchValue(car); this.carForm.get('registrationNumber')?.disable(); this.showEditModal = true; }
  openStatusModal(car: Car): void { this.selectedCar = car; this.statusForm.patchValue({ status: car.status }); this.showStatusModal = true; }
  openMileageModal(car: Car): void { this.selectedCar = car; this.mileageForm.patchValue({ mileage: car.mileage || 0 }); this.showMileageModal = true; }
  closeModals(): void { this.showAddModal = false; this.showEditModal = false; this.showStatusModal = false; this.showMileageModal = false; this.selectedCar = null; this.carForm.get('registrationNumber')?.enable(); this.submitting = false; }

  submitCar(): void {
    if (this.carForm.invalid) { this.carForm.markAllAsTouched(); return; }
    this.submitting = true;
    const data = this.carForm.getRawValue();
    const obs = this.showAddModal ? this.carService.addCar(data) : this.carService.updateCar(this.selectedCar!.registrationNumber, data);
    obs.subscribe({
      next: () => { this.success = this.showAddModal ? 'Car added!' : 'Car updated!'; this.closeModals(); this.loadCars(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  updateStatus(): void {
    if (!this.selectedCar) return;
    this.submitting = true;
    this.carService.updateCarStatus(this.selectedCar.registrationNumber, this.statusForm.value.status).subscribe({
      next: () => { this.success = 'Status updated!'; this.closeModals(); this.loadCars(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  updateMileage(): void {
    if (!this.selectedCar) return;
    this.submitting = true;
    this.carService.updateMileage(this.selectedCar.registrationNumber, this.mileageForm.value.mileage).subscribe({
      next: () => { this.success = 'Mileage updated!'; this.closeModals(); this.loadCars(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  get f() { return this.carForm.controls; }
}

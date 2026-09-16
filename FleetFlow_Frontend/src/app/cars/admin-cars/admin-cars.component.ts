import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CarService } from '../../services/car.service';
import { MaintenanceService } from '../../services/maintenance.service';
import { AuthService } from '../../services/auth.service';
import { Car, Maintenance } from '../../models/models';

@Component({
  selector: 'app-admin-cars',
  templateUrl: './admin-cars.component.html',
  styleUrls: ['./admin-cars.component.css']
})
export class AdminCarsComponent implements OnInit {
  cars: Car[] = [];
  filteredCars: Car[] = [];
  carsNeedingMaintenance: Set<string> = new Set();
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
  showMaintenanceHistoryModal = false;
  showDeleteModal = false;
  showDetailModal = false;
  selectedCar: Car | null = null;
  submitting = false;
  carMaintenanceHistory: Maintenance[] = [];
  carTotalMaintenanceCost = 0;
  loadingHistory = false;

  // Sorting
  sortField = '';
  sortDir: 'asc' | 'desc' = 'asc';

  carForm: FormGroup;
  statusForm: FormGroup;
  mileageForm: FormGroup;

  categories = ['Sedan', 'SUV', 'Hatchback', 'Luxury', 'Van', 'Truck'];
  conditions = ['Excellent', 'Good', 'Fair', 'Poor'];
  statuses = ['AVAILABLE', 'RENTED', 'MAINTENANCE'];

  constructor(private carService: CarService, private maintenanceService: MaintenanceService, private fb: FormBuilder) {
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
    // Load cars needing maintenance for the filter
    this.carService.getCarsRequiringMaintenance().subscribe({
      next: d => { this.carsNeedingMaintenance = new Set(d.map((c: Car) => c.registrationNumber)); },
      error: () => {}
    });
  }

  applyFilters(): void {
    let result = this.cars.filter(c => {
      const s = this.searchTerm.toLowerCase();
      const matchSearch = !s || c.registrationNumber.toLowerCase().includes(s) || c.model.toLowerCase().includes(s);
      // Special filter: REQUIRES_MAINTENANCE shows cars due for service (by business rules)
      const matchStatus = !this.statusFilter
        ? true
        : this.statusFilter === 'REQUIRES_MAINTENANCE'
          ? this.carsNeedingMaintenance.has(c.registrationNumber)
          : c.status === this.statusFilter;
      const matchCat = !this.categoryFilter || c.category === this.categoryFilter;
      return matchSearch && matchStatus && matchCat;
    });

    // Sorting
    if (this.sortField) {
      result = result.sort((a: any, b: any) => {
        const aVal = a[this.sortField] ?? '';
        const bVal = b[this.sortField] ?? '';
        const cmp = String(aVal).localeCompare(String(bVal), undefined, { numeric: true });
        return this.sortDir === 'asc' ? cmp : -cmp;
      });
    }

    this.filteredCars = result;
  }

  openAddModal(): void { this.carForm.reset({ category: 'Sedan', carCondition: 'Excellent', status: 'AVAILABLE', mileage: 0, rentalRatePerDay: 50 }); this.carForm.get('registrationNumber')?.enable(); this.showAddModal = true; this.error = ''; this.success = ''; }
  openEditModal(car: Car): void { this.selectedCar = car; this.carForm.patchValue(car); this.carForm.get('registrationNumber')?.disable(); this.showEditModal = true; }
  openStatusModal(car: Car): void { this.selectedCar = car; this.statusForm.patchValue({ status: car.status }); this.showStatusModal = true; }
  openMileageModal(car: Car): void { this.selectedCar = car; this.mileageForm.patchValue({ mileage: car.mileage || 0 }); this.showMileageModal = true; }

  openMaintenanceHistoryModal(car: Car): void {
    this.selectedCar = car;
    this.carMaintenanceHistory = [];
    this.carTotalMaintenanceCost = 0;
    this.loadingHistory = true;
    this.showMaintenanceHistoryModal = true;
    this.maintenanceService.getByCarRegistration(car.registrationNumber).subscribe({
      next: records => { this.carMaintenanceHistory = records; this.loadingHistory = false; },
      error: () => { this.loadingHistory = false; }
    });
    this.maintenanceService.getTotalCostByCar(car.registrationNumber).subscribe({
      next: cost => { this.carTotalMaintenanceCost = cost || 0; },
      error: () => {}
    });
  }

  openDeleteModal(car: Car): void { this.selectedCar = car; this.showDeleteModal = true; }

  openDetailModal(car: Car): void { this.selectedCar = car; this.showDetailModal = true; }

  deleteCar(): void {
    if (!this.selectedCar) return;
    this.submitting = true;
    this.carService.deleteCar(this.selectedCar.registrationNumber).subscribe({
      next: () => { this.success = `Car ${this.selectedCar?.registrationNumber} deleted successfully.`; this.closeModals(); this.loadCars(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  sortBy(field: string): void {
    if (this.sortField === field) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDir = 'asc';
    }
    this.applyFilters();
  }

  closeModals(): void { this.showAddModal = false; this.showEditModal = false; this.showStatusModal = false; this.showMileageModal = false; this.showMaintenanceHistoryModal = false; this.showDeleteModal = false; this.showDetailModal = false; this.selectedCar = null; this.carForm.get('registrationNumber')?.enable(); this.submitting = false; }

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

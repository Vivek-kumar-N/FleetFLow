import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer.service';
import { BookingService } from '../../services/booking.service';
import { AuthService } from '../../services/auth.service';
import { Customer, CustomerDTO, Booking } from '../../models/models';

@Component({
  selector: 'app-admin-customers',
  templateUrl: './admin-customers.component.html',
  styleUrls: ['./admin-customers.component.css']
})
export class AdminCustomersComponent implements OnInit {
  customers: Customer[] = [];
  filteredCustomers: Customer[] = [];
  selectedCustomerBookings: Booking[] = [];
  loading = false;
  error = '';
  success = '';
  searchTerm = '';
  showBlacklistedOnly = false;

  showAddModal = false;
  showEditModal = false;
  showBlacklistModal = false;
  showHistoryModal = false;
  showDeleteModal = false;
  selectedCustomer: Customer | null = null;
  submitting = false;
  blacklistReason = '';

  customerForm: FormGroup;
  contactForm: FormGroup;

  constructor(
    private customerService: CustomerService,
    private bookingService: BookingService,
    private fb: FormBuilder
  ) {
    this.customerForm = this.fb.group({
      customerName: ['', [Validators.required, Validators.minLength(2)]],
      contactNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      drivingLicense: ['', [Validators.required, Validators.minLength(5)]],
      occupation: [''],
      address: ['', [Validators.required, Validators.minLength(5)]],
      emailId: ['', [Validators.required, Validators.email]]
    });
    this.contactForm = this.fb.group({ contactNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]] });
  }

  ngOnInit(): void { this.loadCustomers(); }

  loadCustomers(): void {
    this.loading = true;
    this.customerService.getAllCustomers().subscribe({
      next: d => { this.customers = d; this.applyFilters(); this.loading = false; },
      error: () => { this.loading = false; this.error = 'Failed to load customers.'; }
    });
  }

  applyFilters(): void {
    this.filteredCustomers = this.customers.filter(c => {
      const s = this.searchTerm.toLowerCase();
      const matchSearch = !s || c.customerName.toLowerCase().includes(s) || (c.customerId || '').toLowerCase().includes(s) || c.emailId.toLowerCase().includes(s);
      const matchBl = !this.showBlacklistedOnly || c.blacklisted;
      return matchSearch && matchBl;
    });
  }

  openAddModal(): void { this.customerForm.reset(); this.showAddModal = true; this.error = ''; this.success = ''; }
  openEditModal(c: Customer): void { this.selectedCustomer = c; this.customerForm.patchValue(c); this.showEditModal = true; }
  openBlacklistModal(c: Customer): void { this.selectedCustomer = c; this.blacklistReason = ''; this.showBlacklistModal = true; }
  openDeleteModal(c: Customer): void { this.selectedCustomer = c; this.showDeleteModal = true; }
  openHistoryModal(c: Customer): void {
    this.selectedCustomer = c;
    this.showHistoryModal = true;
    this.bookingService.getBookingsByCustomer(c.customerId!).subscribe({ next: d => this.selectedCustomerBookings = d, error: () => {} });
  }
  closeModals(): void { this.showAddModal = false; this.showEditModal = false; this.showBlacklistModal = false; this.showHistoryModal = false; this.showDeleteModal = false; this.selectedCustomer = null; this.submitting = false; }

  submitCustomer(): void {
    if (this.customerForm.invalid) { this.customerForm.markAllAsTouched(); return; }
    this.submitting = true;
    const dto: CustomerDTO = this.customerForm.value;
    const obs = this.showAddModal ? this.customerService.addCustomer(dto) : this.customerService.updateCustomer(this.selectedCustomer!.customerId!, dto);
    obs.subscribe({
      next: () => { this.success = this.showAddModal ? 'Customer added!' : 'Customer updated!'; this.closeModals(); this.loadCustomers(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  blacklistCustomer(): void {
    if (!this.selectedCustomer) return;
    this.submitting = true;
    this.customerService.blacklistCustomer(this.selectedCustomer.customerId!).subscribe({
      next: () => { this.success = 'Customer blacklisted.'; this.closeModals(); this.loadCustomers(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  unblacklistCustomer(c: Customer): void {
    if (!confirm(`Remove blacklist restriction for ${c.customerName}?`)) return;
    this.customerService.unblacklistCustomer(c.customerId!).subscribe({
      next: () => { this.success = `${c.customerName} has been unblacklisted.`; this.loadCustomers(); },
      error: (err: any) => { this.error = AuthService.parseError(err); }
    });
  }

  deleteCustomer(): void {
    if (!this.selectedCustomer) return;
    this.submitting = true;
    this.customerService.deleteCustomer(this.selectedCustomer.customerId!).subscribe({
      next: () => { this.success = 'Customer deleted.'; this.closeModals(); this.loadCustomers(); },
      error: (err: any) => { this.submitting = false; this.error = AuthService.parseError(err); }
    });
  }

  get f() { return this.customerForm.controls; }
}

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer.service';
import { BookingService } from '../../services/booking.service';
import { Customer, CustomerDTO, Booking } from '../../models/models';

@Component({
  selector: 'app-employee-customers',
  templateUrl: './employee-customers.component.html',
  styleUrls: ['./employee-customers.component.css']
})
export class EmployeeCustomersComponent implements OnInit {
  customers: Customer[] = [];
  filteredCustomers: Customer[] = [];
  selectedCustomerBookings: Booking[] = [];
  loading = false;
  error = '';
  success = '';
  searchTerm = '';

  showRegisterModal = false;
  showContactModal = false;
  showHistoryModal = false;
  selectedCustomer: Customer | null = null;
  submitting = false;

  registerForm: FormGroup;
  contactForm: FormGroup;

  constructor(
    private customerService: CustomerService,
    private bookingService: BookingService,
    private fb: FormBuilder
  ) {
    this.registerForm = this.fb.group({
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
    const s = this.searchTerm.toLowerCase();
    this.filteredCustomers = this.customers.filter(c =>
      !s || c.customerName.toLowerCase().includes(s) || (c.customerId || '').toLowerCase().includes(s)
    );
  }

  openRegisterModal(): void { this.registerForm.reset(); this.showRegisterModal = true; this.error = ''; this.success = ''; }
  openContactModal(c: Customer): void { this.selectedCustomer = c; this.contactForm.reset({ contactNumber: c.contactNumber }); this.showContactModal = true; }
  openHistoryModal(c: Customer): void {
    this.selectedCustomer = c; this.showHistoryModal = true;
    this.bookingService.getBookingsByCustomer(c.customerId!).subscribe({ next: d => this.selectedCustomerBookings = d, error: () => {} });
  }
  closeModals(): void { this.showRegisterModal = false; this.showContactModal = false; this.showHistoryModal = false; this.selectedCustomer = null; this.submitting = false; }

  registerCustomer(): void {
    if (this.registerForm.invalid) { this.registerForm.markAllAsTouched(); return; }
    this.submitting = true;
    this.customerService.addCustomer(this.registerForm.value as CustomerDTO).subscribe({
      next: (saved: any) => { this.success = `Customer registered! ID: ${saved.customerId}`; this.closeModals(); this.loadCustomers(); },
      error: (err: any) => { this.submitting = false; this.error = err.error || 'Failed to register.'; }
    });
  }

  updateContact(): void {
    if (!this.selectedCustomer || this.contactForm.invalid) return;
    this.submitting = true;
    this.customerService.updateContact(this.selectedCustomer.customerId!, this.contactForm.value.contactNumber).subscribe({
      next: () => { this.success = 'Contact updated!'; this.closeModals(); this.loadCustomers(); },
      error: (err: any) => { this.submitting = false; this.error = err.error || 'Failed.'; }
    });
  }

  get f() { return this.registerForm.controls; }
}

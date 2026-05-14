import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Customer, CustomerDTO } from '../models/models';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private apiUrl = `${environment.apiUrl}/customers`;

  constructor(private http: HttpClient) {}

  addCustomer(dto: CustomerDTO): Observable<Customer> {
    return this.http.post<Customer>(this.apiUrl, dto);
  }

  updateCustomer(id: string, dto: CustomerDTO): Observable<Customer> {
    return this.http.put<Customer>(`${this.apiUrl}/${id}`, dto);
  }

  updateContact(id: string, contact: string): Observable<Customer> {
    return this.http.patch<Customer>(`${this.apiUrl}/${id}/contact/${contact}`, null);
  }

  getCustomerByUsername(username: string): Observable<Customer> {
    return this.http.get<Customer>(`${this.base}/by-username/${username}`);
  }

  getAllCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(this.apiUrl);
  }

  getCustomerById(id: string): Observable<Customer> {
    return this.http.get<Customer>(`${this.apiUrl}/${id}`);
  }

  getCustomersByName(name: string): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.apiUrl}/search/${name}`);
  }

  blacklistCustomer(id: string): Observable<Customer> {
    return this.http.patch<Customer>(`${this.apiUrl}/${id}/blacklist`, null);
  }

  getLoyaltyPoints(id: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${id}/loyalty-points`);
  }

  getLoyaltyDiscount(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}/loyalty-discount`);
  }

  isEligibleForFreeRental(id: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${id}/free-rental-eligibility`);
  }

  getCustomersWithMaxBookings(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.apiUrl}/max-bookings`);
  }

  deleteCustomer(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

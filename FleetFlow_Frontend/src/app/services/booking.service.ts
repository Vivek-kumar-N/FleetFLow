import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Booking, CreateBookingRequest, ModifyBookingRequest, ReturnCarRequest } from '../models/models';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private apiUrl = `${environment.apiUrl}/rentals`;

  constructor(private http: HttpClient) {}

  createBooking(request: CreateBookingRequest): Observable<Booking> {
    return this.http.post<Booking>(this.apiUrl, request);
  }

  modifyBooking(bookingId: number, request: ModifyBookingRequest): Observable<Booking> {
    return this.http.put<Booking>(`${this.apiUrl}/${bookingId}`, request);
  }

  cancelBooking(bookingId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${bookingId}`, { responseType: 'text' });
  }

  returnCar(bookingId: number, request: ReturnCarRequest): Observable<Booking> {
    return this.http.post<Booking>(`${this.apiUrl}/${bookingId}/return`, request);
  }

  checkAvailability(registrationNumber: string, startDate: string, endDate: string): Observable<boolean> {
    const params = new HttpParams()
      .set('registrationNumber', registrationNumber)
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<boolean>(`${this.apiUrl}/availability`, { params });
  }

  getAllBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(this.apiUrl);
  }

  getBookingById(id: number): Observable<Booking> {
    return this.http.get<Booking>(`${this.apiUrl}/${id}`);
  }

  getBookingsByCustomer(customerId: string): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/customer/${customerId}`);
  }

  getBookingsByCar(registrationNumber: string): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/car/${registrationNumber}`);
  }

  getActiveBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/active`);
  }

  getCompletedBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/completed`);
  }
}

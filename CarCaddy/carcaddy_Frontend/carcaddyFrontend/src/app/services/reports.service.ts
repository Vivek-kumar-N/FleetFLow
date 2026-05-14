import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DashboardStats } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ReportsService {
  private apiUrl = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard`);
  }

  getCustomersWithMaxBookings(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/customers/max-bookings`);
  }

  getCarsWithMinimalBookings(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.apiUrl}/cars/minimal-bookings`);
  }

  getIncomeByCarModel(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.apiUrl}/income/by-model`);
  }

  getRevenueForPeriod(startDate: string, endDate: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/revenue/period`, {
      params: new HttpParams().set('startDate', startDate).set('endDate', endDate)
    });
  }

  getMaintenanceCostByCarModel(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.apiUrl}/maintenance/cost-by-model`);
  }

  getMaintenanceCostByPeriod(startDate: string, endDate: string): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.apiUrl}/maintenance/cost-by-period`, {
      params: new HttpParams().set('startDate', startDate).set('endDate', endDate)
    });
  }

  getCarUtilizationReport(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.apiUrl}/car-utilization`);
  }

  getCustomerLoyaltyAnalytics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/customer-loyalty`);
  }

  getProfitabilityReport(): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.apiUrl}/profitability`);
  }

  getFleetHealthReport(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/fleet-health`);
  }

  getServiceCenterPerformance(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/service-center-performance`);
  }

  getBookingTrends(startDate: string, endDate: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/booking-trends`, {
      params: new HttpParams().set('startDate', startDate).set('endDate', endDate)
    });
  }
}

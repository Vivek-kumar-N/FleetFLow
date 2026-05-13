import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Maintenance } from '../models/models';

@Injectable({ providedIn: 'root' })
export class MaintenanceService {
  private apiUrl = `${environment.apiUrl}/maintenance`;

  constructor(private http: HttpClient) {}

  addMaintenance(dto: Maintenance): Observable<Maintenance> {
    return this.http.post<Maintenance>(this.apiUrl, dto);
  }

  scheduleRoutine(dto: Maintenance): Observable<Maintenance> {
    return this.http.post<Maintenance>(`${this.apiUrl}/routine`, dto);
  }

  addEmergency(dto: Maintenance): Observable<Maintenance> {
    return this.http.post<Maintenance>(`${this.apiUrl}/emergency`, dto);
  }

  updateStatus(id: number, status: string, completedDate?: string, cost?: number, performedBy?: string): Observable<Maintenance> {
    const body: any = { status };
    if (completedDate) body.completedDate = completedDate;
    if (cost !== undefined) body.cost = cost;
    if (performedBy) body.performedBy = performedBy;
    return this.http.patch<Maintenance>(`${this.apiUrl}/${id}/status`, body);
  }

  getAll(): Observable<Maintenance[]> {
    return this.http.get<Maintenance[]>(this.apiUrl);
  }

  getById(id: number): Observable<Maintenance> {
    return this.http.get<Maintenance>(`${this.apiUrl}/${id}`);
  }

  getByCarRegistration(regNumber: string): Observable<Maintenance[]> {
    return this.http.get<Maintenance[]>(`${this.apiUrl}/car/${regNumber}`);
  }

  getByType(type: string): Observable<Maintenance[]> {
    return this.http.get<Maintenance[]>(`${this.apiUrl}/type`, {
      params: new HttpParams().set('type', type)
    });
  }

  getByStatus(status: string): Observable<Maintenance[]> {
    return this.http.get<Maintenance[]>(`${this.apiUrl}/status`, {
      params: new HttpParams().set('status', status)
    });
  }

  getUpcoming(): Observable<Maintenance[]> {
    return this.http.get<Maintenance[]>(`${this.apiUrl}/upcoming`);
  }

  getOverdue(): Observable<Maintenance[]> {
    return this.http.get<Maintenance[]>(`${this.apiUrl}/overdue`);
  }

  getTotalCostByCar(regNumber: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/cost/${regNumber}`);
  }

  getByDateRange(start: string, end: string): Observable<Maintenance[]> {
    return this.http.get<Maintenance[]>(`${this.apiUrl}/range`, {
      params: new HttpParams().set('start', start).set('end', end)
    });
  }

  delete(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${id}`);
  }
}

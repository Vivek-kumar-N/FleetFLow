import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Employee } from '../models/models';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private apiUrl = `${environment.apiUrl}/employees`;

  constructor(private http: HttpClient) {}

  addEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.apiUrl, employee);
  }

  updateContactNumber(id: number, contactNumber: string): Observable<Employee> {
    return this.http.patch<Employee>(`${this.apiUrl}/${id}/contact`, { contactNumber });
  }

  changePassword(emailId: string, newPassword: string): Observable<string> {
    return this.http.put<string>(`${this.apiUrl}/change-password`, null, {
      params: new HttpParams().set('emailId', emailId).set('newPassword', newPassword)
    });
  }

  deleteEmployee(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${id}`, { 
      responseType: 'text' // <--- Added this
    });
  }

  getAllEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.apiUrl);
  }

  getEmployeeById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`);
  }

  getEmployeesByDesignation(designation: string): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.apiUrl}/designation/${designation}`);
  }

  setExpiryDate(id: number, expiryDate: string): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/${id}/expiry`, null, {
      params: new HttpParams().set('expiryDate', expiryDate)
    });
  }

 autoDeactivate(): Observable<any[]> {
    return this.http.put<any[]>(`${this.apiUrl}/auto-deactivate`, null);
  }
}

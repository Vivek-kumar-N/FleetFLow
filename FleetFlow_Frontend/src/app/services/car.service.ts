import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Car } from '../models/models';

@Injectable({ providedIn: 'root' })
export class CarService {
  private apiUrl = `${environment.apiUrl}/cars`;

  constructor(private http: HttpClient) {}

  addCar(car: Car): Observable<Car> {
    return this.http.post<Car>(this.apiUrl, car);
  }

  updateCar(registrationNumber: string, car: Car): Observable<Car> {
    return this.http.put<Car>(`${this.apiUrl}/${registrationNumber}`, car);
  }

  updateCarStatus(registrationNumber: string, status: string): Observable<Car> {
    return this.http.patch<Car>(`${this.apiUrl}/${registrationNumber}/status`, { status });
  }

  updateMileage(registrationNumber: string, mileage: number): Observable<Car> {
    return this.http.patch<Car>(`${this.apiUrl}/${registrationNumber}/mileage`, null, {
      params: new HttpParams().set('mileage', mileage.toString())
    });
  }

  getAllCars(): Observable<Car[]> {
    return this.http.get<Car[]>(this.apiUrl);
  }

  getCarByRegistrationNumber(registrationNumber: string): Observable<Car> {
    return this.http.get<Car>(`${this.apiUrl}/${registrationNumber}`);
  }

  getCarsByModel(model: string): Observable<Car[]> {
    return this.http.get<Car[]>(`${this.apiUrl}/model/${model}`);
  }

  getCarsByCategory(category: string): Observable<Car[]> {
    return this.http.get<Car[]>(`${this.apiUrl}/category/${category}`);
  }

  getCarsByStatus(status: string): Observable<Car[]> {
    return this.http.get<Car[]>(`${this.apiUrl}/status/${status}`);
  }

  getAvailableCars(): Observable<Car[]> {
    return this.http.get<Car[]>(`${this.apiUrl}/available`);
  }

  getCarsRequiringMaintenance(): Observable<Car[]> {
    return this.http.get<Car[]>(`${this.apiUrl}/maintenance`);
  }

  deleteCar(registrationNumber: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${registrationNumber}`);
  }
}

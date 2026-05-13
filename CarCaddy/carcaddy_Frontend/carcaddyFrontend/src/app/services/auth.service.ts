import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private tokenKey = 'cc_token';
  private userKey = 'cc_user';

  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.getStoredUser());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        localStorage.setItem(this.tokenKey, response.token);
        localStorage.setItem(this.userKey, JSON.stringify(response));
        this.currentUserSubject.next(response);
      })
    );
  }

  register(request: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, request);
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getRole(): string {
    const user = this.getStoredUser();
    return user?.role || '';
  }

  getUsername(): string {
    const user = this.getStoredUser();
    return user?.username || '';
  }

  isAdmin(): boolean {
    return this.getRole() === 'ROLE_ADMIN';
  }

  isEmployee(): boolean {
    return this.getRole() === 'ROLE_EMPLOYEE';
  }

  isCustomer(): boolean {
    return this.getRole() === 'ROLE_CUSTOMER';
  }

  changePassword(currentPassword: string, newPassword: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/change-password`, { currentPassword, newPassword });
  }

  updateUsername(newUsername: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/update-username`, { newUsername }).pipe(
      tap((res: any) => {
        if (res.token) {
          const user = this.getStoredUser();
          if (user) {
            user.token = res.token;
            user.username = res.username;
            localStorage.setItem(this.tokenKey, res.token);
            localStorage.setItem(this.userKey, JSON.stringify(user));
            this.currentUserSubject.next(user);
          }
        }
      })
    );
  }

  setSecurityQuestion(securityQuestion: string, securityAnswer: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/security-question`, { securityQuestion, securityAnswer });
  }

  getSecurityQuestion(): Observable<any> {
    return this.http.get(`${this.apiUrl}/security-question`);
  }

  getSecurityQuestionByUsername(username: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/security-question/${username}`);
  }

  resetPassword(username: string, securityAnswer: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset-password`, { username, securityAnswer, newPassword });
  }

  verifySecurityAnswer(username: string, securityAnswer: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/verify-security-answer`, { username, securityAnswer });
  }

  /** Parse any HTTP error response into a clean, human-readable string */
  static parseError(err: any): string {
    if (!err) return 'An unexpected error occurred.';
    const e = err.error;
    if (!e) return err.message || 'An unexpected error occurred.';
    // Our new structured error format
    if (typeof e === 'object') {
      if (e.message) return e.message;
      if (e.error) return e.error;
    }
    // Plain string body
    if (typeof e === 'string' && e.length < 300) return e;
    // Fallback for very long raw Spring messages — extract just the default messages
    if (typeof e === 'string') {
      const matches = e.match(/default message \[([^\]]+)\]/g);
      if (matches && matches.length > 0) {
        return matches.map(m => m.replace('default message [', '').replace(']', '')).join('; ');
      }
    }
    return 'Operation failed. Please try again.';
  }

  private getStoredUser(): AuthResponse | null {
    const stored = localStorage.getItem(this.userKey);
    return stored ? JSON.parse(stored) : null;
  }
}

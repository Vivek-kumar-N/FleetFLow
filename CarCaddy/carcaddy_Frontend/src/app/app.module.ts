import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

// Auth
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { ForgotPasswordComponent } from './auth/forgot-password/forgot-password.component';

// Dashboard (parent + role sub-components)
import { DashboardComponent } from './dashboard/dashboard.component';
import { AdminDashboardComponent } from './dashboard/admin-dashboard/admin-dashboard.component';
import { EmployeeDashboardComponent } from './dashboard/employee-dashboard/employee-dashboard.component';
import { CustomerDashboardComponent } from './dashboard/customer-dashboard/customer-dashboard.component';

// Cars (parent + role sub-components)
import { CarsComponent } from './cars/cars.component';
import { AdminCarsComponent } from './cars/admin-cars/admin-cars.component';
import { EmployeeCarsComponent } from './cars/employee-cars/employee-cars.component';
import { CustomerCarsComponent } from './cars/customer-cars/customer-cars.component';

// Customers (parent + role sub-components)
import { CustomersComponent } from './customers/customers.component';
import { AdminCustomersComponent } from './customers/admin-customers/admin-customers.component';
import { EmployeeCustomersComponent } from './customers/employee-customers/employee-customers.component';
import { CustomerSelfProfileComponent } from './customers/customer-self-profile/customer-self-profile.component';

// Employees (parent + role sub-components)
import { EmployeesComponent } from './employees/employees.component';
import { AdminEmployeesComponent } from './employees/admin-employees/admin-employees.component';
import { EmployeeProfileComponent } from './employees/employee-profile/employee-profile.component';

// Rentals (parent + role sub-components)
import { RentalsComponent } from './rentals/rentals.component';
import { AdminRentalsComponent } from './rentals/admin-rentals/admin-rentals.component';
import { EmployeeRentalsComponent } from './rentals/employee-rentals/employee-rentals.component';
import { CustomerRentalsComponent } from './rentals/customer-rentals/customer-rentals.component';

// Maintenance (parent + role sub-components)
import { MaintenanceComponent } from './maintenance/maintenance.component';
import { AdminMaintenanceComponent } from './maintenance/admin-maintenance/admin-maintenance.component';
import { EmployeeMaintenanceComponent } from './maintenance/employee-maintenance/employee-maintenance.component';
import { CustomerMaintenanceComponent } from './maintenance/customer-maintenance/customer-maintenance.component';

// Reports (parent + role sub-components)
import { ReportsComponent } from './reports/reports.component';
import { AdminReportsComponent } from './reports/admin-reports/admin-reports.component';
import { EmployeeReportsComponent } from './reports/employee-reports/employee-reports.component';

// Shared
import { SidebarComponent } from './shared/sidebar/sidebar.component';
import { ProfileComponent } from './shared/profile/profile.component';

// Interceptors
import { AuthInterceptor } from './interceptors/auth.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    // Auth
    LoginComponent,
    RegisterComponent,
    ForgotPasswordComponent,
    // Dashboard
    DashboardComponent,
    AdminDashboardComponent,
    EmployeeDashboardComponent,
    CustomerDashboardComponent,
    // Cars
    CarsComponent,
    AdminCarsComponent,
    EmployeeCarsComponent,
    CustomerCarsComponent,
    // Customers
    CustomersComponent,
    AdminCustomersComponent,
    EmployeeCustomersComponent,
    CustomerSelfProfileComponent,
    // Employees
    EmployeesComponent,
    AdminEmployeesComponent,
    EmployeeProfileComponent,
    // Rentals
    RentalsComponent,
    AdminRentalsComponent,
    EmployeeRentalsComponent,
    CustomerRentalsComponent,
    // Maintenance
    MaintenanceComponent,
    AdminMaintenanceComponent,
    EmployeeMaintenanceComponent,
    CustomerMaintenanceComponent,
    // Reports
    ReportsComponent,
    AdminReportsComponent,
    EmployeeReportsComponent,
    // Shared
    SidebarComponent,
    ProfileComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    AppRoutingModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}

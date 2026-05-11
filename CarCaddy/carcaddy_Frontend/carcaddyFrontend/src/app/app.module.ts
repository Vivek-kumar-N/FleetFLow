import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CreateBookingRequestComponent } from './rentalModule/create-booking-request/create-booking-request.component';
import { ModifyingRequestComponent } from './rental/modifying-request/modifying-request.component';
import { ReturnCarComponent } from './rentalModule/return-car/return-car.component';
import { AuthComponent } from './auth/auth.component';
import { CarsComponent } from './cars/cars.component';
import { CustomersComponent } from './customers/customers.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { EmployeesComponent } from './employees/employees.component';
import { GuardsComponent } from './guards/guards.component';
import { InterceptorsComponent } from './interceptors/interceptors.component';
import { MaintenanceComponent } from './maintenance/maintenance.component';
import { ModelsComponent } from './models/models.component';
import { ReportsComponent } from './reports/reports.component';
import { ServicesComponent } from './services/services.component';
import { SharedComponent } from './shared/shared.component';

@NgModule({
  declarations: [
    AppComponent,
    CreateBookingRequestComponent,
    ModifyingRequestComponent,
    ReturnCarComponent,
    AuthComponent,
    CarsComponent,
    CustomersComponent,
    DashboardComponent,
    EmployeesComponent,
    GuardsComponent,
    InterceptorsComponent,
    MaintenanceComponent,
    ModelsComponent,
    ReportsComponent,
    ServicesComponent,
    SharedComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }

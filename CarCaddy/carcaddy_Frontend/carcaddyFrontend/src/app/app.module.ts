import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CreateBookingRequestComponent } from './rentalModule/create-booking-request/create-booking-request.component';
import { ModifyingRequestComponent } from './rental/modifying-request/modifying-request.component';
import { ReturnCarComponent } from './rentalModule/return-car/return-car.component';

@NgModule({
  declarations: [
    AppComponent,
    CreateBookingRequestComponent,
    ModifyingRequestComponent,
    ReturnCarComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }

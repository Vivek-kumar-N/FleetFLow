# 🚗 FleetFlow — Fleet Management System

> A full-stack fleet management system for car rental businesses. Manage cars, customers, employees, bookings, maintenance and generate real-time reports — all from a single platform.

![FleetFlow](https://img.shields.io/badge/FleetFlow-Fleet%20Management-blue?style=for-the-badge&logo=car)
![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?style=flat-square&logo=springboot)
![Angular](https://img.shields.io/badge/Angular-16-red?style=flat-square&logo=angular)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)

---

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Database Design](#-database-design)
- [API Documentation](#-api-documentation)
- [Authentication & Authorization](#-authentication--authorization)
- [Screenshots](#-screenshots)
- [How to Run Locally](#-how-to-run-locally)
- [Environment Variables](#-environment-variables)
- [Testing](#-testing)
- [Future Improvements](#-future-improvements)

---

## 📌 Project Overview

**FleetFlow** is a comprehensive fleet management system designed for car rental businesses. It provides role-based access for Admins, Employees, and Customers with a clean Angular frontend and a secure Spring Boot REST API backend.

The system handles the complete lifecycle of a car rental — from vehicle registration and customer onboarding, to booking management, maintenance tracking, and financial reporting.

---

## ✨ Features

### 👑 Admin
- Full dashboard with fleet stats, revenue charts, and loyalty analytics
- Manage entire car fleet (add, update, delete, status control)
- Manage employees and customers
- View and manage all bookings
- Schedule and track maintenance (routine, repair, emergency)
- Generate detailed reports (revenue, utilization, profitability, fleet health)
- Blacklist / unblacklist customers
- Auto-maintenance alerts based on mileage and rental count

### 👷 Employee
- View available cars and active bookings
- Process car returns
- Manage maintenance records
- View customer information

### 🙋 Customer
- Register and log in with JWT authentication
- Browse available cars by category/model
- Create, modify, and cancel bookings
- View personal booking history
- Loyalty points system with automatic discounts
- Free rental eligibility after 500+ points

### 🔒 Security
- JWT-based authentication
- Role-based access control (ROLE_ADMIN, ROLE_EMPLOYEE, ROLE_CUSTOMER)
- Password reset via security question
- BCrypt password encoding

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core language |
| Spring Boot | 3.5 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | ORM / database layer |
| Hibernate | 6.x | JPA implementation |
| JWT (jjwt) | 0.11.5 | Token-based auth |
| MySQL Connector | 8.x | Database driver |
| Lombok | latest | Boilerplate reduction |
| Spring Mail | 3.x | Email notifications |
| Maven | 3.9 | Build tool |

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| Angular | 16 | SPA framework |
| TypeScript | 5.1 | Core language |
| Chart.js | 4.5 | Dashboard charts |
| RxJS | 7.8 | Reactive programming |
| Angular Router | 16 | Client-side routing |
| Bootstrap | 5.x | UI styling |
| Bootstrap Icons | latest | Icon library |

### Database
| Technology | Version |
|---|---|
| MySQL | 8.0 |

---

## 🏗 Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                         │
│                                                             │
│   ┌─────────────────────────────────────────────────────┐  │
│   │           Angular 16 SPA (localhost:4200)           │  │
│   │                                                     │  │
│   │  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │  │
│   │  │Auth Guard│  │JWT Inter-│  │  HTTP Services   │  │  │
│   │  │(Route    │  │ceptor    │  │  (auth, car,     │  │  │
│   │  │ protect) │  │(token    │  │  booking, etc.)  │  │  │
│   │  └──────────┘  │ inject)  │  └──────────────────┘  │  │
│   │                └──────────┘                         │  │
│   │  ┌─────────────────────────────────────────────┐    │  │
│   │  │              Components                     │    │  │
│   │  │  Dashboard │ Cars │ Bookings │ Maintenance  │    │  │
│   │  │  Customers │ Employees │ Reports │ Auth     │    │  │
│   │  └─────────────────────────────────────────────┘    │  │
│   └─────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP/REST (JSON)
                          │ Authorization: Bearer <JWT>
┌─────────────────────────▼───────────────────────────────────┐
│                       API LAYER                             │
│                                                             │
│   ┌─────────────────────────────────────────────────────┐  │
│   │         Spring Boot 3.5 (localhost:8080)            │  │
│   │                                                     │  │
│   │  ┌──────────────────────────────────────────────┐   │  │
│   │  │            Security Filter Chain             │   │  │
│   │  │   JWT Filter → Authentication → Authorization│   │  │
│   │  └──────────────────────────────────────────────┘   │  │
│   │                                                     │  │
│   │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────┐  │  │
│   │  │   Auth   │ │   Car    │ │ Booking  │ │ Main- │  │  │
│   │  │Controller│ │Controller│ │Controller│ │tenance│  │  │
│   │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └───┬───┘  │  │
│   │       │            │            │            │      │  │
│   │  ┌────▼─────────────▼────────────▼────────────▼───┐  │  │
│   │  │              Service Layer (Business Logic)    │  │  │
│   │  │  ICarService │ IBookingService │ ICustomer...  │  │  │
│   │  └────────────────────────┬───────────────────────┘  │  │
│   │                           │                          │  │
│   │  ┌────────────────────────▼───────────────────────┐  │  │
│   │  │         Repository Layer (Spring Data JPA)     │  │  │
│   │  │  CarRepo │ BookingRepo │ CustomerRepo │ ...    │  │  │
│   │  └────────────────────────────────────────────────┘  │  │
│   └─────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │ JDBC
┌─────────────────────────▼───────────────────────────────────┐
│                    DATABASE LAYER                           │
│                                                             │
│              MySQL 8.0 — FleetFlow_db                       │
│                                                             │
│   app_user │ car │ customer │ employee │ booking │          │
│   maintenance                                               │
└─────────────────────────────────────────────────────────────┘
```

### Frontend Module Structure

```
src/app/
├── auth/           → Login, Register, Forgot Password
├── dashboard/      → Admin, Employee, Customer dashboards
├── cars/           → Car management (Admin, Employee, Customer views)
├── customers/      → Customer management
├── employees/      → Employee management
├── rentals/        → Booking management + return/modify
├── maintenance/    → Maintenance scheduling and tracking
├── reports/        → Analytics and reports
├── shared/         → Sidebar, Profile components
├── services/       → HTTP service layer
├── guards/         → Route auth guard
├── interceptors/   → JWT token interceptor
└── models/         → TypeScript interfaces
```

---

## 🗄 Database Design

### Entity Relationship Diagram

```
┌─────────────┐       ┌─────────────┐
│  app_user   │       │  employee   │
│─────────────│  1:1  │─────────────│
│ user_id  PK │◄──────│ employee_id │
│ username    │       │ employee_   │
│ password    │       │   name      │
│ role        │       │ email_id    │
│ security_   │       │ contact_    │
│  question   │       │   number    │
│ security_   │       │ designation │
│  answer     │       │ date_of_    │
└─────────────┘       │   birth     │
       ▲              │ account_    │
       │ 1:1          │   type      │
┌──────┴──────┐       │ account_    │
│  customer   │       │   active    │
│─────────────│       │ expiry_date │
│ customer_id │       └─────────────┘
│ customer_   │
│   name      │       ┌─────────────┐
│ email_id    │       │     car     │
│ contact_    │       │─────────────│
│   number    │       │ registration│
│ driving_    │◄──┐   │   _number PK│
│  license    │   │   │ model       │
│ address     │   │   │ color       │
│ occupation  │   │   │ category    │
│ loyalty_    │   │   │ car_        │
│   points    │   │   │  condition  │
│ blacklisted │   │   │ insurance_  │
└──────┬──────┘   │   │   number    │
       │          │   │ mileage     │
       │ 1:N      │   │ rental_rate │
       │          │   │   _per_day  │
┌──────▼──────┐   │   │ status      │
│   booking   │   │   │ last_service│
│─────────────│   │   │   _date     │
│ booking_id  │   │   │ rental_count│
│ start_date  │   │   └──────┬──────┘
│ end_date    │   │          │
│ booking_    │   └──────────┤ 1:N
│   status    │              │
│ total_fare  │◄─────────────┘
│ discount    │
│ passenger_  │   ┌─────────────┐
│   count     │   │ maintenance │
│ mileage_at_ │   │─────────────│
│   start     │   │maintenance  │
│ mileage_at_ │   │   _id    PK │
│   return    │   │ maintenance │
└─────────────┘   │   _type     │
                  │ scheduled_  │
                  │   date      │
                  │ completed_  │
                  │   date      │
                  │ description │
                  │ cost        │
                  │ performed_by│
                  │ status      │
                  └─────────────┘
```

### Tables Summary

| Table | Primary Key | Description |
|---|---|---|
| `app_user` | `user_id` (AUTO) | Login credentials & roles |
| `car` | `registration_number` | Fleet vehicles |
| `customer` | `customer_id` (CUST-001) | Rental customers |
| `employee` | `employee_id` (AUTO) | Staff members |
| `booking` | `booking_id` (AUTO) | Rental bookings |
| `maintenance` | `maintenance_id` (AUTO) | Service records |

### Enums

| Enum | Values |
|---|---|
| `CarStatus` | `AVAILABLE`, `RENTED`, `MAINTENANCE` |
| `BookingStatus` | `CONFIRMED`, `ACTIVE`, `COMPLETED`, `CANCELLED`, `MODIFIED` |
| `MaintenanceStatus` | `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `MaintenanceType` | `ROUTINE`, `REPAIR`, `EMERGENCY` |
| `Role` | `ROLE_ADMIN`, `ROLE_EMPLOYEE`, `ROLE_CUSTOMER` |

---

## 📡 API Documentation

Base URL: `http://localhost:8080/api`

> 🔐 All endpoints except `/auth/login`, `/auth/register`, `/auth/reset-password`, `/auth/security-question/{username}` require `Authorization: Bearer <token>` header.

---

### 🔑 Auth — `/api/auth`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/register` | ❌ | Register new user (Admin/Employee/Customer) |
| POST | `/login` | ❌ | Login and receive JWT token |
| PUT | `/change-password` | ✅ | Change own password |
| PUT | `/update-username` | ✅ | Update own username |
| PUT | `/security-question` | ✅ | Set/update security question |
| GET | `/security-question` | ✅ | Get own security question |
| GET | `/security-question/{username}` | ❌ | Get security question by username |
| POST | `/reset-password` | ❌ | Reset password via security answer |
| POST | `/verify-security-answer` | ❌ | Verify security answer |

**Register Request (Customer):**
```json
{
  "username": "customer1",
  "password": "Cust@123",
  "role": "ROLE_CUSTOMER",
  "customerName": "Rahul Sharma",
  "emailId": "rahul@gmail.com",
  "contactNumber": "9876543210",
  "drivingLicense": "DL0420110012345",
  "address": "123 MG Road, Mumbai",
  "occupation": "Engineer"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "customer1",
  "role": "ROLE_CUSTOMER",
  "message": "Login successful"
}
```

---

### 🚗 Cars — `/api/cars`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/` | Add new car |
| GET | `/` | Get all cars |
| GET | `/{registrationNumber}` | Get car by registration |
| GET | `/available` | Get all available cars |
| GET | `/status/{status}` | Get cars by status |
| GET | `/model/{model}` | Get cars by model |
| GET | `/category/{category}` | Get cars by category |
| GET | `/maintenance` | Get cars requiring maintenance |
| PUT | `/{registrationNumber}` | Update car details |
| PATCH | `/{registrationNumber}/status` | Update car status |
| PATCH | `/{registrationNumber}/mileage` | Update car mileage |
| DELETE | `/{registrationNumber}` | Delete car |

**Add Car Request:**
```json
{
  "registrationNumber": "MH01AB1234",
  "model": "Swift",
  "color": "Red",
  "category": "Hatchback",
  "carCondition": "Excellent",
  "insuranceNumber": "INS-MH-001-2024",
  "mileage": 15000.0,
  "rentalRatePerDay": 1500.00,
  "status": "AVAILABLE"
}
```

---

### 👥 Customers — `/api/customers`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/` | Add customer manually |
| GET | `/` | Get all customers |
| GET | `/{id}` | Get customer by ID |
| GET | `/by-username/{username}` | Get customer by username |
| GET | `/search/{name}` | Search by name |
| GET | `/{id}/loyalty-points` | Get loyalty points |
| GET | `/{id}/loyalty-discount` | Get discount percentage |
| GET | `/{id}/free-rental-eligibility` | Check free rental eligibility |
| PUT | `/{id}` | Update customer |
| PATCH | `/{id}/contact/{contact}` | Update contact number |
| PATCH | `/{id}/blacklist` | Blacklist customer |
| PATCH | `/{id}/unblacklist` | Remove blacklist |
| DELETE | `/{id}` | Delete customer |

---

### 👔 Employees — `/api/employees`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/` | Add employee |
| GET | `/` | Get all employees |
| GET | `/{id}` | Get employee by ID |
| GET | `/designation/{designation}` | Get by designation |
| PUT | `/{id}/expiry` | Set expiry date |
| PUT | `/auto-deactivate` | Auto deactivate expired |
| PATCH | `/{id}/contact` | Update contact number |
| DELETE | `/{id}` | Delete employee |

---

### 📅 Rentals/Bookings — `/api/rentals`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/` | Create booking |
| GET | `/` | Get all bookings |
| GET | `/{bookingId}` | Get booking by ID |
| GET | `/customer/{customerId}` | Get bookings by customer |
| GET | `/car/{registrationNumber}` | Get bookings by car |
| GET | `/active` | Get active bookings |
| GET | `/completed` | Get completed bookings |
| GET | `/availability` | Check car availability |
| PUT | `/{bookingId}` | Modify booking |
| POST | `/{bookingId}/return` | Return car |
| DELETE | `/{bookingId}` | Cancel booking |

**Create Booking Request:**
```json
{
  "customerId": "CUST-001",
  "category": "Sedan",
  "model": "City",
  "startDate": "2026-09-20",
  "endDate": "2026-09-25",
  "passengerCount": 2
}
```

---

### 🔧 Maintenance — `/api/maintenance`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/` | Add maintenance record |
| POST | `/routine` | Schedule routine maintenance |
| POST | `/emergency` | Add emergency maintenance |
| GET | `/` | Get all records |
| GET | `/{id}` | Get by ID |
| GET | `/car/{regNumber}` | Get by car |
| GET | `/type?type=ROUTINE` | Get by type |
| GET | `/status?status=SCHEDULED` | Get by status |
| GET | `/upcoming` | Get upcoming |
| GET | `/overdue` | Get overdue |
| GET | `/cost/{regNumber}` | Get total cost by car |
| GET | `/range?start=&end=` | Get by date range |
| PATCH | `/{id}/status` | Update status |
| DELETE | `/{id}` | Delete record |

---

### 📊 Reports — `/api/reports`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/dashboard` | Dashboard statistics |
| GET | `/fleet-health` | Fleet health report |
| GET | `/customers/max-bookings` | Top customers by bookings |
| GET | `/cars/minimal-bookings` | Cars with low bookings |
| GET | `/cars/utilization` | Car utilization report |
| GET | `/income/by-model` | Revenue by car model |
| GET | `/revenue/period` | Revenue for date range |
| GET | `/maintenance/cost-by-model` | Maintenance cost by model |
| GET | `/maintenance/cost-by-period` | Maintenance cost by period |
| GET | `/car-utilization` | Car utilization map |
| GET | `/customer-loyalty` | Loyalty analytics |
| GET | `/profitability` | Profitability report |
| GET | `/booking-trends` | Booking trends by period |
| GET | `/service-center-performance` | Service center stats |

---

## 🔐 Authentication & Authorization

### JWT Flow

```
Client                          Server
  │                               │
  │──POST /auth/login────────────►│
  │   {username, password}        │
  │                               │── Validate credentials
  │                               │── Generate JWT token
  │◄─────────────{token}──────────│
  │                               │
  │──GET /api/cars ───────────────►│
  │   Authorization: Bearer token │
  │                               │── JwtFilter validates token
  │                               │── Extract role from token
  │                               │── Check authorization
  │◄──────────[car data]──────────│
```

### Role Permissions

| Feature | Admin | Employee | Customer |
|---|:---:|:---:|:---:|
| Manage Cars | ✅ | ❌ | ❌ (view only) |
| Manage Employees | ✅ | ❌ | ❌ |
| Manage Customers | ✅ | ✅ | ❌ (self only) |
| Create Booking | ✅ | ✅ | ✅ |
| Modify/Cancel Booking | ✅ | ✅ | ✅ (own) |
| Return Car | ✅ | ✅ | ❌ |
| Manage Maintenance | ✅ | ✅ | ❌ |
| View Reports | ✅ | ✅ | ❌ |
| Dashboard | ✅ | ✅ | ✅ (limited) |

### Loyalty Points System

| Points | Tier | Benefit |
|---|---|---|
| 0 – 49 | 🥉 Bronze | No discount |
| 50 – 199 | 🥈 Silver | No discount |
| 200 – 499 | 🥇 Gold | 5% discount |
| 500+ | 💎 Platinum | 1 free rental day |
| 500+ | 💎 Platinum | Free rental eligible |

---

## 📸 Screenshots

> The application includes the following screens:

| Screen | Role | Description |
|---|---|---|
| Login Page | All | JWT-based login |
| Register Page | All | Role-based registration |
| Admin Dashboard | Admin | Stats, charts, alerts |
| Employee Dashboard | Employee | Active bookings, maintenance |
| Customer Dashboard | Customer | Bookings, loyalty points |
| Fleet Management | Admin | Full car CRUD |
| Booking Management | All | Create/modify/return/cancel |
| Maintenance Tracker | Admin/Employee | Schedule & track service |
| Reports | Admin/Employee | Revenue, utilization charts |
| Customer Profile | Customer | Self-service profile |

---

## 🚀 How to Run Locally

### Prerequisites

| Tool | Version | Download |
|---|---|---|
| Java JDK | 17+ | [Download](https://www.oracle.com/java/technologies/downloads/) |
| Maven | 3.9+ | [Download](https://maven.apache.org/download.cgi) |
| Node.js | 16+ | [Download](https://nodejs.org/) |
| Angular CLI | 16 | `npm install -g @angular/cli@16` |
| MySQL | 8.0 | [Download](https://dev.mysql.com/downloads/) |
| Git | latest | [Download](https://git-scm.com/) |

---

### Step 1 — Clone the Repository

```bash
git clone https://github.com/Vivek-kumar-N/FleetFlow.git
cd FleetFlow
```

---

### Step 2 — Database Setup

Login to MySQL and create the database:

```sql
CREATE DATABASE IF NOT EXISTS FleetFlow_db;
```

> The tables will be **auto-created** by Hibernate on first run (`ddl-auto=update`).

---

### Step 3 — Configure Backend

Open `FleetFlow_Backend/FleetFlow/src/main/resources/application.properties` and update:

```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/FleetFlow_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

# Mail (optional - for email notifications)
spring.mail.username=YOUR_GMAIL
spring.mail.password=YOUR_APP_PASSWORD
```

---

### Step 4 — Run Backend

```bash
cd FleetFlow_Backend/FleetFlow

# Using Maven wrapper
./mvnw spring-boot:run

# OR using system Maven
mvn spring-boot:run
```

Backend starts at: **http://localhost:8080**

---

### Step 5 — Run Frontend

```bash
cd FleetFlow_Frontend

# Install dependencies
npm install

# Start development server
ng serve
```

Frontend starts at: **http://localhost:4200**

---

### Step 6 — Open in Browser

Navigate to **http://localhost:4200**

**Default test accounts** (register via app or Postman):
```
Admin    → username: admin1    | password: Admin@123
Employee → username: emp1      | password: Emp@123
Customer → username: customer1 | password: Cust@123
```

---

## ⚙ Environment Variables

### Backend — `application.properties`

| Property | Default | Description |
|---|---|---|
| `server.port` | `8080` | Backend server port |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/FleetFlow_db` | MySQL connection URL |
| `spring.datasource.username` | `root` | MySQL username |
| `spring.datasource.password` | _(empty)_ | MySQL password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto schema update |
| `spring.mail.host` | `smtp.gmail.com` | SMTP host |
| `spring.mail.port` | `587` | SMTP port |
| `spring.mail.username` | — | Gmail address |
| `spring.mail.password` | — | Gmail app password |

### Frontend — `environment.ts`

| Variable | Value | Description |
|---|---|---|
| `apiUrl` | `http://localhost:8080/api` | Backend API base URL |
| `production` | `false` | Production flag |

---

## 🧪 Testing

### Postman Collection

A complete Postman collection is included: **`FleetFlow_API_Tests.postman_collection.json`**

It contains **83 API requests** covering all modules with sample data.

**Import Steps:**
1. Open Postman
2. Click **Import**
3. Select `FleetFlow_API_Tests.postman_collection.json`
4. Click **Run collection** for automated testing

**Recommended Run Order:**
```
1. AUTH       → Register + Login (tokens auto-saved)
2. CARS       → Add 3-4 cars
3. EMPLOYEES  → Add employees
4. CUSTOMERS  → Verify customer from registration
5. RENTALS    → Create → Modify → Return → Cancel
6. MAINTENANCE→ Add → Update status
7. REPORTS    → All report endpoints
```

> Set **800ms delay** in Collection Runner for reliable execution.

### Manual Testing

Frontend: **http://localhost:4200**

Test each role separately:
- Login as Admin → test full CRUD
- Login as Employee → test bookings + maintenance
- Login as Customer → test booking flow + profile

---

## 🔮 Future Improvements

- [ ] **Email Notifications** — Booking confirmation, return reminders via SMTP
- [ ] **Payment Gateway Integration** — Razorpay / Stripe for online payments
- [ ] **Mobile App** — React Native / Flutter companion app
- [ ] **GPS Tracking** — Real-time vehicle location tracking
- [ ] **Document Upload** — Customer KYC document management
- [ ] **Multi-branch Support** — Manage multiple rental locations
- [ ] **SMS Notifications** — Twilio integration for SMS alerts
- [ ] **Advanced Analytics** — ML-based demand forecasting
- [ ] **Car Images** — Photo upload for fleet vehicles
- [ ] **Dynamic Pricing** — Peak season / surge pricing
- [ ] **Review System** — Customer feedback and ratings
- [ ] **Export Reports** — PDF / Excel export for reports
- [ ] **Docker Support** — Containerized deployment
- [ ] **CI/CD Pipeline** — GitHub Actions automated deployment
- [ ] **Unit & Integration Tests** — JUnit + Mockito test coverage

---

## 👨‍💻 Project Structure

```
FleetFlow/
├── FleetFlow_Backend/
│   └── FleetFlow/
│       ├── src/main/java/com/FleetFlow/
│       │   ├── config/          → App config
│       │   ├── controller/      → REST controllers
│       │   ├── dto/             → Request/Response DTOs
│       │   ├── entity/          → JPA entities + enums
│       │   ├── exception/       → Global exception handler
│       │   ├── repository/      → Spring Data JPA repos
│       │   ├── security/        → JWT + Security config
│       │   └── service/         → Business logic
│       └── src/main/resources/
│           └── application.properties
│
├── FleetFlow_Frontend/
│   └── src/app/
│       ├── auth/
│       ├── cars/
│       ├── customers/
│       ├── dashboard/
│       ├── employees/
│       ├── guards/
│       ├── interceptors/
│       ├── maintenance/
│       ├── models/
│       ├── rentals/
│       ├── reports/
│       └── shared/
│
├── FleetFlow_API_Tests.postman_collection.json
└── README.md
```

---

## 📄 License

This project is for educational purposes.

---

<div align="center">
  <b>Built with ❤️ using Spring Boot + Angular</b>
</div>


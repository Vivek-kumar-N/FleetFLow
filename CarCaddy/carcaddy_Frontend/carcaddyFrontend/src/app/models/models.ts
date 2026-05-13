// ==================== AUTH ====================
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  role: string;
  securityQuestion?: string;
  securityAnswer?: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
  message: string;
}

// ==================== EMPLOYEE ====================
export interface Employee {
  employeeId?: number;
  employeeName: string;
  contactNumber: string;
  dateOfBirth: string;
  designation: string;
  emailId: string;
  accountType: string;
  accountActive?: boolean;
  firstLogin?: boolean;
  password?: string;
  expiryDate?: string;
  createdAt?: string;
}

// ==================== CAR ====================
export interface Car {
  registrationNumber: string;
  model: string;
  category: string;
  color: string;
  carCondition: string;
  insuranceNumber: string;
  status: 'AVAILABLE' | 'RENTED' | 'MAINTENANCE';
  mileage?: number;
  lastServiceMileage?: number;
  lastServiceDate?: string;
  rentalRatePerDay?: number;
  rentalCount?: number;
  registrationDate?: string;
}

// ==================== CUSTOMER ====================
export interface Customer {
  customerId?: string;
  customerName: string;
  contactNumber: string;
  drivingLicense: string;
  occupation?: string;
  address: string;
  emailId: string;
  loyaltyPoints?: number;
  blacklisted?: boolean;
  blacklistReason?: string;
}

export interface CustomerDTO {
  customerName: string;
  contactNumber: string;
  drivingLicense: string;
  occupation?: string;
  address: string;
  emailId: string;
}

// ==================== BOOKING ====================
export interface Booking {
  bookingId?: number;
  startDate: string;
  endDate: string;
  returnDate?: string;
  totalFare?: number;
  discount?: number;
  passengerCount: number;
  bookingStatus?: string;
  mileageAtStart?: number;
  mileageAtReturn?: number;
  createdAt?: string;
  customer?: Customer;
  car?: Car;
}

export interface CreateBookingRequest {
  customerId: string;
  model: string;
  category: string;
  startDate: string;
  endDate: string;
  passengerCount: number;
}

export interface ModifyBookingRequest {
  startDate?: string;
  endDate?: string;
  model?: string;
  category?: string;
}

export interface ReturnCarRequest {
  mileageAtReturn: number;
  damaged: boolean;
  damageNotes?: string;
}

// ==================== MAINTENANCE ====================
export interface Maintenance {
  maintenanceId?: number;
  registrationNumber: string;
  maintenanceType: 'ROUTINE' | 'REPAIR' | 'EMERGENCY';
  scheduledDate: string;
  completedDate?: string;
  description?: string;
  cost?: number;
  performedBy?: string;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
}

// ==================== REPORTS ====================
export interface DashboardStats {
  totalEmployees: number;
  totalCars: number;
  totalCustomers: number;
  totalRentals: number;
  totalMaintenanceRecords: number;
}

export interface LoyaltyDiscount {
  discountPercent: number;
  customerId: string;
}

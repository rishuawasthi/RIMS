export type UserRole = 'ADMIN' | 'INVENTORY_MANAGER' | 'STORE_STAFF' | 'SUPPLIER';

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED';

export interface AppUserModel {
  userId?: number;
  username: string;
  fullName: string;
  email: string;
  phone?: string;
  role: UserRole;
  status: UserStatus;
  failedLoginAttempts?: number;
  lockedUntil?: string;
  lastLoginAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
  fullName: string;
}

export interface AuthUser {
  username: string;
  role: string;
  fullName: string;
}

// src/redux/types/auth.types.ts

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  password: string;
  phone?: string;
}

export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  roles: string[];
  tokenType: string;
  expiresIn: number;
}

export interface ApiResponse<T> {
  status: 'SUCCESS' | 'ERROR';
  message: string;
  data: T;
  timestamp: string;
}

export interface AuthState {
  user: User | null;
  activeRole: string | null;   // role currently being used
  showRolePicker: boolean;     // show popup when user has multiple roles
  loading: boolean;
  error: string | null;
  registerSuccess: boolean;
}
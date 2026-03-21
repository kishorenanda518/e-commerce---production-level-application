// src/redux/types/user.types.ts

export interface UserListItem {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'PENDING_VERIFICATION';
  emailVerified: boolean;
  roles: string[];
  createdAt: string;
  lastLoginAt: string;
}

export interface UserState {
  users: UserListItem[];
  loading: boolean;
  error: string | null;
  pagination: {
    totalElements: number;
    totalPages: number;
    currentPage: number;
    size: number;
  };
}
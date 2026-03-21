// src/routes/AppRoutes.tsx

import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAppSelector } from '../hooks/reduxHooks';
import {
  selectIsLoggedIn,
  selectActiveRole,
  selectShowRolePicker,
} from '../redux/slices/authSlice';

import LoginPage      from '../pages/LoginPage/LoginPage';
import UserDashboard  from '../pages/UserDashboard/UserDashboard';
import AdminDashboard from '../pages/AdminDashboard/AdminDashboard';
import RolePicker from '../RolePicker/RolePicker';

// ── Protected route wrapper ────────────────────────────────────
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isLoggedIn = useAppSelector(selectIsLoggedIn);
  return isLoggedIn ? <>{children}</> : <Navigate to="/login" replace />;
};

// ── Root redirect — based on active role ──────────────────────
const RoleRouter: React.FC = () => {
  const isLoggedIn     = useAppSelector(selectIsLoggedIn);
  const activeRole     = useAppSelector(selectActiveRole);
  const showRolePicker = useAppSelector(selectShowRolePicker);

  if (!isLoggedIn)     return <Navigate to="/login"  replace />;
  if (showRolePicker)  return <Navigate to="/login"  replace />;
  if (!activeRole)     return <Navigate to="/login"  replace />;

  return activeRole === 'ROLE_ADMIN'
    ? <Navigate to="/admin"     replace />
    : <Navigate to="/dashboard" replace />;
};

// ── All routes ─────────────────────────────────────────────────
const AppRoutes: React.FC = () => (
  <>
    {/* Role picker overlay — renders on top of any page */}
    <RolePicker />

    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />

      {/* Role-based redirect */}
      <Route path="/" element={<RoleRouter />} />

      {/* User dashboard */}
      <Route path="/dashboard" element={
        <ProtectedRoute>
          <UserDashboard />
        </ProtectedRoute>
      } />

      {/* Admin dashboard */}
      <Route path="/admin" element={
        <ProtectedRoute>
          <AdminDashboard />
        </ProtectedRoute>
      } />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  </>
);

export default AppRoutes;

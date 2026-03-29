import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAppSelector } from '../hooks/reduxHooks';
import { selectIsLoggedIn, selectActiveRole, selectShowRolePicker } from '../redux/slices/authSlice';
import LoginPage      from '../pages/LoginPage/LoginPage';
import UserDashboard  from '../pages/UserDashboard/UserDashboard';
import AdminDashboard from '../pages/AdminDashboard/AdminDashboard';
import CheckoutPage   from '../pages/CheckoutPage/CheckoutPage';
import OrdersPage     from '../pages/OrdersPage/OrdersPage';
import RolePicker from '../RolePicker/RolePicker';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isLoggedIn = useAppSelector(selectIsLoggedIn);
  return isLoggedIn ? <>{children}</> : <Navigate to="/login" replace />;
};

const RoleRouter: React.FC = () => {
  const isLoggedIn     = useAppSelector(selectIsLoggedIn);
  const activeRole     = useAppSelector(selectActiveRole);
  const showRolePicker = useAppSelector(selectShowRolePicker);
  if (!isLoggedIn)    return <Navigate to="/login"    replace />;
  if (showRolePicker) return <Navigate to="/login"    replace />;
  if (!activeRole)    return <Navigate to="/login"    replace />;
  return activeRole === 'ROLE_ADMIN'
    ? <Navigate to="/admin"     replace />
    : <Navigate to="/dashboard" replace />;
};

const AppRoutes: React.FC = () => (
  <>
    <RolePicker />
    <Routes>
      <Route path="/login"     element={<LoginPage />} />
      <Route path="/"          element={<RoleRouter />} />
      <Route path="/dashboard" element={<ProtectedRoute><UserDashboard /></ProtectedRoute>} />
      <Route path="/admin"     element={<ProtectedRoute><AdminDashboard /></ProtectedRoute>} />
      <Route path="/checkout"  element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
      <Route path="/orders"    element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
      <Route path="*"          element={<Navigate to="/" replace />} />
    </Routes>
  </>
);

export default AppRoutes;

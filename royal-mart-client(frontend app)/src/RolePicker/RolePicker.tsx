// src/components/RolePicker/RolePicker.tsx
//
// Shown after login when a user has more than one role.
// User picks a role → dispatches selectRole → AppRoutes redirects.

import React from 'react';

import './RolePicker.css';
import { logoutThunk, selectRole, selectShowRolePicker, selectUser } from '../redux/slices/authSlice';
import { useAppDispatch, useAppSelector } from '../hooks/reduxHooks';

// ── Role metadata ──────────────────────────────────────────────
const ROLE_META: Record<string, {
  label: string;
  description: string;
  icon: string;
  color: string;
}> = {
  ROLE_ADMIN: {
    label:       'Administrator',
    description: 'Manage products, users, orders and full system access',
    icon:        '⚙️',
    color:       'rp__card--admin',
  },
  ROLE_USER: {
    label:       'Customer',
    description: 'Browse products, place orders and manage your profile',
    icon:        '🛍️',
    color:       'rp__card--user',
  },
  ROLE_MANAGER: {
    label:       'Manager',
    description: 'Manage inventory, orders and reports',
    icon:        '📊',
    color:       'rp__card--manager',
  },
};

const RolePicker: React.FC = () => {
  const dispatch       = useAppDispatch();
  const user           = useAppSelector(selectUser);
  const showRolePicker = useAppSelector(selectShowRolePicker);

  if (!showRolePicker || !user) return null;

  const roles: string[] = user.roles || [];

  const handleSelectRole = (role: string) => {
    dispatch(selectRole(role));
  };

  const handleCancel = async () => {
    await dispatch(logoutThunk());
  };

  return (
    <div className="rp__overlay">
      <div className="rp__modal">

        {/* Header */}
        <div className="rp__header">
          <div className="rp__crown">♛</div>
          <h2 className="rp__title">Choose Your Role</h2>
          <p className="rp__subtitle">
            Welcome back, <strong>{user.firstName}</strong>!
            You have multiple roles. Select how you'd like to sign in.
          </p>
        </div>

        {/* Role Cards */}
        <div className="rp__cards">
          {roles.map((role) => {
            const meta = ROLE_META[role] || {
              label:       role.replace('ROLE_', ''),
              description: 'Access your dashboard',
              icon:        '👤',
              color:       'rp__card--default',
            };

            return (
              <button
                key={role}
                className={`rp__card ${meta.color}`}
                onClick={() => handleSelectRole(role)}
              >
                <div className="rp__card-icon">{meta.icon}</div>
                <div className="rp__card-body">
                  <div className="rp__card-label">{meta.label}</div>
                  <div className="rp__card-desc">{meta.description}</div>
                </div>
                <div className="rp__card-arrow">→</div>
              </button>
            );
          })}
        </div>

        {/* Footer */}
        <div className="rp__footer">
          <button className="rp__cancel" onClick={handleCancel}>
            Sign out instead
          </button>
        </div>
      </div>
    </div>
  );
};

export default RolePicker;

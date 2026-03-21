// src/redux/slices/authSlice.ts

import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { loginApi, registerApi, logoutApi } from '../services/authService';
import { AuthState, LoginRequest, RegisterRequest } from '../types/auth.types';

// ── Thunks ─────────────────────────────────────────────────────
export const loginThunk = createAsyncThunk(
  'auth/login',
  async (payload: LoginRequest, { rejectWithValue }) => {
    try {
      return await loginApi(payload);
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message || 'Login failed.');
    }
  }
);

export const registerThunk = createAsyncThunk(
  'auth/register',
  async (payload: RegisterRequest, { rejectWithValue }) => {
    try {
      return await registerApi(payload);
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message || 'Registration failed.');
    }
  }
);

export const logoutThunk = createAsyncThunk('auth/logout', async () => {
  try { await logoutApi(); } catch {}
});

// ── Initial State ──────────────────────────────────────────────
const initialState: AuthState = {
  user:           null,
  activeRole:     null,
  showRolePicker: false,
  loading:        false,
  error:          null,
  registerSuccess: false,
};

// ── Slice ──────────────────────────────────────────────────────
const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    // user picks a role from the popup
    selectRole(state, action) {
      state.activeRole     = action.payload;
      state.showRolePicker = false;
    },
    closeRolePicker(state) {
      state.showRolePicker = false;
    },
    clearAuthError(state)       { state.error = null; },
    clearRegisterSuccess(state) { state.registerSuccess = false; },
    resetAuth(state) {
      state.user           = null;
      state.activeRole     = null;
      state.showRolePicker = false;
      state.error          = null;
      state.loading        = false;
      state.registerSuccess = false;
    },
  },
  extraReducers: (builder) => {
    builder
      // login
      .addCase(loginThunk.pending,   (state) => { state.loading = true; state.error = null; })
      .addCase(loginThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.user    = action.payload;
        const roles   = action.payload?.roles || [];

        if (roles.length > 1) {
          // multiple roles → show picker popup
          state.showRolePicker = true;
          state.activeRole     = null;
        } else {
          // single role → go directly
          state.showRolePicker = false;
          state.activeRole     = roles[0] || null;
        }
      })
      .addCase(loginThunk.rejected,  (state, action) => {
        state.loading = false;
        state.error   = action.payload as string;
      })
      // register
      .addCase(registerThunk.pending,   (state) => { state.loading = true; state.error = null; })
      .addCase(registerThunk.fulfilled, (state) => { state.loading = false; state.registerSuccess = true; })
      .addCase(registerThunk.rejected,  (state, action) => { state.loading = false; state.error = action.payload as string; })
      // logout
      .addCase(logoutThunk.fulfilled, (state) => {
        state.user           = null;
        state.activeRole     = null;
        state.showRolePicker = false;
        state.error          = null;
      });
  },
});

export const {
  selectRole,
  closeRolePicker,
  clearAuthError,
  clearRegisterSuccess,
  resetAuth,
} = authSlice.actions;

// ── Selectors ──────────────────────────────────────────────────
export const selectUser            = (state: any) => state.auth.user;
export const selectActiveRole      = (state: any) => state.auth.activeRole;
export const selectShowRolePicker  = (state: any) => state.auth.showRolePicker;
export const selectAuthLoading     = (state: any) => state.auth.loading;
export const selectAuthError       = (state: any) => state.auth.error;
export const selectRegisterSuccess = (state: any) => state.auth.registerSuccess;
export const selectIsLoggedIn      = (state: any) => !!state.auth.user;
export const selectIsRoleSelected  = (state: any) => !!state.auth.activeRole;

export default authSlice.reducer;
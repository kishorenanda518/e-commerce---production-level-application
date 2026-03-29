// src/redux/slices/userSlice.ts

import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { fetchUsersApi, updateUserStatusApi } from '../services/userService';
import { UserState } from '../types/user.types';

const initialState: UserState = {
  users:   [],
  loading: false,
  error:   null,
  pagination: {
    totalElements: 0,
    totalPages:    0,
    currentPage:   0,
    size:          10,
  },
};

// ── Thunks ────────────────────────────────────────────────────
export const loadUsersThunk = createAsyncThunk(
  'users/loadUsers',
  async (
    { page, size, token }: { page: number; size: number; token: string },
    { rejectWithValue }
  ) => {
    try {
      return await fetchUsersApi(page, size, token);
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message || 'Failed to load users');
    }
  }
);

export const updateUserStatusThunk = createAsyncThunk(
  'users/updateStatus',
  async (
    { userId, status, token }: { userId: string; status: string; token: string },
    { rejectWithValue }
  ) => {
    try {
      return await updateUserStatusApi(userId, status, token);
    } catch (err: any) {
      return rejectWithValue('Failed to update user status');
    }
  }
);

// ── Slice ─────────────────────────────────────────────────────
const userSlice = createSlice({
  name: 'users',
  initialState,
  reducers: {
    clearUsersError(state) { state.error = null; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loadUsersThunk.pending,   (state) => { state.loading = true; state.error = null; })
      .addCase(loadUsersThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.users   = action.payload?.content || [];
        state.pagination = {
          totalElements: action.payload?.totalElements || 0,
          totalPages:    action.payload?.totalPages    || 0,
          currentPage:   action.payload?.number        || 0,
          size:          action.payload?.size          || 10,
        };
      })
      .addCase(loadUsersThunk.rejected,  (state, action) => {
        state.loading = false;
        state.error   = action.payload as string;
      })
      .addCase(updateUserStatusThunk.fulfilled, (state, action) => {
        const updated = action.payload;
        const idx = state.users.findIndex(u => u.id === updated.id);
        if (idx !== -1) state.users[idx] = updated;
      });
  },
});

export const { clearUsersError } = userSlice.actions;

export const selectUsers          = (state: any) => state.users.users;
export const selectUsersLoading   = (state: any) => state.users.loading;
export const selectUsersError     = (state: any) => state.users.error;
export const selectUsersPagination = (state: any) => state.users.pagination;

export default userSlice.reducer;
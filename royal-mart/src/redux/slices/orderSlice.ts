// src/redux/slices/orderSlice.ts

import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import {
  placeOrderApi,
  getMyOrdersApi,
  getOrderByIdApi,
  getOrderHistoryApi,
  cancelOrderApi,
} from '../services/orderService';
import { OrderState, CreateOrderRequest } from '../types/order.types';

// ── Initial state ──────────────────────────────────────────────
const initialState: OrderState = {
  orders:       [],
  currentOrder: null,
  orderHistory: [],
  loading:      false,
  placing:      false,
  error:        null,
  orderSuccess: false,
  pagination: {
    totalElements: 0,
    totalPages:    0,
    currentPage:   0,
    size:          10,
  },
};

// ── Thunks ─────────────────────────────────────────────────────

// Place order
export const placeOrderThunk = createAsyncThunk(
  'orders/place',
  async (payload: CreateOrderRequest, { rejectWithValue }) => {
    try {
      return await placeOrderApi(payload);
    } catch (err: any) {
      return rejectWithValue(
        err.response?.data?.message || 'Failed to place order. Please try again.'
      );
    }
  }
);

// Get my orders
export const getMyOrdersThunk = createAsyncThunk(
  'orders/getMyOrders',
  async ({ page = 0, size = 10 }: { page?: number; size?: number }, { rejectWithValue }) => {
    try {
      return await getMyOrdersApi(page, size);
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message || 'Failed to fetch orders');
    }
  }
);

// Get order by ID
export const getOrderByIdThunk = createAsyncThunk(
  'orders/getById',
  async (orderId: string, { rejectWithValue }) => {
    try {
      return await getOrderByIdApi(orderId);
    } catch (err: any) {
      return rejectWithValue('Order not found');
    }
  }
);

// Get order history
export const getOrderHistoryThunk = createAsyncThunk(
  'orders/getHistory',
  async (orderId: string, { rejectWithValue }) => {
    try {
      return await getOrderHistoryApi(orderId);
    } catch (err: any) {
      return rejectWithValue('Failed to fetch order history');
    }
  }
);

// Cancel order
export const cancelOrderThunk = createAsyncThunk(
  'orders/cancel',
  async ({ orderId, reason }: { orderId: string; reason?: string }, { rejectWithValue }) => {
    try {
      return await cancelOrderApi(orderId, reason);
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message || 'Failed to cancel order');
    }
  }
);

// ── Slice ──────────────────────────────────────────────────────
const orderSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {
    clearOrderError(state)   { state.error = null; },
    clearOrderSuccess(state) { state.orderSuccess = false; state.currentOrder = null; },
    clearCurrentOrder(state) { state.currentOrder = null; },
  },
  extraReducers: (builder) => {
    builder
      // ── place order ─────────────────────────────────────────
      .addCase(placeOrderThunk.pending,   (state) => { state.placing = true;  state.error = null; })
      .addCase(placeOrderThunk.fulfilled, (state, action) => {
        state.placing      = false;
        state.orderSuccess = true;
        state.currentOrder = action.payload;
        // add to top of orders list
        state.orders.unshift(action.payload);
      })
      .addCase(placeOrderThunk.rejected,  (state, action) => {
        state.placing = false;
        state.error   = action.payload as string;
      })

      // ── get my orders ───────────────────────────────────────
      .addCase(getMyOrdersThunk.pending,   (state) => { state.loading = true; state.error = null; })
      .addCase(getMyOrdersThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.orders  = action.payload?.content || [];
        state.pagination = {
          totalElements: action.payload?.totalElements || 0,
          totalPages:    action.payload?.totalPages    || 0,
          currentPage:   action.payload?.number        || 0,
          size:          action.payload?.size          || 10,
        };
      })
      .addCase(getMyOrdersThunk.rejected,  (state, action) => {
        state.loading = false;
        state.error   = action.payload as string;
      })

      // ── get order by id ─────────────────────────────────────
      .addCase(getOrderByIdThunk.pending,   (state) => { state.loading = true; })
      .addCase(getOrderByIdThunk.fulfilled, (state, action) => {
        state.loading      = false;
        state.currentOrder = action.payload;
      })
      .addCase(getOrderByIdThunk.rejected,  (state) => { state.loading = false; })

      // ── order history ───────────────────────────────────────
      .addCase(getOrderHistoryThunk.fulfilled, (state, action) => {
        state.orderHistory = action.payload || [];
      })

      // ── cancel order ────────────────────────────────────────
      .addCase(cancelOrderThunk.fulfilled, (state, action) => {
        const updated = action.payload;
        const idx = state.orders.findIndex(o => o.id === updated.id);
        if (idx !== -1) state.orders[idx] = updated;
        if (state.currentOrder?.id === updated.id) state.currentOrder = updated;
      })
      .addCase(cancelOrderThunk.rejected, (state, action) => {
        state.error = action.payload as string;
      });
  },
});

export const { clearOrderError, clearOrderSuccess, clearCurrentOrder } = orderSlice.actions;

// ── Selectors ──────────────────────────────────────────────────
// replace all selectors at bottom of orderSlice.ts with these:
export const selectOrders          = (state: any) => state.orders?.orders       ?? [];
export const selectCurrentOrder    = (state: any) => state.orders?.currentOrder ?? null;
export const selectOrderHistory    = (state: any) => state.orders?.orderHistory ?? [];
export const selectOrderLoading    = (state: any) => state.orders?.loading      ?? false;
export const selectOrderPlacing    = (state: any) => state.orders?.placing      ?? false;
export const selectOrderError      = (state: any) => state.orders?.error        ?? null;
export const selectOrderSuccess    = (state: any) => state.orders?.orderSuccess ?? false;
export const selectOrderPagination = (state: any) => state.orders?.pagination   ?? {
  totalElements: 0, totalPages: 0, currentPage: 0, size: 10,
};

export default orderSlice.reducer;
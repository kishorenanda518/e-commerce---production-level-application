// src/redux/services/orderService.ts

import axios from 'axios';
import { ORDER_ENDPOINTS } from '../../constants/apiEndpoints';
import { CreateOrderRequest } from '../types/order.types';

// ── Read JWT token from localStorage ─────────────────────────
const getToken = (): string | null => {
  try {
    const raw = localStorage.getItem('rm_auth');
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    // token could be at user.accessToken or user.token
    return parsed?.user?.accessToken
        || parsed?.user?.token
        || parsed?.accessToken
        || null;
  } catch { return null; }
};

const authHeaders = () => {
  const token = getToken();
  return token
    ? { Authorization: `Bearer ${token}` }
    : {};
};

// ── Place new order ───────────────────────────────────────────
export const placeOrderApi = async (payload: CreateOrderRequest) => {
  const res = await axios.post(
    ORDER_ENDPOINTS.CREATE,
    payload,
    { withCredentials: true, headers: authHeaders() }
  );
  return res.data.data;
};

// ── Get my orders ─────────────────────────────────────────────
export const getMyOrdersApi = async (page = 0, size = 10) => {
  const res = await axios.get(ORDER_ENDPOINTS.MY_ORDERS, {
    params: { page, size },
    withCredentials: true,
    headers: authHeaders(),
  });
  return res.data.data;
};

// ── Get order by ID ───────────────────────────────────────────
export const getOrderByIdApi = async (orderId: string) => {
  const res = await axios.get(ORDER_ENDPOINTS.BY_ID(orderId), {
    withCredentials: true,
    headers: authHeaders(),
  });
  return res.data.data;
};

// ── Get order by number ───────────────────────────────────────
export const getOrderByNumberApi = async (orderNumber: string) => {
  const res = await axios.get(ORDER_ENDPOINTS.BY_NUMBER(orderNumber), {
    withCredentials: true,
    headers: authHeaders(),
  });
  return res.data.data;
};

// ── Get order status history ──────────────────────────────────
export const getOrderHistoryApi = async (orderId: string) => {
  const res = await axios.get(ORDER_ENDPOINTS.HISTORY(orderId), {
    withCredentials: true,
    headers: authHeaders(),
  });
  return res.data.data;
};

// ── Cancel order ──────────────────────────────────────────────
export const cancelOrderApi = async (orderId: string, reason?: string) => {
  const res = await axios.patch(
    ORDER_ENDPOINTS.CANCEL(orderId),
    { reason: reason || 'Cancelled by user' },
    { withCredentials: true, headers: authHeaders() }
  );
  return res.data.data;
};
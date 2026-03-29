// src/redux/slices/cartSlice.ts
//
// Cart is managed purely in Redux + localStorage.
// No backend API call needed for cart — only when placing order.

import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { CartState, CartItem }        from '../types/cart.types';

// ── helpers ───────────────────────────────────────────────────
const calcTotals = (items: CartItem[]) => {
  const totalItems = items.reduce((sum, i) => sum + i.quantity, 0);
  const totalPrice = items.reduce((sum, i) => sum + i.price * i.quantity, 0);
  const originalPrice = items.reduce(
    (sum, i) => sum + (i.compareAtPrice || i.price) * i.quantity, 0
  );
  const discount   = originalPrice - totalPrice;
  const finalPrice = totalPrice;
  return { totalItems, totalPrice, discount, finalPrice };
};

// ── load from localStorage ────────────────────────────────────
const loadCart = (): CartItem[] => {
  try {
    const data = localStorage.getItem('rm_cart');
    return data ? JSON.parse(data) : [];
  } catch { return []; }
};

// ── save to localStorage ──────────────────────────────────────
const saveCart = (items: CartItem[]) => {
  try { localStorage.setItem('rm_cart', JSON.stringify(items)); } catch {}
};

const initialItems = loadCart();
const initialTotals = calcTotals(initialItems);

const initialState: CartState = {
  items:      initialItems,
  isOpen:     false,
  ...initialTotals,
};

// ── Slice ─────────────────────────────────────────────────────
const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {

    // ── Add item — if exists increase qty ─────────────────────
    addToCart(state, action: PayloadAction<CartItem>) {
      const existing = state.items.find(
        i => i.productId === action.payload.productId
      );
      if (existing) {
        // respect maxStock
        existing.quantity = Math.min(
          existing.quantity + action.payload.quantity,
          existing.maxStock
        );
      } else {
        state.items.push({ ...action.payload });
      }
      saveCart(state.items);
      const totals = calcTotals(state.items);
      Object.assign(state, totals);
      state.isOpen = true;   // open cart drawer on add
    },

    // ── Remove item completely ────────────────────────────────
    removeFromCart(state, action: PayloadAction<string>) {
      state.items = state.items.filter(
        i => i.productId !== action.payload
      );
      saveCart(state.items);
      Object.assign(state, calcTotals(state.items));
    },

    // ── Update quantity ───────────────────────────────────────
    updateQuantity(state, action: PayloadAction<{ productId: string; quantity: number }>) {
      const item = state.items.find(
        i => i.productId === action.payload.productId
      );
      if (item) {
        if (action.payload.quantity <= 0) {
          state.items = state.items.filter(
            i => i.productId !== action.payload.productId
          );
        } else {
          item.quantity = Math.min(action.payload.quantity, item.maxStock);
        }
      }
      saveCart(state.items);
      Object.assign(state, calcTotals(state.items));
    },

    // ── Clear entire cart ─────────────────────────────────────
    clearCart(state) {
      state.items = [];
      saveCart([]);
      Object.assign(state, calcTotals([]));
    },

    // ── Open/close cart drawer ────────────────────────────────
    openCart(state)  { state.isOpen = true;  },
    closeCart(state) { state.isOpen = false; },
    toggleCart(state){ state.isOpen = !state.isOpen; },
  },
});

export const {
  addToCart,
  removeFromCart,
  updateQuantity,
  clearCart,
  openCart,
  closeCart,
  toggleCart,
} = cartSlice.actions;

// ── Selectors ─────────────────────────────────────────────────
export const selectCartItems      = (state: any) => state.cart.items;
export const selectCartIsOpen     = (state: any) => state.cart.isOpen;
export const selectCartTotalItems = (state: any) => state.cart.totalItems;
export const selectCartTotalPrice = (state: any) => state.cart.totalPrice;
export const selectCartDiscount   = (state: any) => state.cart.discount;
export const selectCartFinalPrice = (state: any) => state.cart.finalPrice;
export const selectCartItemCount  = (state: any) => state.cart.items.length;
export const selectIsInCart = (productId: string) => (state: any) =>
  state.cart.items.some((i: CartItem) => i.productId === productId);
export const selectCartItemQty = (productId: string) => (state: any) =>
  state.cart.items.find((i: CartItem) => i.productId === productId)?.quantity || 0;

export default cartSlice.reducer;
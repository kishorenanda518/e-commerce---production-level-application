// ─────────────────────────────────────────────────────────────
// src/redux/rootReducer.ts
//
// Add every new slice here as the app grows.
// ─────────────────────────────────────────────────────────────

import { combineReducers } from '@reduxjs/toolkit';
import authReducer    from './slices/authSlice';
import productReducer from './slices/productSlice';
import userReducer    from './slices/userSlice';

const rootReducer = combineReducers({
  auth:     authReducer,
  products: productReducer,
  users:    userReducer,
});

export type RootState = ReturnType<typeof rootReducer>;
export default rootReducer;
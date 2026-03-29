import { combineReducers } from '@reduxjs/toolkit';
import authReducer    from './slices/authSlice';
import productReducer from './slices/productSlice';
import userReducer    from './slices/userSlice';
import cartReducer    from './slices/cartSlice';
import orderReducer   from './slices/orderSlice';

const rootReducer = combineReducers({
  auth:     authReducer,
  products: productReducer,
  users:    userReducer,
  cart:     cartReducer,
  order:    orderReducer,
});

export type RootState = ReturnType<typeof rootReducer>;
export default rootReducer;
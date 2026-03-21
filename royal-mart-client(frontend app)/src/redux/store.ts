import { configureStore } from '@reduxjs/toolkit';
import rootReducer from './rootReducer';

// ── Load persisted auth state from localStorage ───────────────
const loadAuthState = () => {
  try {
    const serialized = localStorage.getItem('rm_auth');
    if (!serialized) return undefined;
    return { auth: JSON.parse(serialized) };
  } catch {
    return undefined;
  }
};

// ── Save auth state to localStorage on every change ───────────
const saveAuthState = (state: any) => {
  try {
    const authState = {
      user:           state.auth.user,
      activeRole:     state.auth.activeRole,
      showRolePicker: false,   // never persist picker open state
      loading:        false,
      error:          null,
      registerSuccess: false,
    };
    localStorage.setItem('rm_auth', JSON.stringify(authState));
  } catch {}
};

const store = configureStore({
  reducer: rootReducer,
  preloadedState: loadAuthState(),  // ← hydrate from localStorage on load
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({ serializableCheck: false }),
  devTools: process.env.NODE_ENV !== 'production',
});

// ── Subscribe to save auth on every state change ──────────────
store.subscribe(() => {
  saveAuthState(store.getState());
});

export type AppDispatch = typeof store.dispatch;
export type RootState = ReturnType<typeof store.getState>;
export default store;
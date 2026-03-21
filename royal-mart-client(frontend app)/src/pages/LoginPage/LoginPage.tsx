// ─────────────────────────────────────────────────────────────
// src/pages/LoginPage/LoginPage.tsx
// ─────────────────────────────────────────────────────────────

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../hooks/reduxHooks';
import {
  loginThunk,
  registerThunk,
  clearAuthError,
  clearRegisterSuccess,
  selectAuthLoading,
  selectAuthError,
  selectIsLoggedIn,
  selectShowRolePicker,
  selectActiveRole,
  selectRegisterSuccess,
} from '../../redux/slices/authSlice';
import type { LoginRequest, RegisterRequest } from '../../redux/types/auth.types';
import './LoginPage.css';

type Tab = 'login' | 'register';

const LoginPage: React.FC = () => {
  const dispatch    = useAppDispatch();
  const navigate    = useNavigate();
  const isLoggedIn     = useAppSelector(selectIsLoggedIn);
  const showRolePicker = useAppSelector(selectShowRolePicker);
  const activeRole     = useAppSelector(selectActiveRole);
  const loading        = useAppSelector(selectAuthLoading);
  const error          = useAppSelector(selectAuthError);
  const regSuccess     = useAppSelector(selectRegisterSuccess);

  const [tab, setTab]         = useState<Tab>('login');
  const [showPwd, setShowPwd] = useState(false);

  const [loginForm, setLoginForm]   = useState<LoginRequest>({ usernameOrEmail: '', password: '' });
  const [regForm,   setRegForm]     = useState<RegisterRequest>({
    firstName: '', lastName: '', username: '', email: '', password: '', phone: '',
  });

  // Redirect logic:
  // - if role picker is showing → stay on login page (RolePicker overlay appears)
  // - if logged in AND role selected → go to role-based page
  useEffect(() => {
    if (isLoggedIn && !showRolePicker && activeRole) {
      navigate('/', { replace: true });
    }
  }, [isLoggedIn, showRolePicker, activeRole, navigate]);

  // Clear error on tab switch
  useEffect(() => {
    dispatch(clearAuthError());
    dispatch(clearRegisterSuccess());
    setShowPwd(false);
  }, [tab, dispatch]);

  // After register success switch to login tab
  useEffect(() => {
    if (regSuccess) {
      setTimeout(() => {
        dispatch(clearRegisterSuccess());
        setTab('login');
      }, 2000);
    }
  }, [regSuccess, dispatch]);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    dispatch(loginThunk(loginForm));
  };

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    dispatch(registerThunk(regForm));
  };

  const setLogin  = (key: keyof LoginRequest,  val: string) => setLoginForm(f => ({ ...f, [key]: val }));
  const setReg    = (key: keyof RegisterRequest, val: string) => setRegForm(f => ({ ...f, [key]: val }));

  return (
    <div className="lp">

      {/* Left panel */}
      <aside className="lp__left">
        <div className="lp__left-inner">
          <div className="lp__brand">
            <div className="lp__crown">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="currentColor">
                <path d="M2 20L5 8L9 14L12 4L15 14L19 8L22 20H2Z"/>
                <circle cx="5"  cy="8" r="1.5"/>
                <circle cx="12" cy="4" r="1.5"/>
                <circle cx="19" cy="8" r="1.5"/>
              </svg>
            </div>
            <span className="lp__brand-name">ROYAL MART</span>
          </div>

          <div className="lp__left-body">
            <div className="lp__quote-mark">"</div>
            <blockquote className="lp__quote">
              Where quality meets royalty — curated collections for the discerning shopper.
            </blockquote>
            <div className="lp__founder">
              <div className="lp__founder-avatar">N</div>
              <div>
                <div className="lp__founder-name">Nanda Kishore</div>
                <div className="lp__founder-role">Founder & CEO</div>
              </div>
            </div>
          </div>

          <div className="lp__stats">
            {[['10K+','Products'],['500+','Brands'],['50K+','Customers']].map(([v,l]) => (
              <div key={l} className="lp__stat">
                <span className="lp__stat-val">{v}</span>
                <span className="lp__stat-label">{l}</span>
              </div>
            ))}
          </div>
        </div>
      </aside>

      {/* Right panel — form */}
      <main className="lp__right">
        <div className="lp__form-wrap">

          {/* Logo (mobile only) */}
          <div className="lp__mobile-brand">
            <div className="lp__crown lp__crown--dark">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M2 20L5 8L9 14L12 4L15 14L19 8L22 20H2Z"/>
                <circle cx="5" cy="8" r="1.5"/>
                <circle cx="12" cy="4" r="1.5"/>
                <circle cx="19" cy="8" r="1.5"/>
              </svg>
            </div>
            <span className="lp__brand-name lp__brand-name--dark">ROYAL MART</span>
          </div>

          {/* Tabs */}
          <div className="lp__tabs">
            <button className={`lp__tab ${tab === 'login' ? 'lp__tab--active' : ''}`}
              onClick={() => setTab('login')}>
              Sign In
            </button>
            <button className={`lp__tab ${tab === 'register' ? 'lp__tab--active' : ''}`}
              onClick={() => setTab('register')}>
              Create Account
            </button>
          </div>

          {/* Heading */}
          <div className="lp__heading">
            <h1 className="lp__title">
              {tab === 'login' ? 'Welcome Back' : 'Join Royal Mart'}
            </h1>
            <p className="lp__subtitle">
              {tab === 'login'
                ? 'Sign in to continue your royal shopping experience'
                : 'Create your account and start your royal journey'}
            </p>
          </div>

          {/* Error */}
          {error && (
            <div className="lp__alert lp__alert--error">
              <WarningIcon /> {error}
            </div>
          )}

          {/* Register success */}
          {regSuccess && (
            <div className="lp__alert lp__alert--success">
              <CheckIcon /> Account created! Please verify your email, then sign in.
            </div>
          )}

          {/* ── LOGIN FORM ───────────────────────────────── */}
          {tab === 'login' && (
            <form className="lp__form" onSubmit={handleLogin} noValidate>

              <div className="lp__field">
                <label className="lp__label">Username or Email</label>
                <div className="lp__input-wrap">
                  <span className="lp__input-icon"><UserIcon /></span>
                  <input
                    className="lp__input"
                    type="text"
                    placeholder="Enter your username"
                    required
                    autoFocus
                    value={loginForm.usernameOrEmail}
                    onChange={e => setLogin('usernameOrEmail', e.target.value)}
                  />
                </div>
              </div>

              <div className="lp__field">
                <label className="lp__label">Password</label>
                <div className="lp__input-wrap">
                  <span className="lp__input-icon"><LockIcon /></span>
                  <input
                    className="lp__input"
                    type={showPwd ? 'text' : 'password'}
                    placeholder="Enter your password"
                    required
                    value={loginForm.password}
                    onChange={e => setLogin('password', e.target.value)}
                  />
                  <button type="button" className="lp__eye" onClick={() => setShowPwd(v => !v)}>
                    {showPwd ? <EyeOffIcon /> : <EyeIcon />}
                  </button>
                </div>
              </div>

              <button type="submit" className="lp__submit" disabled={loading}>
                {loading ? <span className="lp__spinner" /> : null}
                {loading ? 'Signing in...' : 'Sign In'}
              </button>

              <p className="lp__switch">
                Don't have an account?{' '}
                <button type="button" className="lp__switch-btn" onClick={() => setTab('register')}>
                  Create one
                </button>
              </p>
            </form>
          )}

          {/* ── REGISTER FORM ────────────────────────────── */}
          {tab === 'register' && !regSuccess && (
            <form className="lp__form" onSubmit={handleRegister} noValidate>

              <div className="lp__row">
                <div className="lp__field">
                  <label className="lp__label">First Name</label>
                  <div className="lp__input-wrap">
                    <span className="lp__input-icon"><UserIcon /></span>
                    <input className="lp__input" required placeholder="Nanda"
                      value={regForm.firstName} onChange={e => setReg('firstName', e.target.value)} />
                  </div>
                </div>
                <div className="lp__field">
                  <label className="lp__label">Last Name</label>
                  <div className="lp__input-wrap">
                    <span className="lp__input-icon"><UserIcon /></span>
                    <input className="lp__input" required placeholder="Kishore"
                      value={regForm.lastName} onChange={e => setReg('lastName', e.target.value)} />
                  </div>
                </div>
              </div>

              <div className="lp__field">
                <label className="lp__label">Username</label>
                <div className="lp__input-wrap">
                  <span className="lp__input-icon"><AtIcon /></span>
                  <input className="lp__input" required placeholder="nandakishore"
                    value={regForm.username} onChange={e => setReg('username', e.target.value)} />
                </div>
              </div>

              <div className="lp__field">
                <label className="lp__label">Email</label>
                <div className="lp__input-wrap">
                  <span className="lp__input-icon"><MailIcon /></span>
                  <input className="lp__input" type="email" required placeholder="nanda@example.com"
                    value={regForm.email} onChange={e => setReg('email', e.target.value)} />
                </div>
              </div>

              <div className="lp__field">
                <label className="lp__label">Phone <span className="lp__optional">(optional)</span></label>
                <div className="lp__input-wrap">
                  <span className="lp__input-icon"><PhoneIcon /></span>
                  <input className="lp__input" type="tel" placeholder="9876543210"
                    value={regForm.phone} onChange={e => setReg('phone', e.target.value)} />
                </div>
              </div>

              <div className="lp__field">
                <label className="lp__label">Password</label>
                <div className="lp__input-wrap">
                  <span className="lp__input-icon"><LockIcon /></span>
                  <input className="lp__input" type={showPwd ? 'text' : 'password'}
                    required minLength={8} placeholder="Min. 8 characters"
                    value={regForm.password} onChange={e => setReg('password', e.target.value)} />
                  <button type="button" className="lp__eye" onClick={() => setShowPwd(v => !v)}>
                    {showPwd ? <EyeOffIcon /> : <EyeIcon />}
                  </button>
                </div>
              </div>

              <button type="submit" className="lp__submit" disabled={loading}>
                {loading ? <span className="lp__spinner" /> : null}
                {loading ? 'Creating Account...' : 'Create Account'}
              </button>

              <p className="lp__switch">
                Already have an account?{' '}
                <button type="button" className="lp__switch-btn" onClick={() => setTab('login')}>
                  Sign in
                </button>
              </p>
            </form>
          )}

        </div>
      </main>
    </div>
  );
};

export default LoginPage;

// ── Icons ──────────────────────────────────────────────────────
const UserIcon    = () => <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>;
const LockIcon    = () => <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>;
const MailIcon    = () => <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>;
const AtIcon      = () => <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="4"/><path d="M16 8v5a3 3 0 006 0v-1a10 10 0 10-3.92 7.94"/></svg>;
const PhoneIcon   = () => <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07A19.5 19.5 0 013.07 9.81a19.79 19.79 0 01-3.07-8.67A2 2 0 012 1h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L6.91 8.91a16 16 0 006.18 6.18l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>;
const EyeIcon     = () => <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>;
const EyeOffIcon  = () => <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>;
const CheckIcon   = () => <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><polyline points="20 6 9 17 4 12"/></svg>;
const WarningIcon = () => <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>;

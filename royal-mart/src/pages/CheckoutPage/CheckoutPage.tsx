// src/pages/CheckoutPage/CheckoutPage.tsx

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../hooks/reduxHooks';
import { selectUser }          from '../../redux/slices/authSlice';
import {
  selectCartItems, selectCartFinalPrice,
  selectCartDiscount, selectCartTotalPrice,
  clearCart,
} from '../../redux/slices/cartSlice';
import {
  placeOrderThunk,
  clearOrderSuccess, clearOrderError,
  selectOrderPlacing, selectOrderError,
  selectOrderSuccess, selectCurrentOrder,
} from '../../redux/slices/orderSlice';
import { CreateOrderRequest, AddressRequest } from '../../redux/types/order.types';
import './CheckoutPage.css';

const fmt = (n: number) =>
  `₹${Number(n).toLocaleString('en-IN', { minimumFractionDigits: 0 })}`;

const INDIAN_STATES = [
  'Andhra Pradesh','Arunachal Pradesh','Assam','Bihar','Chhattisgarh',
  'Goa','Gujarat','Haryana','Himachal Pradesh','Jharkhand','Karnataka',
  'Kerala','Madhya Pradesh','Maharashtra','Manipur','Meghalaya','Mizoram',
  'Nagaland','Odisha','Punjab','Rajasthan','Sikkim','Tamil Nadu','Telangana',
  'Tripura','Uttar Pradesh','Uttarakhand','West Bengal',
  'Delhi','Jammu & Kashmir','Ladakh','Puducherry',
];

const EMPTY_ADDRESS: AddressRequest = {
  fullName: '', phone: '', addressLine1: '', addressLine2: '',
  city: '', state: '', pincode: '', country: 'India',
};

const CheckoutPage: React.FC = () => {
  const dispatch      = useAppDispatch();
  const navigate      = useNavigate();
  const user          = useAppSelector(selectUser);
  const cartItems     = useAppSelector(selectCartItems);
  const totalPrice    = useAppSelector(selectCartTotalPrice);
  const discount      = useAppSelector(selectCartDiscount);
  const finalPrice    = useAppSelector(selectCartFinalPrice);
  const placing       = useAppSelector(selectOrderPlacing);
  const error         = useAppSelector(selectOrderError);
  const orderSuccess  = useAppSelector(selectOrderSuccess);
  const currentOrder  = useAppSelector(selectCurrentOrder);

  const shippingCharge = finalPrice >= 999 ? 0 : 99;
  const grandTotal     = finalPrice + shippingCharge;
  const tax            = Math.round(grandTotal * 0.18);

  const [address, setAddress]       = useState<AddressRequest>(EMPTY_ADDRESS);
  const [paymentMethod, setPayment] = useState<'COD' | 'ONLINE'>('COD');
  const [notes, setNotes]           = useState('');
  const [errors, setErrors]         = useState<Partial<AddressRequest>>({});

  // redirect if cart is empty
  useEffect(() => {
    if (cartItems.length === 0 && !orderSuccess) {
      navigate('/dashboard', { replace: true });
    }
  }, [cartItems, orderSuccess, navigate]);

  // on order success
  useEffect(() => {
    if (orderSuccess && currentOrder) {
      dispatch(clearCart());
    }
  }, [orderSuccess, currentOrder, dispatch]);

  const setField = (key: keyof AddressRequest, value: string) => {
    setAddress(a => ({ ...a, [key]: value }));
    if (errors[key]) setErrors(e => ({ ...e, [key]: '' }));
  };

  const validate = (): boolean => {
    const e: Partial<AddressRequest> = {};
    if (!address.fullName.trim())     e.fullName     = 'Full name is required';
    if (!address.phone.trim())        e.phone        = 'Phone number is required';
    if (!/^\d{10}$/.test(address.phone.trim())) e.phone = 'Enter valid 10-digit phone';
    if (!address.addressLine1.trim()) e.addressLine1 = 'Address is required';
    if (!address.city.trim())         e.city         = 'City is required';
    if (!address.state)               e.state        = 'State is required';
    if (!address.pincode.trim())      e.pincode      = 'Pincode is required';
    if (!/^\d{6}$/.test(address.pincode.trim())) e.pincode = 'Enter valid 6-digit pincode';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handlePlaceOrder = async () => {
    if (!validate()) return;
    dispatch(clearOrderError());

    const payload: CreateOrderRequest = {
      items: cartItems.map((item: any) => ({
        productId: item.productId,
        quantity:  item.quantity,
      })),
      shippingAddress: address,
      paymentMethod,
      notes,
    };

    dispatch(placeOrderThunk(payload));
  };

  // ── Success screen ─────────────────────────────────────────
  if (orderSuccess && currentOrder) {
    return (
      <div className="cp__success">
        <div className="cp__success-card">
          <div className="cp__success-icon">✓</div>
          <h1 className="cp__success-title">Order Placed!</h1>
          <p className="cp__success-sub">
            Your order has been placed successfully.
          </p>
          <div className="cp__success-info">
            <div className="cp__success-row">
              <span>Order Number</span>
              <strong>#{currentOrder.orderNumber}</strong>
            </div>
            <div className="cp__success-row">
              <span>Total Amount</span>
              <strong>{fmt(currentOrder.totalAmount)}</strong>
            </div>
            <div className="cp__success-row">
              <span>Payment</span>
              <strong>{currentOrder.paymentMethod}</strong>
            </div>
            <div className="cp__success-row">
              <span>Status</span>
              <span className="cp__status-badge cp__status-badge--pending">
                {currentOrder.status}
              </span>
            </div>
          </div>
          <div className="cp__success-actions">
            <button className="cp__btn cp__btn--gold"
              onClick={() => { dispatch(clearOrderSuccess()); navigate('/orders'); }}>
              View My Orders
            </button>
            <button className="cp__btn cp__btn--outline"
              onClick={() => { dispatch(clearOrderSuccess()); navigate('/dashboard'); }}>
              Continue Shopping
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="cp">

      {/* Header */}
      <header className="cp__header">
        <div className="cp__header-inner">
          <div className="cp__logo" onClick={() => navigate('/dashboard')}>
            <span className="cp__crown">♛</span>
            <span className="cp__logo-name">ROYAL MART</span>
          </div>
          <div className="cp__steps">
            <span className="cp__step cp__step--active">1. Cart</span>
            <span className="cp__step-sep">→</span>
            <span className="cp__step cp__step--active">2. Checkout</span>
            <span className="cp__step-sep">→</span>
            <span className="cp__step">3. Confirmation</span>
          </div>
        </div>
      </header>

      <div className="cp__body">

        {/* ── LEFT: Address + Payment ── */}
        <div className="cp__left">

          {/* Delivery Address */}
          <div className="cp__section">
            <h2 className="cp__section-title">
              <span className="cp__section-num">1</span>
              Delivery Address
            </h2>

            <div className="cp__form">
              <div className="cp__row">
                <Field label="Full Name *" error={errors.fullName}>
                  <input className={`cp__input ${errors.fullName ? 'cp__input--error' : ''}`}
                    placeholder="Nanda Kishore"
                    value={address.fullName}
                    onChange={e => setField('fullName', e.target.value)} />
                </Field>
                <Field label="Phone Number *" error={errors.phone}>
                  <input className={`cp__input ${errors.phone ? 'cp__input--error' : ''}`}
                    placeholder="9876543210" maxLength={10}
                    value={address.phone}
                    onChange={e => setField('phone', e.target.value.replace(/\D/,''))} />
                </Field>
              </div>

              <Field label="Address Line 1 *" error={errors.addressLine1}>
                <input className={`cp__input ${errors.addressLine1 ? 'cp__input--error' : ''}`}
                  placeholder="House No, Street, Area"
                  value={address.addressLine1}
                  onChange={e => setField('addressLine1', e.target.value)} />
              </Field>

              <Field label="Address Line 2">
                <input className="cp__input"
                  placeholder="Landmark, Colony (optional)"
                  value={address.addressLine2}
                  onChange={e => setField('addressLine2', e.target.value)} />
              </Field>

              <div className="cp__row">
                <Field label="City *" error={errors.city}>
                  <input className={`cp__input ${errors.city ? 'cp__input--error' : ''}`}
                    placeholder="Hyderabad"
                    value={address.city}
                    onChange={e => setField('city', e.target.value)} />
                </Field>
                <Field label="Pincode *" error={errors.pincode}>
                  <input className={`cp__input ${errors.pincode ? 'cp__input--error' : ''}`}
                    placeholder="500001" maxLength={6}
                    value={address.pincode}
                    onChange={e => setField('pincode', e.target.value.replace(/\D/,''))} />
                </Field>
              </div>

              <div className="cp__row">
                <Field label="State *" error={errors.state}>
                  <select className={`cp__input cp__select ${errors.state ? 'cp__input--error' : ''}`}
                    value={address.state}
                    onChange={e => setField('state', e.target.value)}>
                    <option value="">Select State</option>
                    {INDIAN_STATES.map(s => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                </Field>
                <Field label="Country">
                  <input className="cp__input" value="India" disabled />
                </Field>
              </div>

              <Field label="Order Notes (optional)">
                <textarea className="cp__input cp__textarea"
                  placeholder="Any special instructions for delivery..."
                  value={notes} rows={2}
                  onChange={e => setNotes(e.target.value)} />
              </Field>
            </div>
          </div>

          {/* Payment Method */}
          <div className="cp__section">
            <h2 className="cp__section-title">
              <span className="cp__section-num">2</span>
              Payment Method
            </h2>

            <div className="cp__payment-options">
              <label className={`cp__payment-opt ${paymentMethod === 'COD' ? 'cp__payment-opt--active' : ''}`}>
                <input type="radio" name="payment" value="COD"
                  checked={paymentMethod === 'COD'}
                  onChange={() => setPayment('COD')} />
                <div className="cp__payment-icon">💵</div>
                <div>
                  <div className="cp__payment-label">Cash on Delivery</div>
                  <div className="cp__payment-sub">Pay when you receive your order</div>
                </div>
              </label>

              <label className={`cp__payment-opt ${paymentMethod === 'ONLINE' ? 'cp__payment-opt--active' : ''}`}>
                <input type="radio" name="payment" value="ONLINE"
                  checked={paymentMethod === 'ONLINE'}
                  onChange={() => setPayment('ONLINE')} />
                <div className="cp__payment-icon">💳</div>
                <div>
                  <div className="cp__payment-label">Online Payment</div>
                  <div className="cp__payment-sub">UPI, Cards, Net Banking</div>
                </div>
              </label>
            </div>
          </div>
        </div>

        {/* ── RIGHT: Order Summary ── */}
        <div className="cp__right">
          <div className="cp__summary">
            <h2 className="cp__summary-title">Order Summary</h2>

            {/* Items */}
            <div className="cp__summary-items">
              {cartItems.map((item: any) => (
                <div key={item.productId} className="cp__summary-item">
                  <div className="cp__summary-img">
                    {item.imageUrl
                      ? <img src={item.imageUrl} alt={item.name} />
                      : <span>🛍️</span>}
                  </div>
                  <div className="cp__summary-item-details">
                    <div className="cp__summary-item-name">{item.name}</div>
                    <div className="cp__summary-item-brand">{item.brandName}</div>
                    <div className="cp__summary-item-qty">Qty: {item.quantity}</div>
                  </div>
                  <div className="cp__summary-item-price">
                    {fmt(item.price * item.quantity)}
                  </div>
                </div>
              ))}
            </div>

            {/* Totals */}
            <div className="cp__summary-totals">
              <div className="cp__total-row">
                <span>Subtotal ({cartItems.length} items)</span>
                <span>{fmt(totalPrice)}</span>
              </div>
              {discount > 0 && (
                <div className="cp__total-row cp__total-row--green">
                  <span>Discount</span>
                  <span>− {fmt(discount)}</span>
                </div>
              )}
              <div className="cp__total-row">
                <span>Shipping</span>
                <span className={shippingCharge === 0 ? 'cp__free' : ''}>
                  {shippingCharge === 0 ? 'FREE' : fmt(shippingCharge)}
                </span>
              </div>
              <div className="cp__total-row">
                <span>Tax (18% GST)</span>
                <span>{fmt(tax)}</span>
              </div>
              <div className="cp__total-row cp__total-row--total">
                <span>Grand Total</span>
                <span>{fmt(grandTotal + tax)}</span>
              </div>
            </div>

            {/* Error */}
            {error && (
              <div className="cp__error">
                ⚠ {error}
              </div>
            )}

            {/* Place order button */}
            <button
              className="cp__place-btn"
              onClick={handlePlaceOrder}
              disabled={placing || cartItems.length === 0}
            >
              {placing ? (
                <><span className="cp__spinner" /> Placing Order...</>
              ) : (
                `Place Order • ${fmt(grandTotal + tax)}`
              )}
            </button>

            <p className="cp__secure-msg">
              🔒 Secure checkout. Your data is protected.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

// ── Field wrapper ─────────────────────────────────────────────
const Field: React.FC<{
  label: string;
  error?: string;
  children: React.ReactNode;
}> = ({ label, error, children }) => (
  <div className="cp__field">
    <label className="cp__label">{label}</label>
    {children}
    {error && <span className="cp__field-error">{error}</span>}
  </div>
);

export default CheckoutPage;

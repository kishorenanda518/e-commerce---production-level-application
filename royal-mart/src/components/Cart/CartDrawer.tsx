// src/components/Cart/CartDrawer.tsx

import React from 'react';
import { useAppDispatch, useAppSelector } from '../../hooks/reduxHooks';
import { useNavigate } from 'react-router-dom';
import {
  selectCartItems,
  selectCartIsOpen,
  selectCartTotalItems,
  selectCartTotalPrice,
  selectCartDiscount,
  selectCartFinalPrice,
  closeCart,
  removeFromCart,
  updateQuantity,
  clearCart,
} from '../../redux/slices/cartSlice';
import { CartItem } from '../../redux/types/cart.types';
import './CartDrawer.css';

const fmt = (n: number) => `₹${Number(n).toLocaleString('en-IN', { minimumFractionDigits: 0 })}`;

const CartDrawer: React.FC = () => {
  const dispatch    = useAppDispatch();
  const navigate    = useNavigate();
  const isOpen      = useAppSelector(selectCartIsOpen);
  const items       = useAppSelector(selectCartItems);
  const totalItems  = useAppSelector(selectCartTotalItems);
  const totalPrice  = useAppSelector(selectCartTotalPrice);
  const discount    = useAppSelector(selectCartDiscount);
  const finalPrice  = useAppSelector(selectCartFinalPrice);

  const handleCheckout = () => {
    dispatch(closeCart());
    navigate('/checkout');
  };

  const handleClose = () => dispatch(closeCart());

  return (
    <>
      {/* Backdrop */}
      <div
        className={`cd__backdrop ${isOpen ? 'cd__backdrop--open' : ''}`}
        onClick={handleClose}
      />

      {/* Drawer */}
      <div className={`cd ${isOpen ? 'cd--open' : ''}`}>

        {/* Header */}
        <div className="cd__header">
          <div className="cd__header-left">
            <span className="cd__crown">♛</span>
            <h2 className="cd__title">Your Cart</h2>
            {totalItems > 0 && (
              <span className="cd__count">{totalItems} item{totalItems > 1 ? 's' : ''}</span>
            )}
          </div>
          <div className="cd__header-right">
            {items.length > 0 && (
              <button className="cd__clear-btn" onClick={() => dispatch(clearCart())}>
                Clear All
              </button>
            )}
            <button className="cd__close-btn" onClick={handleClose}>✕</button>
          </div>
        </div>

        {/* Body */}
        <div className="cd__body">
          {items.length === 0 ? (
            <div className="cd__empty">
              <div className="cd__empty-icon">🛒</div>
              <h3>Your cart is empty</h3>
              <p>Add items from the shop to get started</p>
              <button className="cd__shop-btn" onClick={handleClose}>
                Continue Shopping
              </button>
            </div>
          ) : (
            <div className="cd__items">
              {items.map((item: CartItem) => (
                <CartItemRow
                  key={item.productId}
                  item={item}
                  onRemove={() => dispatch(removeFromCart(item.productId))}
                  onQtyChange={(qty) =>
                    dispatch(updateQuantity({ productId: item.productId, quantity: qty }))
                  }
                />
              ))}
            </div>
          )}
        </div>

        {/* Footer — only show when items exist */}
        {items.length > 0 && (
          <div className="cd__footer">
            <div className="cd__summary">
              <div className="cd__summary-row">
                <span>Subtotal</span>
                <span>{fmt(totalPrice)}</span>
              </div>
              {discount > 0 && (
                <div className="cd__summary-row cd__summary-row--discount">
                  <span>You Save</span>
                  <span>− {fmt(discount)}</span>
                </div>
              )}
              <div className="cd__summary-row">
                <span>Shipping</span>
                <span className={finalPrice >= 999 ? 'cd__free' : ''}>
                  {finalPrice >= 999 ? 'FREE' : fmt(99)}
                </span>
              </div>
              <div className="cd__summary-row cd__summary-row--total">
                <span>Total</span>
                <span>{fmt(finalPrice >= 999 ? finalPrice : finalPrice + 99)}</span>
              </div>
              {finalPrice < 999 && (
                <div className="cd__free-msg">
                  Add {fmt(999 - finalPrice)} more for FREE shipping
                </div>
              )}
            </div>

            <button className="cd__checkout-btn" onClick={handleCheckout}>
              Proceed to Checkout →
            </button>
            <button className="cd__continue-btn" onClick={handleClose}>
              Continue Shopping
            </button>
          </div>
        )}
      </div>
    </>
  );
};

// ── Cart Item Row ─────────────────────────────────────────────
const CartItemRow: React.FC<{
  item: CartItem;
  onRemove: () => void;
  onQtyChange: (qty: number) => void;
}> = ({ item, onRemove, onQtyChange }) => {
  const fmt = (n: number) =>
    `₹${Number(n).toLocaleString('en-IN', { minimumFractionDigits: 0 })}`;
  const discount = item.compareAtPrice > item.price
    ? Math.round((1 - item.price / item.compareAtPrice) * 100)
    : 0;

  return (
    <div className="cd__item">
      {/* Image */}
      <div className="cd__item-img">
        {item.imageUrl ? (
          <img src={item.imageUrl} alt={item.name} loading="lazy" />
        ) : (
          <span className="cd__item-img-placeholder">🛍️</span>
        )}
      </div>

      {/* Details */}
      <div className="cd__item-details">
        <span className="cd__item-brand">{item.brandName}</span>
        <h4 className="cd__item-name">{item.name}</h4>

        <div className="cd__item-prices">
          <span className="cd__item-price">{fmt(item.price)}</span>
          {discount > 0 && (
            <>
              <span className="cd__item-compare">{fmt(item.compareAtPrice)}</span>
              <span className="cd__item-disc">{discount}% off</span>
            </>
          )}
        </div>

        {/* Qty controls */}
        <div className="cd__item-controls">
          <div className="cd__qty">
            <button
              className="cd__qty-btn"
              onClick={() => onQtyChange(item.quantity - 1)}
            >−</button>
            <span className="cd__qty-val">{item.quantity}</span>
            <button
              className="cd__qty-btn"
              onClick={() => onQtyChange(item.quantity + 1)}
              disabled={item.quantity >= item.maxStock}
            >+</button>
          </div>
          <span className="cd__item-subtotal">
            {fmt(item.price * item.quantity)}
          </span>
          <button className="cd__item-remove" onClick={onRemove} title="Remove">
            🗑
          </button>
        </div>
      </div>
    </div>
  );
};

export default CartDrawer;

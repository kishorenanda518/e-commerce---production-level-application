// src/pages/OrdersPage/OrdersPage.tsx

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../hooks/reduxHooks';
import {
  getMyOrdersThunk, cancelOrderThunk,
  getOrderHistoryThunk, clearOrderError,
  selectOrders, selectOrderLoading, selectOrderError,
  selectOrderPagination, selectOrderHistory,
} from '../../redux/slices/orderSlice';
import { Order, OrderStatusHistory } from '../../redux/types/order.types';
import './OrdersPage.css';

const fmt = (n: number) =>
  `₹${Number(n).toLocaleString('en-IN', { minimumFractionDigits: 0 })}`;

const STATUS_META: Record<string, { label: string; cls: string; icon: string }> = {
  PENDING:    { label: 'Pending',    cls: 'op__s--pending',    icon: '⏳' },
  CONFIRMED:  { label: 'Confirmed',  cls: 'op__s--confirmed',  icon: '✅' },
  PROCESSING: { label: 'Processing', cls: 'op__s--processing', icon: '⚙️' },
  SHIPPED:    { label: 'Shipped',    cls: 'op__s--shipped',    icon: '🚚' },
  DELIVERED:  { label: 'Delivered',  cls: 'op__s--delivered',  icon: '📦' },
  CANCELLED:  { label: 'Cancelled',  cls: 'op__s--cancelled',  icon: '✕' },
  REFUNDED:   { label: 'Refunded',   cls: 'op__s--refunded',   icon: '↩' },
};

const OrdersPage: React.FC = () => {
  const dispatch    = useAppDispatch();
  const navigate    = useNavigate();
  const orders      = useAppSelector(selectOrders);
  const loading     = useAppSelector(selectOrderLoading);
  const error       = useAppSelector(selectOrderError);
  const pagination  = useAppSelector(selectOrderPagination);
  const history     = useAppSelector(selectOrderHistory);

  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [showHistory,   setShowHistory]   = useState(false);
  const [cancelId,      setCancelId]      = useState<string | null>(null);
  const [cancelReason,  setCancelReason]  = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    dispatch(getMyOrdersThunk({ page, size: 10 }));
  }, [dispatch, page]);

  const handleViewHistory = (order: Order) => {
    setSelectedOrder(order);
    setShowHistory(true);
    dispatch(getOrderHistoryThunk(order.id));
  };

  const handleCancelConfirm = async () => {
    if (!cancelId) return;
    await dispatch(cancelOrderThunk({ orderId: cancelId, reason: cancelReason }));
    setCancelId(null);
    setCancelReason('');
    dispatch(getMyOrdersThunk({ page, size: 10 }));
  };

  return (
    <div className="op">

      {/* Header */}
      <header className="op__header">
        <div className="op__header-inner">
          <div className="op__logo" onClick={() => navigate('/dashboard')}>
            <span>♛</span>
            <span className="op__logo-name">ROYAL MART</span>
          </div>
          <h1 className="op__title">My Orders</h1>
          <button className="op__back-btn" onClick={() => navigate('/dashboard')}>
            ← Continue Shopping
          </button>
        </div>
      </header>

      <div className="op__body">

        {error && (
          <div className="op__error">
            ⚠ {error}
            <button onClick={() => dispatch(clearOrderError())}>✕</button>
          </div>
        )}

        {loading ? (
          <div className="op__loading">
            <div className="op__spinner" />
            <span>Loading your orders...</span>
          </div>
        ) : orders.length === 0 ? (
          <div className="op__empty">
            <div className="op__empty-icon">📦</div>
            <h2>No orders yet</h2>
            <p>When you place an order it will appear here</p>
            <button className="op__shop-btn" onClick={() => navigate('/dashboard')}>
              Start Shopping
            </button>
          </div>
        ) : (
          <>
            <div className="op__orders">
              {orders.map((order: Order) => (
                <OrderCard
                  key={order.id}
                  order={order}
                  onViewHistory={() => handleViewHistory(order)}
                  onCancel={() => setCancelId(order.id)}
                />
              ))}
            </div>

            {/* Pagination */}
            {pagination.totalPages > 1 && (
              <div className="op__pagination">
                <button className="op__page-btn"
                  disabled={page === 0}
                  onClick={() => setPage(p => p - 1)}>← Prev</button>
                <span className="op__page-info">
                  Page {page + 1} of {pagination.totalPages}
                </span>
                <button className="op__page-btn"
                  disabled={page === pagination.totalPages - 1}
                  onClick={() => setPage(p => p + 1)}>Next →</button>
              </div>
            )}
          </>
        )}
      </div>

      {/* ── Order History Modal ─────────────────────────────── */}
      {showHistory && selectedOrder && (
        <div className="op__modal-overlay" onClick={() => setShowHistory(false)}>
          <div className="op__modal" onClick={e => e.stopPropagation()}>
            <div className="op__modal-header">
              <h3>Order #{selectedOrder.orderNumber} — History</h3>
              <button className="op__modal-close" onClick={() => setShowHistory(false)}>✕</button>
            </div>
            <div className="op__modal-body">
              {history.length === 0 ? (
                <p className="op__modal-empty">No history available</p>
              ) : (
                <div className="op__timeline">
                  {history.map((h: OrderStatusHistory, i: number) => {
                    const meta = STATUS_META[h.status] || { label: h.status, cls: '', icon: '●' };
                    return (
                      <div key={h.id} className={`op__timeline-item ${i === 0 ? 'op__timeline-item--latest' : ''}`}>
                        <div className={`op__timeline-dot ${meta.cls}`}>{meta.icon}</div>
                        <div className="op__timeline-content">
                          <div className="op__timeline-status">{meta.label}</div>
                          {h.comment && <div className="op__timeline-comment">{h.comment}</div>}
                          <div className="op__timeline-time">
                            {new Date(h.createdAt).toLocaleString('en-IN')}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* ── Cancel Confirm Modal ────────────────────────────── */}
      {cancelId && (
        <div className="op__modal-overlay" onClick={() => setCancelId(null)}>
          <div className="op__modal op__modal--sm" onClick={e => e.stopPropagation()}>
            <div className="op__modal-header">
              <h3>Cancel Order</h3>
              <button className="op__modal-close" onClick={() => setCancelId(null)}>✕</button>
            </div>
            <div className="op__modal-body">
              <p className="op__cancel-msg">
                Are you sure you want to cancel this order?
              </p>
              <label className="op__cancel-label">Reason (optional)</label>
              <textarea
                className="op__cancel-input"
                placeholder="Why are you cancelling?"
                rows={3}
                value={cancelReason}
                onChange={e => setCancelReason(e.target.value)}
              />
              <div className="op__cancel-actions">
                <button className="op__cancel-confirm" onClick={handleCancelConfirm}>
                  Yes, Cancel Order
                </button>
                <button className="op__cancel-abort" onClick={() => setCancelId(null)}>
                  Keep Order
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// ── Order Card ────────────────────────────────────────────────
const OrderCard: React.FC<{
  order: Order;
  onViewHistory: () => void;
  onCancel: () => void;
}> = ({ order, onViewHistory, onCancel }) => {
  const meta = STATUS_META[order.status] || { label: order.status, cls: '', icon: '●' };
  const canCancel = ['PENDING', 'CONFIRMED'].includes(order.status);

  return (
    <div className="op__card">
      {/* Card header */}
      <div className="op__card-header">
        <div className="op__card-header-left">
          <div className="op__order-num">Order #{order.orderNumber}</div>
          <div className="op__order-date">
            {new Date(order.createdAt).toLocaleDateString('en-IN', {
              day: 'numeric', month: 'long', year: 'numeric'
            })}
          </div>
        </div>
        <div className="op__card-header-right">
          <span className={`op__status ${meta.cls}`}>
            {meta.icon} {meta.label}
          </span>
          <div className="op__order-total">{fmt(order.totalAmount)}</div>
        </div>
      </div>

      {/* Items */}
      <div className="op__card-items">
        {(order.items || []).slice(0, 3).map((item: any) => (
          <div key={item.id} className="op__item">
            <div className="op__item-img">
              {item.imageUrl
                ? <img src={item.imageUrl} alt={item.productName} />
                : <span>📦</span>}
            </div>
            <div className="op__item-details">
              <div className="op__item-name">{item.productName}</div>
              <div className="op__item-meta">
                Qty: {item.quantity} × {fmt(item.unitPrice)}
              </div>
            </div>
            <div className="op__item-total">{fmt(item.totalPrice)}</div>
          </div>
        ))}
        {order.items?.length > 3 && (
          <div className="op__more-items">
            +{order.items.length - 3} more item{order.items.length - 3 > 1 ? 's' : ''}
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="op__card-footer">
        <div className="op__card-footer-left">
          <span className="op__payment-badge">
            {order.paymentMethod === 'COD' ? '💵 COD' : '💳 Online'}
          </span>
          <span className={`op__pay-status ${order.paymentStatus === 'PAID' ? 'op__pay-status--paid' : ''}`}>
            {order.paymentStatus}
          </span>
        </div>
        <div className="op__card-footer-right">
          <button className="op__action-btn op__action-btn--history" onClick={onViewHistory}>
            Track Order
          </button>
          {canCancel && (
            <button className="op__action-btn op__action-btn--cancel" onClick={onCancel}>
              Cancel
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default OrdersPage;

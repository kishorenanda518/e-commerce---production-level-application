// src/pages/AdminDashboard/AdminDashboard.tsx

import React, { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '../../hooks/reduxHooks';
import { selectUser, logoutThunk }  from '../../redux/slices/authSlice';
import {
  loadUsersThunk,
  updateUserStatusThunk,
  selectUsers,
  selectUsersLoading,
  selectUsersPagination,
} from '../../redux/slices/userSlice';
import {
  loadProductsThunk,
  loadCategoriesThunk,
  setFilter,
  selectProducts,
  selectCategories,
  selectProductsLoading,
  selectPagination,
  selectFilters,
} from '../../redux/slices/productSlice';
import { useNavigate } from 'react-router-dom';
import './AdminDashboard.css';

type AdminTab = 'products' | 'users';

const fmt = (p: number) => `₹${Number(p).toLocaleString('en-IN')}`;

const STATUS_META: Record<string, { label: string; cls: string }> = {
  ACTIVE:               { label: 'Active',               cls: 'status--active'   },
  SUSPENDED:            { label: 'Suspended',             cls: 'status--suspended'},
  PENDING_VERIFICATION: { label: 'Pending Verification',  cls: 'status--pending'  },
};

const AdminDashboard: React.FC = () => {
  const dispatch   = useAppDispatch();
  const navigate   = useNavigate();
  const user       = useAppSelector(selectUser);
  const [activeTab, setActiveTab] = useState<AdminTab>('products');

  // product state
  const products   = useAppSelector(selectProducts);
  const categories = useAppSelector(selectCategories);
  const prodLoading= useAppSelector(selectProductsLoading);
  const pagination = useAppSelector(selectPagination);
  const filters    = useAppSelector(selectFilters);

  // user state
  const users      = useAppSelector(selectUsers);
  const usersLoading = useAppSelector(selectUsersLoading);
  const usersPagination = useAppSelector(selectUsersPagination);
  const [userPage, setUserPage] = useState(0);

  // load products + categories on mount
  useEffect(() => {
    dispatch(loadCategoriesThunk());
    dispatch(loadProductsThunk({ ...filters, page: 0, size: 12, sort: 'newest' }));
  }, [dispatch]);

  // load users when tab switches to users
  useEffect(() => {
    if (activeTab === 'users') {
      dispatch(loadUsersThunk({ page: userPage, size: 10, token: '' }));
    }
  }, [activeTab, userPage, dispatch]);

  const handleLogout = async () => {
    await dispatch(logoutThunk());
    navigate('/login', { replace: true });
  };

  const handleCategoryFilter = (catId: string) => {
    dispatch(setFilter({ key: 'categoryId', value: catId }));
    dispatch(loadProductsThunk({ ...filters, categoryId: catId, page: 0 }));
  };

  const handleProductPage = (p: number) => {
    dispatch(loadProductsThunk({ ...filters, page: p }));
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleToggleUserStatus = (userId: string, currentStatus: string) => {
    const newStatus = currentStatus === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    dispatch(updateUserStatusThunk({ userId, status: newStatus, token: '' }));
  };

  const topCategories = categories.filter((c: any) => !c.parentId);

  return (
    <div className="ad">

      {/* ── NAVBAR ──────────────────────────────────────────── */}
      <nav className="ad__nav">
        <div className="ad__nav-inner">
          <div className="ad__logo">
            <div className="ad__logo-icon">♛</div>
            <div>
              <div className="ad__logo-name">ROYAL MART</div>
              <div className="ad__logo-tag">Admin Panel</div>
            </div>
          </div>

          <div className="ad__nav-tabs">
            <button
              className={`ad__nav-tab ${activeTab === 'products' ? 'ad__nav-tab--active' : ''}`}
              onClick={() => setActiveTab('products')}
            >📦 Products</button>
            <button
              className={`ad__nav-tab ${activeTab === 'users' ? 'ad__nav-tab--active' : ''}`}
              onClick={() => setActiveTab('users')}
            >👥 Users</button>
          </div>

          <div className="ad__nav-right">
            <div className="ad__admin-badge">ADMIN</div>
            <div className="ad__user-info">
              <div className="ad__user-avatar">{user?.firstName?.[0]}{user?.lastName?.[0]}</div>
              <span className="ad__user-name">{user?.firstName}</span>
            </div>
            <button className="ad__logout-btn" onClick={handleLogout}>Sign Out</button>
          </div>
        </div>
      </nav>

      <div className="ad__body">

        {/* ════════════════════════════════════════════════════
            PRODUCTS TAB
        ════════════════════════════════════════════════════ */}
        {activeTab === 'products' && (
          <div className="ad__content">

            {/* Stats bar */}
            <div className="ad__stats">
              <div className="ad__stat-card">
                <div className="ad__stat-val">{pagination.totalElements}</div>
                <div className="ad__stat-label">Total Products</div>
              </div>
              <div className="ad__stat-card">
                <div className="ad__stat-val">{topCategories.length}</div>
                <div className="ad__stat-label">Categories</div>
              </div>
              <div className="ad__stat-card">
                <div className="ad__stat-val">{products.filter((p: any) => p.inStock !== false).length}</div>
                <div className="ad__stat-label">In Stock</div>
              </div>
              <div className="ad__stat-card ad__stat-card--warning">
                <div className="ad__stat-val">{products.filter((p: any) => p.inStock === false).length}</div>
                <div className="ad__stat-label">Out of Stock</div>
              </div>
            </div>

            {/* Category filter pills */}
            <div className="ad__filter-row">
              <button
                className={`ad__filter-pill ${!filters.categoryId ? 'ad__filter-pill--active' : ''}`}
                onClick={() => { dispatch(setFilter({ key: 'categoryId', value: '' })); dispatch(loadProductsThunk({ ...filters, categoryId: '', page: 0 })); }}
              >All</button>
              {topCategories.map((cat: any) => (
                <button
                  key={cat.id}
                  className={`ad__filter-pill ${filters.categoryId === cat.id ? 'ad__filter-pill--active' : ''}`}
                  onClick={() => handleCategoryFilter(cat.id)}
                >{cat.name}</button>
              ))}
            </div>

            {/* Products Table */}
            <div className="ad__table-wrap">
              <div className="ad__table-header">
                <h2 className="ad__section-title">Products</h2>
                <span className="ad__total">{pagination.totalElements} total</span>
              </div>

              {prodLoading ? (
                <div className="ad__loading">
                  <div className="ad__spinner" />
                  <span>Loading products...</span>
                </div>
              ) : (
                <table className="ad__table">
                  <thead>
                    <tr>
                      <th>Product</th>
                      <th>Category</th>
                      <th>Brand</th>
                      <th>Price</th>
                      <th>Stock</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {products.map((p: any) => (
                      <tr key={p.id} className="ad__table-row">
                        <td>
                          <div className="ad__prod-cell">
                            <div className="ad__prod-img">
                              {p.imageUrls?.[0]
                                ? <img src={p.imageUrls[0]} alt={p.name} />
                                : '📦'}
                            </div>
                            <div>
                              <div className="ad__prod-name">{p.name}</div>
                              <div className="ad__prod-sku">{p.sku}</div>
                            </div>
                          </div>
                        </td>
                        <td><span className="ad__cat-tag">{p.categoryName || '—'}</span></td>
                        <td className="ad__brand">{p.brandName}</td>
                        <td>
                          <div className="ad__price">{fmt(p.price)}</div>
                          {p.compareAtPrice > p.price && (
                            <div className="ad__compare">{fmt(p.compareAtPrice)}</div>
                          )}
                        </td>
                        <td>
                          <span className={`ad__stock ${p.inStock !== false ? 'ad__stock--in' : 'ad__stock--out'}`}>
                            {p.inStock !== false ? `✓ In Stock` : '✕ Out of Stock'}
                          </span>
                        </td>
                        <td>
                          <span className={`ad__status-badge ${p.status === 'ACTIVE' ? 'ad__status-badge--active' : 'ad__status-badge--inactive'}`}>
                            {p.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            {/* Product Pagination */}
            {pagination.totalPages > 1 && (
              <div className="ad__pagination">
                <button className="ad__page-btn" disabled={pagination.currentPage === 0}
                  onClick={() => handleProductPage(pagination.currentPage - 1)}>← Prev</button>
                <span className="ad__page-info">Page {pagination.currentPage + 1} of {pagination.totalPages}</span>
                <button className="ad__page-btn" disabled={pagination.currentPage === pagination.totalPages - 1}
                  onClick={() => handleProductPage(pagination.currentPage + 1)}>Next →</button>
              </div>
            )}
          </div>
        )}

        {/* ════════════════════════════════════════════════════
            USERS TAB
        ════════════════════════════════════════════════════ */}
        {activeTab === 'users' && (
          <div className="ad__content">

            {/* Stats */}
            <div className="ad__stats">
              <div className="ad__stat-card">
                <div className="ad__stat-val">{usersPagination.totalElements}</div>
                <div className="ad__stat-label">Total Users</div>
              </div>
              <div className="ad__stat-card ad__stat-card--success">
                <div className="ad__stat-val">{users.filter((u: any) => u.status === 'ACTIVE').length}</div>
                <div className="ad__stat-label">Active</div>
              </div>
              <div className="ad__stat-card ad__stat-card--warning">
                <div className="ad__stat-val">{users.filter((u: any) => u.status === 'PENDING_VERIFICATION').length}</div>
                <div className="ad__stat-label">Pending</div>
              </div>
              <div className="ad__stat-card ad__stat-card--error">
                <div className="ad__stat-val">{users.filter((u: any) => u.status === 'SUSPENDED').length}</div>
                <div className="ad__stat-label">Suspended</div>
              </div>
            </div>

            {/* Users Table */}
            <div className="ad__table-wrap">
              <div className="ad__table-header">
                <h2 className="ad__section-title">Users</h2>
                <span className="ad__total">{usersPagination.totalElements} total</span>
              </div>

              {usersLoading ? (
                <div className="ad__loading">
                  <div className="ad__spinner" />
                  <span>Loading users...</span>
                </div>
              ) : users.length === 0 ? (
                <div className="ad__empty">
                  <div>👥</div>
                  <p>No users found</p>
                </div>
              ) : (
                <table className="ad__table">
                  <thead>
                    <tr>
                      <th>User</th>
                      <th>Username</th>
                      <th>Roles</th>
                      <th>Status</th>
                      <th>Verified</th>
                      <th>Joined</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((u: any) => {
                      const meta = STATUS_META[u.status] || { label: u.status, cls: '' };
                      return (
                        <tr key={u.id} className="ad__table-row">
                          <td>
                            <div className="ad__user-cell">
                              <div className="ad__user-cell-avatar">
                                {u.firstName?.[0]}{u.lastName?.[0]}
                              </div>
                              <div>
                                <div className="ad__user-cell-name">{u.firstName} {u.lastName}</div>
                                <div className="ad__user-cell-email">{u.email}</div>
                              </div>
                            </div>
                          </td>
                          <td className="ad__username">@{u.username}</td>
                          <td>
                            <div className="ad__roles">
                              {(u.roles || []).map((r: string) => (
                                <span key={r} className={`ad__role-badge ${r.includes('ADMIN') ? 'ad__role-badge--admin' : ''}`}>
                                  {r.replace('ROLE_', '')}
                                </span>
                              ))}
                            </div>
                          </td>
                          <td>
                            <span className={`ad__user-status ${meta.cls}`}>{meta.label}</span>
                          </td>
                          <td>
                            <span className={`ad__verified ${u.emailVerified ? 'ad__verified--yes' : 'ad__verified--no'}`}>
                              {u.emailVerified ? '✓ Verified' : '✕ Not Verified'}
                            </span>
                          </td>
                          <td className="ad__date">
                            {u.createdAt ? new Date(u.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : '—'}
                          </td>
                          <td>
                            <button
                              className={`ad__action-btn ${u.status === 'ACTIVE' ? 'ad__action-btn--suspend' : 'ad__action-btn--activate'}`}
                              onClick={() => handleToggleUserStatus(u.id, u.status)}
                            >
                              {u.status === 'ACTIVE' ? 'Suspend' : 'Activate'}
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>

            {/* User Pagination */}
            {usersPagination.totalPages > 1 && (
              <div className="ad__pagination">
                <button className="ad__page-btn" disabled={userPage === 0}
                  onClick={() => setUserPage(p => p - 1)}>← Prev</button>
                <span className="ad__page-info">Page {userPage + 1} of {usersPagination.totalPages}</span>
                <button className="ad__page-btn" disabled={userPage === usersPagination.totalPages - 1}
                  onClick={() => setUserPage(p => p + 1)}>Next →</button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;

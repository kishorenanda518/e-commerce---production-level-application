// src/pages/UserDashboard/UserDashboard.tsx

import React, { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '../../hooks/reduxHooks';
import { selectUser, logoutThunk }         from '../../redux/slices/authSlice';
import {
  loadProductsThunk, loadCategoriesThunk,
  setFilter, setPage,
  selectProducts, selectCategories,
  selectProductsLoading, selectFilters, selectPagination,
} from '../../redux/slices/productSlice';
import {
  addToCart, toggleCart,
  selectCartTotalItems, selectIsInCart, selectCartItemQty,
} from '../../redux/slices/cartSlice';
import { CartItem } from '../../redux/types/cart.types';
import CartDrawer   from '../../components/Cart/CartDrawer';
import { useNavigate } from 'react-router-dom';
import './UserDashboard.css';

const CAT_ICON: Record<string, string> = {
  Electronics: '⚡', Clothing: '👕', Men: '👔', Women: '👗',
  'Kids & Toys': '🧸', Home: '🏠', Phones: '📱', Watches: '⌚',
  Gadgets: '💻', Toys: '🧸', default: '🛍️',
};

const fmt  = (p: number) => `₹${Number(p).toLocaleString('en-IN')}`;
const disc = (p: number, c: number) => c && c > p ? Math.round((1 - p / c) * 100) : 0;

const CategoryPill: React.FC<{
  name: string; active: boolean; onClick: () => void;
}> = ({ name, active, onClick }) => (
  <button className={`ud__pill ${active ? 'ud__pill--active' : ''}`} onClick={onClick}>
    <span className="ud__pill-icon">{CAT_ICON[name] ?? CAT_ICON.default}</span>
    <span className="ud__pill-name">{name}</span>
  </button>
);

const UserDashboard: React.FC = () => {
  const dispatch    = useAppDispatch();
  const navigate    = useNavigate();
  const user        = useAppSelector(selectUser);
  const products    = useAppSelector(selectProducts);
  const categories  = useAppSelector(selectCategories);
  const loading     = useAppSelector(selectProductsLoading);
  const filters     = useAppSelector(selectFilters);
  const pagination  = useAppSelector(selectPagination);
  const cartCount   = useAppSelector(selectCartTotalItems);
  const [search, setSearch] = useState('');

  useEffect(() => {
    dispatch(loadCategoriesThunk());
    dispatch(loadProductsThunk({ q:'', categoryId:'', brand:'', sort:'newest', page:0, size:12 }));
  }, [dispatch]);

  const topCategories = categories.filter((c: any) => !c.parentId);

  const handleCategoryClick = (catId: string) => {
    dispatch(setFilter({ key: 'categoryId', value: catId }));
    dispatch(loadProductsThunk({ ...filters, categoryId: catId, page: 0 }));
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!search.trim()) return;
    dispatch(setFilter({ key: 'q', value: search.trim() }));
    dispatch(loadProductsThunk({ ...filters, q: search.trim(), page: 0 }));
  };

  const handleSortChange = (sort: string) => {
    dispatch(setFilter({ key: 'sort', value: sort }));
    dispatch(loadProductsThunk({ ...filters, sort, page: 0 }));
  };

  const handlePageChange = (newPage: number) => {
    dispatch(setPage(newPage));
    dispatch(loadProductsThunk({ ...filters, page: newPage }));
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleClearFilters = () => {
    setSearch('');
    dispatch(setFilter({ key: 'categoryId', value: '' }));
    dispatch(setFilter({ key: 'q', value: '' }));
    dispatch(loadProductsThunk({ q:'', categoryId:'', brand:'', sort:'newest', page:0, size:12 }));
  };

  const handleLogout = async () => {
    await dispatch(logoutThunk());
    navigate('/login', { replace: true });
  };

  return (
    <div className="ud">
      <CartDrawer />

      {/* ── NAVBAR ──────────────────────────────────────────── */}
      <nav className="ud__nav">
        <div className="ud__nav-inner">
          <div className="ud__logo">
            <div className="ud__logo-icon">♛</div>
            <div>
              <div className="ud__logo-name">ROYAL MART</div>
              <div className="ud__logo-tag">Founded by Nanda</div>
            </div>
          </div>

          <form className="ud__search" onSubmit={handleSearch}>
            <input className="ud__search-input"
              placeholder="Search products, brands..."
              value={search} onChange={e => setSearch(e.target.value)} />
            <button type="submit" className="ud__search-btn">🔍</button>
          </form>

          <div className="ud__nav-right">
            {/* Cart button with badge */}
            <button className="ud__cart-btn" onClick={() => dispatch(toggleCart())}>
              <span className="ud__cart-icon">🛒</span>
              {cartCount > 0 && (
                <span className="ud__cart-badge">
                  {cartCount > 99 ? '99+' : cartCount}
                </span>
              )}
            </button>

            <div className="ud__user-info">
              <div className="ud__user-avatar">
                {user?.firstName?.[0]}{user?.lastName?.[0]}
              </div>
              <div>
                <div className="ud__user-name">{user?.firstName} {user?.lastName}</div>
                <div className="ud__user-role">Member</div>
              </div>
            </div>
            <button className="ud__logout-btn" onClick={handleLogout}>Sign Out</button>
          </div>
        </div>

        {/* Category pills */}
        <div className="ud__cat-bar">
          <div className="ud__cat-bar-inner">
            <CategoryPill name="All" active={!filters.categoryId} onClick={handleClearFilters} />
            {topCategories.map((cat: any) => (
              <CategoryPill key={cat.id} name={cat.name}
                active={filters.categoryId === cat.id}
                onClick={() => handleCategoryClick(cat.id)} />
            ))}
          </div>
        </div>
      </nav>

      <div className="ud__body">
        {/* SIDEBAR */}
        <aside className="ud__sidebar">
          <div className="ud__sidebar-section">
            <h3 className="ud__sidebar-title">Categories</h3>
            <button className={`ud__cat-btn ${!filters.categoryId ? 'ud__cat-btn--active' : ''}`}
              onClick={handleClearFilters}><span>🛍️</span> All Products</button>
            {topCategories.map((cat: any) => (
              <button key={cat.id}
                className={`ud__cat-btn ${filters.categoryId === cat.id ? 'ud__cat-btn--active' : ''}`}
                onClick={() => handleCategoryClick(cat.id)}>
                <span>{CAT_ICON[cat.name] ?? CAT_ICON.default}</span> {cat.name}
              </button>
            ))}
          </div>

          <div className="ud__sidebar-section">
            <h3 className="ud__sidebar-title">Sort By</h3>
            {[
              { value: 'newest',      label: 'Newest First'      },
              { value: 'price,asc',   label: 'Price: Low → High' },
              { value: 'price,desc',  label: 'Price: High → Low' },
              { value: 'rating,desc', label: 'Top Rated'         },
            ].map(s => (
              <button key={s.value}
                className={`ud__sort-btn ${filters.sort === s.value ? 'ud__sort-btn--active' : ''}`}
                onClick={() => handleSortChange(s.value)}>{s.label}</button>
            ))}
          </div>
        </aside>

        {/* MAIN */}
        <main className="ud__main">
          <div className="ud__meta">
            <span className="ud__meta-count">
              {loading ? 'Loading...'
                : pagination.totalElements > 0
                ? `Showing ${pagination.currentPage * pagination.size + 1}–${
                    Math.min((pagination.currentPage + 1) * pagination.size,
                    pagination.totalElements)} of ${pagination.totalElements} products`
                : 'No products found'}
            </span>
            {(filters.categoryId || filters.q) && (
              <button className="ud__clear-btn" onClick={handleClearFilters}>
                Clear Filters ✕
              </button>
            )}
          </div>

          {loading ? (
            <div className="ud__grid">
              {Array(8).fill(0).map((_, i) => (
                <div key={i} className="ud__skeleton">
                  <div className="ud__skeleton-img" />
                  <div className="ud__skeleton-body">
                    <div className="ud__skeleton-line ud__skeleton-line--sm" />
                    <div className="ud__skeleton-line" />
                    <div className="ud__skeleton-line ud__skeleton-line--md" />
                    <div className="ud__skeleton-line ud__skeleton-line--btn" />
                  </div>
                </div>
              ))}
            </div>
          ) : products.length === 0 ? (
            <div className="ud__empty">
              <div className="ud__empty-icon">🔍</div>
              <h3>No products found</h3>
              <p>Try a different category or search term</p>
              <button className="ud__empty-btn" onClick={handleClearFilters}>
                Clear Filters
              </button>
            </div>
          ) : (
            <div className="ud__grid">
              {products.map((product: any) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}

          {pagination.totalPages > 1 && (
            <div className="ud__pagination">
              <button className="ud__page-btn"
                disabled={pagination.currentPage === 0}
                onClick={() => handlePageChange(pagination.currentPage - 1)}>← Prev</button>
              {Array.from({ length: Math.min(pagination.totalPages, 5) }, (_, i) => {
                const p = Math.max(0, pagination.currentPage - 2) + i;
                if (p >= pagination.totalPages) return null;
                return (
                  <button key={p}
                    className={`ud__page-num ${pagination.currentPage === p ? 'ud__page-num--active' : ''}`}
                    onClick={() => handlePageChange(p)}>{p + 1}</button>
                );
              })}
              <button className="ud__page-btn"
                disabled={pagination.currentPage === pagination.totalPages - 1}
                onClick={() => handlePageChange(pagination.currentPage + 1)}>Next →</button>
              <span className="ud__page-info">
                Page {pagination.currentPage + 1} of {pagination.totalPages}
              </span>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

// ── Product Card ──────────────────────────────────────────────
const ProductCard: React.FC<{ product: any }> = ({ product }) => {
  const dispatch  = useAppDispatch();
  const inCart    = useAppSelector(selectIsInCart(product.id));
  const cartQty   = useAppSelector(selectCartItemQty(product.id));
  const [justAdded, setJustAdded] = useState(false);
  const d = disc(product.price, product.compareAtPrice);

  const handleAdd = (e: React.MouseEvent) => {
    e.stopPropagation();
    const item: CartItem = {
      productId:      product.id,
      name:           product.name,
      brandName:      product.brandName || '',
      imageUrl:       product.imageUrls?.[0] || null,
      price:          Number(product.price),
      compareAtPrice: Number(product.compareAtPrice) || Number(product.price),
      quantity:       1,
      inStock:        product.inStock !== false,
      maxStock:       product.stockQuantity || 99,
      categoryName:   product.categoryName || '',
    };
    dispatch(addToCart(item));
    setJustAdded(true);
    setTimeout(() => setJustAdded(false), 2000);
  };

  return (
    <div className="ud__card">
      {d > 0 && <span className="ud__card-badge">{d}% Off</span>}
      {inCart && <span className="ud__card-in-cart">🛒 {cartQty}</span>}

      <div className="ud__card-img">
        {product.imageUrls?.[0] ? (
          <img src={product.imageUrls[0]} alt={product.name} loading="lazy"
            onError={e => { (e.target as HTMLImageElement).style.display = 'none'; }} />
        ) : (
          <span className="ud__card-img-placeholder">
            {CAT_ICON[product.categoryName] ?? '🛍️'}
          </span>
        )}
      </div>

      <div className="ud__card-body">
        <span className="ud__card-brand">{product.brandName}</span>
        <h3 className="ud__card-name">{product.name}</h3>
        {product.averageRating > 0 && (
          <div className="ud__card-stars">
            {Array(5).fill(0).map((_, i) => (
              <span key={i} style={{
                color: i < Math.round(product.averageRating) ? '#C9A84C' : '#E8E2D4'
              }}>★</span>
            ))}
            <span className="ud__card-reviews">({product.reviewCount || 0})</span>
          </div>
        )}
        <div className="ud__card-prices">
          <span className="ud__card-price">{fmt(product.price)}</span>
          {product.compareAtPrice > product.price && (
            <span className="ud__card-compare">{fmt(product.compareAtPrice)}</span>
          )}
        </div>
        <button
          className={`ud__card-btn ${justAdded ? 'ud__card-btn--added' : inCart ? 'ud__card-btn--in-cart' : ''}`}
          onClick={handleAdd}
          disabled={product.inStock === false}
        >
          {product.inStock === false ? 'Out of Stock'
            : justAdded ? '✓ Added to Cart'
            : inCart    ? `Add More (${cartQty} in cart)`
            : 'Add to Cart'}
        </button>
      </div>
    </div>
  );
};

export default UserDashboard;

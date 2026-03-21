// src/redux/slices/productSlice.ts

import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import {
  fetchProductsApi,
  fetchCategoriesApi,
  fetchProductByIdApi,
} from '../services/productService';
import { ProductState, ProductFilters } from '../types/product.types';

// ── Initial State ─────────────────────────────────────────────
const initialState: ProductState = {
  items:             [],
  categories:        [],
  selectedProduct:   null,
  loading:           false,
  categoriesLoading: false,
  error:             null,
  filters: {
    q:          '',
    categoryId: '',
    brand:      '',
    sort:       'newest',
    page:       0,
    size:       12,
  },
  pagination: {
    totalElements: 0,
    totalPages:    0,
    currentPage:   0,
    size:          12,
  },
};

// ── Thunks ────────────────────────────────────────────────────
export const loadProductsThunk = createAsyncThunk(
  'products/loadProducts',
  async (filters: ProductFilters, { rejectWithValue }) => {
    try {
      return await fetchProductsApi(filters);
    } catch (err: any) {
      return rejectWithValue(err.response?.data?.message || 'Failed to load products');
    }
  }
);

export const loadCategoriesThunk = createAsyncThunk(
  'products/loadCategories',
  async (_, { rejectWithValue }) => {
    try {
      const data = await fetchCategoriesApi();
      // API returns flat list — filter only top level (parentId === null)
      // but store ALL so sidebar can filter
      return Array.isArray(data) ? data : [];
    } catch (err: any) {
      return rejectWithValue('Failed to load categories');
    }
  }
);

export const loadProductByIdThunk = createAsyncThunk(
  'products/loadById',
  async (id: string, { rejectWithValue }) => {
    try {
      return await fetchProductByIdApi(id);
    } catch (err: any) {
      return rejectWithValue('Product not found');
    }
  }
);


// ── Slice ─────────────────────────────────────────────────────
const productSlice = createSlice({
  name: 'products',
  initialState,
  reducers: {
    // update a single filter key and reset to page 0
    setFilter(state, action: PayloadAction<{ key: keyof ProductFilters; value: any }>) {
      (state.filters as any)[action.payload.key] = action.payload.value;
      state.filters.page = 0;
    },
    // update multiple filters at once and reset to page 0
    setFilters(state, action: PayloadAction<Partial<ProductFilters>>) {
      state.filters = { ...state.filters, ...action.payload, page: 0 };
    },
    // only change page — keep existing filters
    setPage(state, action: PayloadAction<number>) {
      state.filters.page = action.payload;
    },
    clearFilters(state) {
      state.filters = initialState.filters;
    },
    clearSelectedProduct(state) {
      state.selectedProduct = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // ── loadProducts ───────────────────────────────────────
      .addCase(loadProductsThunk.pending,   (state) => {
        state.loading = true;
        state.error   = null;
      })
      .addCase(loadProductsThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.items   = action.payload?.content || [];
        state.pagination = {
          totalElements: action.payload?.totalElements || 0,
          totalPages:    action.payload?.totalPages    || 0,
          currentPage:   action.payload?.number        || 0,
          size:          action.payload?.size          || 12,
        };
      })
      .addCase(loadProductsThunk.rejected,  (state, action) => {
        state.loading = false;
        state.error   = action.payload as string;
      })

      // ── loadCategories ─────────────────────────────────────
      .addCase(loadCategoriesThunk.pending,   (state) => { state.categoriesLoading = true; })
      .addCase(loadCategoriesThunk.fulfilled, (state, action) => {
        state.categoriesLoading = false;
        state.categories        = action.payload || [];
      })
      .addCase(loadCategoriesThunk.rejected,  (state) => { state.categoriesLoading = false; })

      // ── loadById ───────────────────────────────────────────
      .addCase(loadProductByIdThunk.fulfilled, (state, action) => {
        state.selectedProduct = action.payload;
      });
  },
});

export const {
  setFilter,
  setFilters,
  setPage,
  clearFilters,
  clearSelectedProduct,
} = productSlice.actions;

// ── Selectors ─────────────────────────────────────────────────
export const selectProducts          = (state: any) => state.products.items;
export const selectCategories        = (state: any) => state.products.categories;
export const selectSelectedProduct   = (state: any) => state.products.selectedProduct;
export const selectProductsLoading   = (state: any) => state.products.loading;
export const selectCategoriesLoading = (state: any) => state.products.categoriesLoading;
export const selectProductsError     = (state: any) => state.products.error;
export const selectFilters           = (state: any) => state.products.filters;
export const selectPagination        = (state: any) => state.products.pagination;

export default productSlice.reducer;
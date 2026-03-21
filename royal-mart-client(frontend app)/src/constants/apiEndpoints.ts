// src/constants/apiEndpoints.ts

export const BASE_URLS = {
  USER:    'http://localhost:8081',
  PRODUCT: 'http://localhost:8082',
  ORDER:   'http://localhost:8083',
};

export const AUTH_ENDPOINTS = {
  LOGIN:           `${BASE_URLS.USER}/api/v1/auth/login`,
  REGISTER:        `${BASE_URLS.USER}/api/v1/auth/register`,
  LOGOUT:          `${BASE_URLS.USER}/api/v1/auth/logout`,
  REFRESH_TOKEN:   `${BASE_URLS.USER}/api/v1/auth/refresh-token`,
  FORGOT_PASSWORD: `${BASE_URLS.USER}/api/v1/users/forgot-password`,
  RESET_PASSWORD:  `${BASE_URLS.USER}/api/v1/users/reset-password`,
};

export const USER_ENDPOINTS = {
  ME:            `${BASE_URLS.USER}/api/v1/users/me`,
  ME_PASSWORD:   `${BASE_URLS.USER}/api/v1/users/me/password`,
  ME_ADDRESSES:  `${BASE_URLS.USER}/api/v1/users/me/addresses`,
  ALL_USERS:     `${BASE_URLS.USER}/api/v1/auth/users`,
  UPDATE_STATUS: (id: string) => `${BASE_URLS.USER}/api/v1/admin/users/${id}/status`,
  UPDATE_ROLE:   (id: string) => `${BASE_URLS.USER}/api/v1/admin/users/${id}/role`,
};

export const PRODUCT_ENDPOINTS = {
  ALL:          `${BASE_URLS.PRODUCT}/api/v1/products`,
  SEARCH:       `${BASE_URLS.PRODUCT}/api/v1/products/search`,
  BY_ID:        (id: string) => `${BASE_URLS.PRODUCT}/api/v1/products/${id}`,
  FEATURED:     `${BASE_URLS.PRODUCT}/api/v1/products/featured`,
  NEW_ARRIVALS: `${BASE_URLS.PRODUCT}/api/v1/products/new-arrivals`,
  CATEGORIES:   `${BASE_URLS.PRODUCT}/api/v1/categories`,
  ADMIN_CREATE: `${BASE_URLS.PRODUCT}/api/v1/admin/products`,
  ADMIN_BULK:   `${BASE_URLS.PRODUCT}/api/v1/admin/products/bulk`,
  ADMIN_STATUS: (id: string) => `${BASE_URLS.PRODUCT}/api/v1/admin/products/${id}/status`,
  ADMIN_BY_ID:  (id: string) => `${BASE_URLS.PRODUCT}/api/v1/admin/products/${id}`,
};

export const ORDER_ENDPOINTS = {
  CREATE:       `${BASE_URLS.ORDER}/api/v1/orders`,
  MY_ORDERS:    `${BASE_URLS.ORDER}/api/v1/orders/my-orders`,
  BY_ID:        (id: string) => `${BASE_URLS.ORDER}/api/v1/orders/${id}`,
  CANCEL:       (id: string) => `${BASE_URLS.ORDER}/api/v1/orders/${id}/cancel`,
  ADMIN_ALL:    `${BASE_URLS.ORDER}/api/v1/admin/orders`,
  ADMIN_STATUS: (id: string) => `${BASE_URLS.ORDER}/api/v1/admin/orders/${id}/status`,
};
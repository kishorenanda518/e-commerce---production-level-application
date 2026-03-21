import axios from 'axios';
import { PRODUCT_ENDPOINTS } from '../../constants/apiEndpoints';
import { ProductFilters }    from '../types/product.types';

export const fetchProductsApi = async (filters: ProductFilters) => {
  const {
    q, categoryId, brand, inStock,
    minPrice, maxPrice, sort, page, size
  } = filters;

  // always use /products endpoint — backend handles all filters
  const params: Record<string, any> = { page, size, sort };

  // only add param if it has a value
  if (q?.trim())              params.q          = q.trim();
  if (categoryId?.trim())     params.categoryId = categoryId.trim();
  if (brand?.trim())          params.brand      = brand.trim();
  if (inStock !== undefined)  params.inStock    = inStock;
  if (minPrice !== undefined) params.minPrice   = minPrice;
  if (maxPrice !== undefined) params.maxPrice   = maxPrice;

  console.log('Fetching products with params:', params);

  const res = await axios.get(PRODUCT_ENDPOINTS.ALL, { params });
  return res.data.data;
};

export const fetchCategoriesApi = async () => {
  const res = await axios.get(PRODUCT_ENDPOINTS.CATEGORIES);
  console.log('Categories loaded:', res.data.data?.length);
  return res.data.data;
};

export const fetchProductByIdApi = async (id: string) => {
  const res = await axios.get(PRODUCT_ENDPOINTS.BY_ID(id));
  return res.data.data;
};
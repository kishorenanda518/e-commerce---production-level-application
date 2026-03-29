export interface ProductFilters {
  q?:          string;
  categoryId?: string;
  brand?:      string;
  inStock?:    boolean;
  minPrice?:   number;
  maxPrice?:   number;
  sort:        string;
  page:        number;
  size:        number;
}

export interface Category {
  id:           string;
  name:         string;
  slug:         string;
  description:  string;
  parentId:     string | null;
  displayOrder: number;
  isActive:     boolean;
  imageUrl:     string | null;
}

export interface Product {
  id:              string;
  name:            string;
  description:     string;
  shortDescription:string;
  sku:             string;
  price:           number;
  compareAtPrice:  number;
  categoryId:      string;
  categoryName:    string;
  brandName:       string;
  tags:            string[];
  status:          string;
  inStock:         boolean;
  stockQuantity:   number;
  averageRating:   number;
  reviewCount:     number;
  imageUrls:       string[];
  attributes:      Record<string, string>;
  createdAt:       string;
}

export interface PageResponse<T> {
  content:       T[];
  totalElements: number;
  totalPages:    number;
  number:        number;
  size:          number;
}

export interface ProductState {
  items:            Product[];
  categories:       Category[];
  selectedProduct:  Product | null;
  filters:          ProductFilters;
  pagination: {
    totalElements: number;
    totalPages:    number;
    currentPage:   number;
    size:          number;
  };
  loading:           boolean;
  categoriesLoading: boolean;
  error:             string | null;
}
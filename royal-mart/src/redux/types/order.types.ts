// src/redux/types/order.types.ts

export interface OrderItemRequest {
  productId: string;
  quantity:  number;
}

export interface CreateOrderRequest {
  items:          OrderItemRequest[];
  shippingAddress: AddressRequest;
  paymentMethod:  string;   // COD | ONLINE
  notes?:         string;
}

export interface AddressRequest {
  fullName:    string;
  phone:       string;
  addressLine1:string;
  addressLine2?:string;
  city:        string;
  state:       string;
  pincode:     string;
  country:     string;
}

export interface OrderItem {
  id:          string;
  productId:   string;
  productName: string;
  productSku:  string;
  brandName:   string;
  imageUrl:    string;
  quantity:    number;
  unitPrice:   number;
  totalPrice:  number;
}

export interface Order {
  id:              string;
  orderNumber:     string;
  userId:          string;
  status:          OrderStatus;
  paymentStatus:   PaymentStatus;
  paymentMethod:   string;
  items:           OrderItem[];
  subtotal:        number;
  shippingCharge:  number;
  taxAmount:       number;
  discountAmount:  number;
  totalAmount:     number;
  shippingAddress: Address;
  notes:           string;
  createdAt:       string;
  updatedAt:       string;
}

export interface Address {
  fullName:     string;
  phone:        string;
  addressLine1: string;
  addressLine2: string;
  city:         string;
  state:        string;
  pincode:      string;
  country:      string;
}

export interface OrderStatusHistory {
  id:        string;
  status:    OrderStatus;
  comment:   string;
  createdAt: string;
}

export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'REFUNDED';

export type PaymentStatus =
  | 'PENDING'
  | 'PAID'
  | 'FAILED'
  | 'REFUNDED';

export interface OrderState {
  orders:         Order[];
  currentOrder:   Order | null;
  orderHistory:   OrderStatusHistory[];
  loading:        boolean;
  placing:        boolean;   // placing new order
  error:          string | null;
  orderSuccess:   boolean;
  pagination: {
    totalElements: number;
    totalPages:    number;
    currentPage:   number;
    size:          number;
  };
}
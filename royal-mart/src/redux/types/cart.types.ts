export interface CartItem {
  productId:    string;
  name:         string;
  brandName:    string;
  imageUrl:     string | null;
  price:        number;
  compareAtPrice: number;
  quantity:     number;
  inStock:      boolean;
  maxStock:     number;
  categoryName: string;
}
 
export interface CartState {
  items:        CartItem[];
  isOpen:       boolean;   // drawer open/close
  totalItems:   number;
  totalPrice:   number;
  discount:     number;
  finalPrice:   number;
}
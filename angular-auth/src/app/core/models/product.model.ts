export interface Product {
  productId?: number;
  productName: string;
  productPrice: number;
  productCategory: string;
  sku: string;
  brand?: string;
  description?: string;
  imageUrl?: string;
  unit: string;
  status?: 'ACTIVE' | 'INACTIVE' | 'DISCONTINUED';
  createdAt?: string;
  updatedAt?: string;
}

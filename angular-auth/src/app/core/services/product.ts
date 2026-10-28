import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Product as ProductModel } from '../models/product.model';

@Injectable({
  providedIn: 'root',
})
export class Product {
  private readonly http = inject(HttpClient, { optional: true });
  private readonly baseUrl = '/api/products';

  getAll(): Observable<ProductModel[]> {
    return this.http ? this.http.get<ProductModel[]>(this.baseUrl) : new Observable();
  }

  getById(id: number): Observable<ProductModel> {
    return this.http ? this.http.get<ProductModel>(`${this.baseUrl}/${id}`) : new Observable();
  }

  create(product: Partial<ProductModel>): Observable<ProductModel> {
    return this.http ? this.http.post<ProductModel>(this.baseUrl, product) : new Observable();
  }

  update(id: number, product: Partial<ProductModel>): Observable<ProductModel> {
    return this.http ? this.http.put<ProductModel>(`${this.baseUrl}/${id}`, product) : new Observable();
  }

  delete(id: number): Observable<void> {
    return this.http ? this.http.delete<void>(`${this.baseUrl}/${id}`) : new Observable();
  }

  searchByName(name: string): Observable<ProductModel[]> {
    return this.http ? this.http.get<ProductModel[]>(`${this.baseUrl}/search/name`, { params: { name } }) : new Observable();
  }

  searchByCategory(category: string): Observable<ProductModel[]> {
    return this.http ? this.http.get<ProductModel[]>(`${this.baseUrl}/search/category`, { params: { category } }) : new Observable();
  }

  searchByBrand(brand: string): Observable<ProductModel[]> {
    return this.http ? this.http.get<ProductModel[]>(`${this.baseUrl}/search/brand`, { params: { brand } }) : new Observable();
  }

  bulkImport(file: File): Observable<{ importedCount: number; skippedCount: number; errors: string[] }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      ? this.http.post<{ importedCount: number; skippedCount: number; errors: string[] }>(`${this.baseUrl}/bulk-import`, formData)
      : new Observable();
  }

  exportCsv(): Observable<Blob> {
    return this.http ? this.http.get(`${this.baseUrl}/export/csv`, { responseType: 'blob' }) : new Observable();
  }
}

export { Product as ProductService };

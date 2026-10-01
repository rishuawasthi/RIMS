import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class InventoryApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  list<T>(resource: string): Observable<T[]> {
    return this.http.get<T[]>(`${this.baseUrl}/${resource}`);
  }

  create<T>(resource: string, body: unknown): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}/${resource}`, body);
  }

  update<T>(resource: string, id: string | number, body: unknown): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}/${resource}/${id}`, body);
  }

  postAction<T>(resource: string, id: string | number, action: string, parameters?: Record<string, string>): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}/${resource}/${id}/${action}`, null, { params: parameters });
  }

  remove(resource: string, id: string | number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${resource}/${id}`);
  }
}
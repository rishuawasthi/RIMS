import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { InventoryApi } from '../../core/services/inventory-api';

type DashboardRecord = Record<string, unknown>;

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Dashboard {
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly products = signal<DashboardRecord[]>([]);
  readonly inventory = signal<DashboardRecord[]>([]);
  readonly lowStock = signal<DashboardRecord[]>([]);
  readonly orders = signal<DashboardRecord[]>([]);
  readonly purchaseOrders = signal<DashboardRecord[]>([]);
  readonly suppliers = signal<DashboardRecord[]>([]);
  readonly kpis = signal<Record<string, unknown>>({});
  readonly categories = signal<DashboardRecord[]>([]);

  readonly openOrders = computed(() => this.orders().filter(order => !['FULFILLED', 'CANCELLED'].includes(String(order['status']))).length);
  readonly openPurchaseOrders = computed(() => this.purchaseOrders().filter(order => !['RECEIVED', 'CANCELLED'].includes(String(order['status']))).length);

  private readonly api = inject(InventoryApi);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    forkJoin({
      products: this.api.list<DashboardRecord>('products').pipe(catchError(() => of([]))),
      inventory: this.api.list<DashboardRecord>('inventory').pipe(catchError(() => of([]))),
      lowStock: this.api.list<DashboardRecord>('inventory/low-stock').pipe(catchError(() => of([]))),
      orders: this.api.list<DashboardRecord>('orders').pipe(catchError(() => of([]))),
      purchaseOrders: this.api.list<DashboardRecord>('purchase-orders').pipe(catchError(() => of([]))),
      suppliers: this.api.list<DashboardRecord>('suppliers').pipe(catchError(() => of([]))),
      kpiData: this.api.get<Record<string, unknown>>('analytics/kpis').pipe(catchError(() => of({}))),
      categoryData: this.api.list<DashboardRecord>('analytics/categories').pipe(catchError(() => of([]))),
    }).subscribe({
      next: data => {
        this.products.set(data.products);
        this.inventory.set(data.inventory);
        this.lowStock.set(data.lowStock);
        this.orders.set(data.orders);
        this.purchaseOrders.set(data.purchaseOrders);
        this.suppliers.set(data.suppliers);
        this.kpis.set(data.kpiData);
        this.categories.set(data.categoryData);
        this.loading.set(false);
      },
      error: error => {
        this.errorMessage.set(error instanceof HttpErrorResponse && error.status === 0
          ? 'Inventory API unavailable. Start the backend on port 8080 to load live figures.'
          : 'Some dashboard data could not be loaded.');
        this.loading.set(false);
      },
    });
  }

  formatCurrency(value: unknown): string {
    const amount = Number(value ?? 0);
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(Number.isFinite(amount) ? amount : 0);
  }

  formatNumber(value: unknown): string {
    const amount = Number(value ?? 0);
    return new Intl.NumberFormat('en-IN').format(Number.isFinite(amount) ? amount : 0);
  }
}

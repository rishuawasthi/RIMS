import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
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
  readonly openOrders = computed(() => this.orders().filter(order => !['FULFILLED', 'CANCELLED'].includes(String(order['status']))).length);
  readonly openPurchaseOrders = computed(() => this.purchaseOrders().filter(order => !['RECEIVED', 'CANCELLED'].includes(String(order['status']))).length);

  private readonly api = inject(InventoryApi);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    const endpoints = ['products', 'inventory', 'inventory/low-stock', 'orders', 'purchase-orders', 'suppliers'];
    forkJoin(endpoints.map(endpoint => this.api.list<DashboardRecord>(endpoint))).subscribe({
      next: ([products, inventory, lowStock, orders, purchaseOrders, suppliers]) => {
        this.products.set(products);
        this.inventory.set(inventory);
        this.lowStock.set(lowStock);
        this.orders.set(orders);
        this.purchaseOrders.set(purchaseOrders);
        this.suppliers.set(suppliers);
        this.loading.set(false);
      },
      error: error => {
        this.errorMessage.set(error instanceof HttpErrorResponse && error.status === 0
          ? 'Inventory API unavailable. Start the backend on port 8083 to load live figures.'
          : 'Some dashboard data could not be loaded.');
        this.loading.set(false);
      },
    });
  }

  formatCurrency(value: unknown): string {
    const amount = Number(value ?? 0);
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(Number.isFinite(amount) ? amount : 0);
  }
}

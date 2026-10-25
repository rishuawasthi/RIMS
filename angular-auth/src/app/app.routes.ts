import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/login/login')
        .then(m => m.Login),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard')
        .then(m => m.Dashboard),
  },
  {
    path: 'products',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/products/products')
        .then(m => m.Products),
  },
  {
    path: 'warehouses',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/warehouses/warehouses')
        .then(m => m.Warehouses),
  },
  {
    path: 'inventory',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/inventory/inventory')
        .then(m => m.Inventory),
  },
  {
    path: 'suppliers',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/suppliers/suppliers')
        .then(m => m.Suppliers),
  },
  {
    path: 'purchase-orders',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/purchase-orders/purchase-orders')
        .then(m => m.PurchaseOrders),
  },
  {
    path: 'orders',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/orders/orders')
        .then(m => m.Orders),
  },
  {
    path: 'order-items',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/order-items/order-items')
        .then(m => m.OrderItems),
  },
  {
    path: 'users',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/users/users')
        .then(m => m.Users),
  },
  {
    path: 'stock-movements',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/stock-movements/stock-movements')
        .then(m => m.StockMovements),
  },
  {
    path: 'scanner',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/scanner/scanner')
        .then(m => m.Scanner),
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
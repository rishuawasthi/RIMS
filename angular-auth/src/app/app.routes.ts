import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard')
        .then(m => m.Dashboard)
  },
  {
    path: 'products',
    loadComponent: () =>
      import('./features/products/products')
        .then(m => m.Products)
  },
  {
    path: 'warehouses',
    loadComponent: () =>
      import('./features/warehouses/warehouses')
        .then(m => m.Warehouses)
  },
  {
    path: 'inventory',
    loadComponent: () =>
      import('./features/inventory/inventory')
        .then(m => m.Inventory)
  },
  {
    path: 'suppliers',
    loadComponent: () =>
      import('./features/suppliers/suppliers')
        .then(m => m.Suppliers)
  },
  {
    path: 'purchase-orders',
    loadComponent: () =>
      import('./features/purchase-orders/purchase-orders')
        .then(m => m.PurchaseOrders)
  },
  {
    path: 'orders',
    loadComponent: () =>
      import('./features/orders/orders')
        .then(m => m.Orders)
  },
  {
    path: 'order-items',
    loadComponent: () =>
      import('./features/order-items/order-items')
        .then(m => m.OrderItems)
  },
  {
    path: 'users',
    loadComponent: () =>
      import('./features/users/users')
        .then(m => m.Users)
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
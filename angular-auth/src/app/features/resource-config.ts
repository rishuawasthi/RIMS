export type ResourceFieldType = 'text' | 'email' | 'tel' | 'number' | 'date' | 'select' | 'textarea' | 'password';

export interface ResourceField {
  key: string;
  label: string;
  type?: ResourceFieldType;
  required?: boolean;
  options?: readonly string[];
  min?: number;
  step?: string;
}

export interface ResourceColumn {
  key: string;
  label: string;
}

export interface ResourceAction {
  label: string;
  path: string;
  statuses: readonly string[];
  queryParameter?: string;
}

export interface ResourceConfig {
  title: string;
  description: string;
  endpoint: string;
  idKey: string;
  columns: readonly ResourceColumn[];
  fields: readonly ResourceField[];
  actions?: readonly ResourceAction[];
}

export const resourceConfigs: Record<string, ResourceConfig> = {
  products: {
    title: 'Products',
    description: 'Maintain the catalog, pricing, and product availability.',
    endpoint: 'products',
    idKey: 'productId',
    columns: [
      { key: 'productName', label: 'Product' },
      { key: 'sku', label: 'SKU' },
      { key: 'productCategory', label: 'Category' },
      { key: 'brand', label: 'Brand' },
      { key: 'productPrice', label: 'Price' },
      { key: 'status', label: 'Status' },
    ],
    fields: [
      { key: 'productName', label: 'Product name', required: true },
      { key: 'sku', label: 'SKU', required: true },
      { key: 'productCategory', label: 'Category', required: true },
      { key: 'productPrice', label: 'Price', type: 'number', required: true, min: 0.01, step: '0.01' },
      { key: 'brand', label: 'Brand' },
      { key: 'unit', label: 'Unit', required: true },
      { key: 'status', label: 'Status', type: 'select', required: true, options: ['ACTIVE', 'INACTIVE', 'DISCONTINUED'] },
      { key: 'description', label: 'Description', type: 'textarea' },
    ],
  },
  warehouses: {
    title: 'Warehouses',
    description: 'Manage storage locations, contacts, and capacity.',
    endpoint: 'warehouses',
    idKey: 'warehouseId',
    columns: [
      { key: 'warehouseName', label: 'Warehouse' },
      { key: 'warehouseCode', label: 'Code' },
      { key: 'warehouseLocation', label: 'Location' },
      { key: 'managerName', label: 'Manager' },
      { key: 'capacity', label: 'Capacity' },
      { key: 'status', label: 'Status' },
    ],
    fields: [
      { key: 'warehouseCode', label: 'Warehouse code', required: true },
      { key: 'warehouseName', label: 'Warehouse name', required: true },
      { key: 'warehouseLocation', label: 'Location', required: true },
      { key: 'address', label: 'Address', required: true },
      { key: 'city', label: 'City', required: true },
      { key: 'state', label: 'State', required: true },
      { key: 'pincode', label: 'PIN code', required: true },
      { key: 'managerName', label: 'Manager name' },
      { key: 'contactNumber', label: 'Contact number' },
      { key: 'email', label: 'Email', type: 'email' },
      { key: 'capacity', label: 'Capacity', type: 'number', min: 0 },
      { key: 'status', label: 'Status', type: 'select', required: true, options: ['ACTIVE', 'INACTIVE', 'CLOSED'] },
    ],
  },
  inventory: {
    title: 'Inventory',
    description: 'Track on-hand quantities and replenishment thresholds by location.',
    endpoint: 'inventory',
    idKey: 'inventoryId',
    columns: [
      { key: 'product.productName', label: 'Product' },
      { key: 'product.sku', label: 'SKU' },
      { key: 'warehouse.warehouseName', label: 'Warehouse' },
      { key: 'quantity', label: 'On hand' },
      { key: 'reservedQuantity', label: 'Reserved' },
      { key: 'reorderLevel', label: 'Reorder at' },
      { key: 'status', label: 'Status' },
    ],
    fields: [
      { key: 'product.productId', label: 'Product ID', type: 'number', required: true, min: 1 },
      { key: 'warehouse.warehouseId', label: 'Warehouse ID', type: 'number', required: true, min: 1 },
      { key: 'quantity', label: 'Quantity', type: 'number', required: true, min: 0 },
      { key: 'reorderLevel', label: 'Reorder level', type: 'number', required: true, min: 0 },
      { key: 'reservedQuantity', label: 'Reserved quantity', type: 'number', min: 0 },
      { key: 'status', label: 'Status', type: 'select', required: true, options: ['ACTIVE', 'INACTIVE'] },
    ],
  },
  suppliers: {
    title: 'Suppliers',
    description: 'Keep supplier contacts, payment terms, and account status current.',
    endpoint: 'suppliers',
    idKey: 'supplierId',
    columns: [
      { key: 'supplierName', label: 'Supplier' },
      { key: 'supplierCode', label: 'Code' },
      { key: 'contactPerson', label: 'Contact' },
      { key: 'email', label: 'Email' },
      { key: 'city', label: 'City' },
      { key: 'status', label: 'Status' },
    ],
    fields: [
      { key: 'supplierCode', label: 'Supplier code', required: true },
      { key: 'supplierName', label: 'Supplier name', required: true },
      { key: 'contactPerson', label: 'Contact person' },
      { key: 'email', label: 'Email', type: 'email', required: true },
      { key: 'phone', label: 'Phone', type: 'tel', required: true },
      { key: 'address', label: 'Address', required: true },
      { key: 'city', label: 'City', required: true },
      { key: 'state', label: 'State', required: true },
      { key: 'pincode', label: 'PIN code', required: true },
      { key: 'taxId', label: 'Tax ID' },
      { key: 'paymentTerms', label: 'Payment terms' },
    ],
    actions: [
      { label: 'Activate', path: 'activate', statuses: ['INACTIVE'] },
      { label: 'Deactivate', path: 'deactivate', statuses: ['ACTIVE'] },
      { label: 'Block', path: 'block', statuses: ['ACTIVE', 'INACTIVE'] },
    ],
  },
  'purchase-orders': {
    title: 'Purchase orders',
    description: 'Create and monitor supplier replenishment orders.',
    endpoint: 'purchase-orders',
    idKey: 'poId',
    columns: [
      { key: 'poNumber', label: 'PO number' },
      { key: 'supplier.supplierName', label: 'Supplier' },
      { key: 'product.productName', label: 'Product' },
      { key: 'quantity', label: 'Quantity' },
      { key: 'totalAmount', label: 'Total' },
      { key: 'expectedDeliveryDate', label: 'Expected' },
      { key: 'status', label: 'Status' },
    ],
    fields: [
      { key: 'poNumber', label: 'PO number', required: true },
      { key: 'supplier.supplierId', label: 'Supplier ID', type: 'number', required: true, min: 1 },
      { key: 'product.productId', label: 'Product ID', type: 'number', required: true, min: 1 },
      { key: 'warehouse.warehouseId', label: 'Warehouse ID', type: 'number', required: true, min: 1 },
      { key: 'quantity', label: 'Quantity', type: 'number', required: true, min: 1 },
      { key: 'unitPrice', label: 'Unit price', type: 'number', required: true, min: 0.01, step: '0.01' },
      { key: 'expectedDeliveryDate', label: 'Expected delivery', type: 'date' },
      { key: 'notes', label: 'Notes', type: 'textarea' },
    ],
    actions: [
      { label: 'Place', path: 'place', statuses: ['DRAFT'] },
      { label: 'Receive', path: 'receive', statuses: ['PLACED', 'PARTIALLY_RECEIVED'], queryParameter: 'quantity' },
      { label: 'Cancel', path: 'cancel', statuses: ['DRAFT', 'PLACED', 'PARTIALLY_RECEIVED'] },
    ],
  },
  orders: {
    title: 'Customer orders',
    description: 'Review customer details, fulfillment state, and order totals.',
    endpoint: 'orders',
    idKey: 'orderId',
    columns: [
      { key: 'orderNumber', label: 'Order' },
      { key: 'customerName', label: 'Customer' },
      { key: 'customerEmail', label: 'Email' },
      { key: 'city', label: 'City' },
      { key: 'totalAmount', label: 'Total' },
      { key: 'status', label: 'Status' },
    ],
    fields: [
      { key: 'orderNumber', label: 'Order number', required: true },
      { key: 'customerName', label: 'Customer name', required: true },
      { key: 'customerEmail', label: 'Email', type: 'email', required: true },
      { key: 'customerPhone', label: 'Phone', type: 'tel' },
      { key: 'shippingAddress', label: 'Shipping address', type: 'textarea', required: true },
      { key: 'city', label: 'City', required: true },
      { key: 'state', label: 'State', required: true },
      { key: 'pincode', label: 'PIN code', required: true },
      { key: 'notes', label: 'Notes', type: 'textarea' },
    ],
    actions: [
      { label: 'Confirm', path: 'confirm', statuses: ['DRAFT'] },
      { label: 'Cancel', path: 'cancel', statuses: ['DRAFT', 'CONFIRMED', 'PROCESSING', 'PARTIALLY_FULFILLED', 'BACKORDERED'] },
    ],
  },
  'order-items': {
    title: 'Order items',
    description: 'Manage product allocations for individual customer orders.',
    endpoint: 'order-items',
    idKey: 'orderItemId',
    columns: [
      { key: 'order.orderNumber', label: 'Order' },
      { key: 'product.productName', label: 'Product' },
      { key: 'warehouse.warehouseName', label: 'Warehouse' },
      { key: 'quantity', label: 'Quantity' },
      { key: 'allocatedQuantity', label: 'Allocated' },
      { key: 'subtotal', label: 'Subtotal' },
      { key: 'status', label: 'Status' },
    ],
    fields: [
      { key: 'order.orderId', label: 'Order ID', type: 'number', required: true, min: 1 },
      { key: 'product.productId', label: 'Product ID', type: 'number', required: true, min: 1 },
      { key: 'warehouse.warehouseId', label: 'Warehouse ID', type: 'number', required: true, min: 1 },
      { key: 'quantity', label: 'Quantity', type: 'number', required: true, min: 1 },
      { key: 'unitPrice', label: 'Unit price', type: 'number', required: true, min: 0.01, step: '0.01' },
      { key: 'allocatedQuantity', label: 'Allocated quantity', type: 'number', min: 0 },
    ],
    actions: [
      { label: 'Cancel', path: 'cancel', statuses: ['PENDING', 'ALLOCATED', 'PARTIALLY_ALLOCATED', 'BACKORDERED'] },
    ],
  },
  users: {
    title: 'Users',
    description: 'Manage team access and account status.',
    endpoint: 'users',
    idKey: 'userId',
    columns: [
      { key: 'fullName', label: 'Name' },
      { key: 'username', label: 'Username' },
      { key: 'email', label: 'Email' },
      { key: 'phone', label: 'Phone' },
      { key: 'role', label: 'Role' },
      { key: 'status', label: 'Status' },
    ],
    fields: [
      { key: 'username', label: 'Username', required: true },
      { key: 'password', label: 'Password', type: 'password', required: true },
      { key: 'fullName', label: 'Full name', required: true },
      { key: 'email', label: 'Email', type: 'email', required: true },
      { key: 'phone', label: 'Phone', type: 'tel' },
      { key: 'role', label: 'Role', type: 'select', required: true, options: ['ADMIN', 'INVENTORY_MANAGER', 'STORE_STAFF', 'SUPPLIER'] },
    ],
    actions: [
      { label: 'Activate', path: 'activate', statuses: ['INACTIVE'] },
      { label: 'Deactivate', path: 'deactivate', statuses: ['ACTIVE'] },
      { label: 'Lock', path: 'lock', statuses: ['ACTIVE'] },
      { label: 'Unlock', path: 'unlock', statuses: ['LOCKED'] },
    ],
  },
};
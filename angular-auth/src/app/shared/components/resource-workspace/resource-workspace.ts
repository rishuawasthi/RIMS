import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { FormControl, FormRecord, ReactiveFormsModule, Validators } from '@angular/forms';
import { InventoryApi } from '../../../core/services/inventory-api';
import { ResourceAction, ResourceConfig, ResourceField } from '../../../features/resource-config';

type ResourceRecord = Record<string, unknown>;

@Component({
  selector: 'app-resource-workspace',
  imports: [ReactiveFormsModule],
  templateUrl: './resource-workspace.html',
  styleUrl: './resource-workspace.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResourceWorkspace implements OnInit {
  readonly config = input.required<ResourceConfig>();
  readonly records = signal<ResourceRecord[]>([]);
  readonly query = signal('');
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly editorOpen = signal(false);
  readonly editingId = signal<string | number | null>(null);
  readonly errorMessage = signal('');
  readonly form = new FormRecord<FormControl<unknown>>({});
  readonly filteredRecords = computed(() => {
    const search = this.query().trim().toLocaleLowerCase();
    if (!search) return this.records();
    return this.records().filter(record =>
      this.config().columns.some(column => this.displayValue(record, column.key).toLocaleLowerCase().includes(search)),
    );
  });

  private readonly api = inject(InventoryApi);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.api.list<ResourceRecord>(this.config().endpoint).subscribe({
      next: records => {
        this.records.set(records);
        this.loading.set(false);
      },
      error: error => {
        this.errorMessage.set(this.errorText(error));
        this.loading.set(false);
      },
    });
  }

  beginCreate(): void {
    this.editingId.set(null);
    this.buildForm();
    this.errorMessage.set('');
    this.editorOpen.set(true);
  }

  beginEdit(record: ResourceRecord): void {
    const id = this.valueAtPath(record, this.config().idKey);
    this.editingId.set(typeof id === 'string' || typeof id === 'number' ? id : null);
    this.buildForm(record);
    this.errorMessage.set('');
    this.editorOpen.set(true);
  }

  closeEditor(): void {
    this.editorOpen.set(false);
    this.form.reset();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const payload = this.buildPayload();
    const id = this.editingId();
    this.saving.set(true);
    this.errorMessage.set('');
    const request = id === null
      ? this.api.create<ResourceRecord>(this.config().endpoint, payload)
      : this.api.update<ResourceRecord>(this.config().endpoint, id, payload);

    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.closeEditor();
        this.load();
      },
      error: error => {
        this.saving.set(false);
        this.errorMessage.set(this.errorText(error));
      },
    });
  }

  remove(record: ResourceRecord): void {
    const id = this.valueAtPath(record, this.config().idKey);
    if ((typeof id !== 'string' && typeof id !== 'number') || !window.confirm('Delete this record?')) return;
    this.errorMessage.set('');
    this.api.remove(this.config().endpoint, id).subscribe({
      next: () => this.load(),
      error: error => this.errorMessage.set(this.errorText(error)),
    });
  }

  actionAvailable(record: ResourceRecord, action: ResourceAction): boolean {
    return action.statuses.includes(String(record['status'] ?? '').toUpperCase());
  }

  runAction(record: ResourceRecord, action: ResourceAction): void {
    const id = this.valueAtPath(record, this.config().idKey);
    if (typeof id !== 'string' && typeof id !== 'number') return;
    let parameters: Record<string, string> | undefined;
    if (action.queryParameter) {
      const value = window.prompt(`Enter ${action.queryParameter}:`);
      if (value === null) return;
      const quantity = Number(value);
      if (!Number.isInteger(quantity) || quantity < 1) {
        this.errorMessage.set('Enter a whole number greater than zero.');
        return;
      }
      parameters = { [action.queryParameter]: String(quantity) };
    }
    if (['cancel', 'block'].includes(action.path) && !window.confirm(`${action.label} this record?`)) return;
    this.errorMessage.set('');
    this.api.postAction(this.config().endpoint, id, action.path, parameters).subscribe({
      next: () => this.load(),
      error: error => this.errorMessage.set(this.errorText(error)),
    });
  }

  setQuery(event: Event): void {
    this.query.set((event.target as HTMLInputElement).value);
  }

  displayValue(record: ResourceRecord, path: string): string {
    const value = this.valueAtPath(record, path);
    if (value === null || value === undefined || value === '') return '—';
    if (typeof value === 'object') return '—';
    if (path.toLowerCase().includes('price') || path.toLowerCase().includes('amount') || path.toLowerCase().includes('subtotal')) {
      const amount = Number(value);
      return Number.isFinite(amount) ? new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(amount) : String(value);
    }
    return String(value).replaceAll('_', ' ');
  }

  fieldControl(field: ResourceField): FormControl<unknown> {
    return this.form.controls[field.key];
  }

  private buildForm(record?: ResourceRecord): void {
    for (const key of Object.keys(this.form.controls)) this.form.removeControl(key);
    for (const field of this.config().fields) {
      const current = record && field.type !== 'password' ? this.valueAtPath(record, field.key) : null;
      const value = field.type === 'date' && typeof current === 'string' ? current.slice(0, 10) : current ?? '';
      const validators = field.required && (field.type !== 'password' || !record) ? [Validators.required] : [];
      if (field.type === 'email') validators.push(Validators.email);
      if (field.min !== undefined) validators.push(Validators.min(field.min));
      this.form.addControl(field.key, new FormControl<unknown>(value, validators));
    }
  }

  private buildPayload(): ResourceRecord {
    const payload: ResourceRecord = {};
    for (const field of this.config().fields) {
      let value = this.form.controls[field.key].value;
      if (field.type === 'password' && (value === '' || value === null)) continue;
      if (field.type === 'number' && value !== '' && value !== null) value = Number(value);
      if (value === '') value = null;
      this.assignPath(payload, field.key, value);
    }
    return payload;
  }

  private valueAtPath(record: ResourceRecord, path: string): unknown {
    return path.split('.').reduce<unknown>((value, part) => {
      if (typeof value !== 'object' || value === null) return undefined;
      return (value as ResourceRecord)[part];
    }, record);
  }

  private assignPath(record: ResourceRecord, path: string, value: unknown): void {
    const parts = path.split('.');
    let target = record;
    for (const part of parts.slice(0, -1)) {
      const child = target[part];
      if (typeof child !== 'object' || child === null) target[part] = {};
      target = target[part] as ResourceRecord;
    }
    target[parts.at(-1)!] = value;
  }

  private errorText(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) return 'Could not reach the inventory API. Check that the backend is running on port 8083.';
      if (typeof error.error === 'string' && error.error) return error.error;
      if (typeof error.error?.message === 'string') return error.error.message;
      return `Request failed (${error.status}). Check the record details and try again.`;
    }
    return 'Something went wrong. Please try again.';
  }
}
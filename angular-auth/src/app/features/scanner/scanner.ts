import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-scanner',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './scanner.html',
  styleUrl: './scanner.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Scanner {
  private readonly http = inject(HttpClient);

  readonly scanMode = signal<'barcode' | 'rfid'>('barcode');
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly scanResult = signal<any>(null);
  readonly rfidResult = signal<any>(null);

  readonly barcodeForm = new FormGroup({
    barcode: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  readonly rfidForm = new FormGroup({
    warehouseId: new FormControl(1, { nonNullable: true, validators: [Validators.required] }),
    operationType: new FormControl('INBOUND', { nonNullable: true }),
    referenceNumber: new FormControl('PO-RFID-2026-01', { nonNullable: true }),
    tagsInput: new FormControl('ELEC-LP-001, APP-TS-010, HOME-BL-101', { nonNullable: true, validators: [Validators.required] }),
  });

  setMode(mode: 'barcode' | 'rfid'): void {
    this.scanMode.set(mode);
    this.errorMessage.set('');
  }

  scanQuickSku(sku: string): void {
    this.barcodeForm.controls.barcode.setValue(sku);
    this.onBarcodeSubmit();
  }

  onBarcodeSubmit(): void {
    if (this.barcodeForm.invalid) return;

    this.loading.set(true);
    this.errorMessage.set('');
    this.scanResult.set(null);

    const code = this.barcodeForm.controls.barcode.value;

    this.http.get<any>(`/api/scanner/scan?barcode=${encodeURIComponent(code)}`).subscribe({
      next: data => {
        this.loading.set(false);
        this.scanResult.set(data);
      },
      error: err => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Barcode / SKU not found in catalog.');
      },
    });
  }

  onRfidSubmit(): void {
    if (this.rfidForm.invalid) return;

    this.loading.set(true);
    this.errorMessage.set('');
    this.rfidResult.set(null);

    const rawTags = this.rfidForm.controls.tagsInput.value;
    const tags = rawTags.split(',').map(t => t.trim()).filter(Boolean);

    const payload = {
      warehouseId: this.rfidForm.controls.warehouseId.value,
      operationType: this.rfidForm.controls.operationType.value,
      referenceNumber: this.rfidForm.controls.referenceNumber.value,
      tags,
    };

    this.http.post<any>('/api/scanner/rfid-batch', payload).subscribe({
      next: data => {
        this.loading.set(false);
        this.rfidResult.set(data);
      },
      error: err => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'RFID batch processing failed.');
      },
    });
  }
}

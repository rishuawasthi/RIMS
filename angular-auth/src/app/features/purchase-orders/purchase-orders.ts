import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ResourceWorkspace } from '../../shared/components/resource-workspace/resource-workspace';
import { resourceConfigs } from '../resource-config';

@Component({
  selector: 'app-purchase-orders',
  imports: [ResourceWorkspace],
  templateUrl: './purchase-orders.html',
  styleUrl: './purchase-orders.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PurchaseOrders {
  readonly config = resourceConfigs['purchase-orders'];
}

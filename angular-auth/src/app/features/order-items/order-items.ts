import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ResourceWorkspace } from '../../shared/components/resource-workspace/resource-workspace';
import { resourceConfigs } from '../resource-config';

@Component({
  selector: 'app-order-items',
  imports: [ResourceWorkspace],
  templateUrl: './order-items.html',
  styleUrl: './order-items.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OrderItems {
  readonly config = resourceConfigs['order-items'];
}
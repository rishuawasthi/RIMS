import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ResourceWorkspace } from '../../shared/components/resource-workspace/resource-workspace';
import { resourceConfigs } from '../resource-config';

@Component({
  selector: 'app-orders',
  imports: [ResourceWorkspace],
  templateUrl: './orders.html',
  styleUrl: './orders.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Orders {
  readonly config = resourceConfigs['orders'];
}

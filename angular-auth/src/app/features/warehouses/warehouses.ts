import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ResourceWorkspace } from '../../shared/components/resource-workspace/resource-workspace';
import { resourceConfigs } from '../resource-config';

@Component({
  selector: 'app-warehouses',
  imports: [ResourceWorkspace],
  templateUrl: './warehouses.html',
  styleUrl: './warehouses.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Warehouses {
  readonly config = resourceConfigs['warehouses'];
}

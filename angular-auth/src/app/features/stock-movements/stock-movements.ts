import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ResourceWorkspace } from '../../shared/components/resource-workspace/resource-workspace';
import { resourceConfigs } from '../resource-config';

@Component({
  selector: 'app-stock-movements',
  standalone: true,
  imports: [ResourceWorkspace],
  template: `<app-resource-workspace [config]="config"></app-resource-workspace>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StockMovements {
  readonly config = resourceConfigs['stock-movements'];
}

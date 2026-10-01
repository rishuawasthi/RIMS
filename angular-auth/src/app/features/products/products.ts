import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ResourceWorkspace } from '../../shared/components/resource-workspace/resource-workspace';
import { resourceConfigs } from '../resource-config';

@Component({
  selector: 'app-products',
  imports: [ResourceWorkspace],
  templateUrl: './products.html',
  styleUrl: './products.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Products {
  readonly config = resourceConfigs['products'];
}

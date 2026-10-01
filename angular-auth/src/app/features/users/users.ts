import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ResourceWorkspace } from '../../shared/components/resource-workspace/resource-workspace';
import { resourceConfigs } from '../resource-config';

@Component({
  selector: 'app-users',
  imports: [ResourceWorkspace],
  templateUrl: './users.html',
  styleUrl: './users.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Users {
  readonly config = resourceConfigs['users'];
}

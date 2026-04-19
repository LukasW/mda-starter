import { Component, ChangeDetectionStrategy } from '@angular/core';
import { AppShell } from './layout/app-shell/app-shell';

@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AppShell],
  template: `<app-shell />`,
})
export class App {}

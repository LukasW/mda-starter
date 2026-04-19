import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

interface NavEntry {
  readonly label: string;
  readonly route: string;
  readonly icon: string;
}

@Component({
  selector: 'app-shell',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './app-shell.html',
  styleUrl: './app-shell.scss',
})
export class AppShell {
  protected readonly sidenavOpen = signal(true);

  protected readonly navigation: readonly NavEntry[] = [
    { label: 'Vertraege',     route: '/vertraege',      icon: 'description' },
    { label: 'Neu erfassen',  route: '/vertraege/neu',  icon: 'note_add' },
    { label: 'Fristen',       route: '/fristen',        icon: 'event_available' },
  ];

  protected toggleSidenav(): void {
    this.sidenavOpen.update(v => !v);
  }
}

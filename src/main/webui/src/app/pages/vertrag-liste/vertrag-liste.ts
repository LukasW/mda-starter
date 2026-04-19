import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { ContractService } from '../../core/contract';
import { VertragDto } from '../../core/models';

@Component({
  selector: 'app-vertrag-liste',
  imports: [
    MatTableModule,
    MatButtonModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    RouterLink,
  ],
  templateUrl: './vertrag-liste.html',
  styleUrl: './vertrag-liste.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VertragListe {
  private readonly contract = inject(ContractService);
  readonly loading = signal(true);
  readonly items = signal<VertragDto[]>([]);
  readonly error = signal<string | null>(null);

  readonly displayedColumns = ['titel', 'typ', 'stage', 'aktionen'];

  constructor() {
    this.reload();
  }

  reload() {
    this.loading.set(true);
    this.error.set(null);
    this.contract.list().subscribe({
      next: (data) => {
        this.items.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.message ?? 'Unbekannter Fehler');
        this.loading.set(false);
      },
    });
  }
}

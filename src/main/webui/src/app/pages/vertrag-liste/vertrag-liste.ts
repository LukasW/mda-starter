import { Component, ChangeDetectionStrategy, inject, signal, computed, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { VertragService } from '../../core/vertrag';
import type { VertragDto, VertragStatus } from '../../core/models';
import type { ApiError } from '../../core/api-client';

type LoadState = 'idle' | 'loading' | 'ready' | 'error';

const STATUS_CLASS: Record<VertragStatus, string> = {
  ENTWURF: 'status-entwurf',
  IN_PRUEFUNG: 'status-pruefung',
  UEBERARBEITUNG: 'status-ueberarbeitung',
  FREIGEGEBEN: 'status-freigegeben',
  AKTIV: 'status-aktiv',
  IN_KUENDIGUNG: 'status-kuendigung',
  BEENDET: 'status-beendet',
  ABGELAUFEN: 'status-abgelaufen',
  ARCHIVIERT: 'status-archiviert',
};

@Component({
  selector: 'app-vertrag-liste',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DatePipe,
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatCardModule,
  ],
  templateUrl: './vertrag-liste.html',
  styleUrl: './vertrag-liste.scss',
})
export class VertragListe implements OnInit {
  private readonly service = inject(VertragService);

  protected readonly state = signal<LoadState>('idle');
  protected readonly vertraege = signal<VertragDto[]>([]);
  protected readonly error = signal<string | null>(null);

  protected readonly hasData = computed(() => this.vertraege().length > 0);

  protected readonly displayedColumns = [
    'titel',
    'vertragsart',
    'status',
    'startDatum',
    'endDatum',
    'versionen',
    'aktionen',
  ] as const;

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.state.set('loading');
    this.error.set(null);
    this.service.list().subscribe({
      next: (data) => {
        this.vertraege.set(data);
        this.state.set('ready');
      },
      error: (err: ApiError) => {
        this.error.set(err.message);
        this.state.set('error');
      },
    });
  }

  protected statusClass(status: VertragStatus): string {
    return STATUS_CLASS[status] ?? '';
  }
}

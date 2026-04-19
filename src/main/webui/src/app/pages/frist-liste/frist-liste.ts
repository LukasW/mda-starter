import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FristService } from '../../core/frist';
import type { ApiError } from '../../core/api-client';
import type { FristDto, FristStatus } from '../../core/models';

type LoadState = 'idle' | 'loading' | 'ready' | 'error';

function randomUuid(): string { return crypto.randomUUID(); }

@Component({
  selector: 'app-frist-liste',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatCardModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './frist-liste.html',
  styleUrl: './frist-liste.scss',
})
export class FristListe implements OnInit {
  private readonly service = inject(FristService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly state = signal<LoadState>('idle');
  protected readonly fristen = signal<FristDto[]>([]);
  protected readonly error = signal<string | null>(null);

  protected readonly displayedColumns = [
    'vertrag',
    'art',
    'faelligkeit',
    'erinnerung',
    'status',
    'aktionen',
  ] as const;

  ngOnInit(): void { this.load(); }

  protected load(): void {
    this.state.set('loading');
    this.service.list().subscribe({
      next: (data) => { this.fristen.set(data); this.state.set('ready'); },
      error: (err: ApiError) => { this.error.set(err.message); this.state.set('error'); },
    });
  }

  protected sichten(frist: FristDto): void {
    this.service.sichten(frist.fristId, randomUuid()).subscribe({
      next: () => {
        this.snackBar.open('Frist als gesichtet markiert', 'OK', { duration: 3000 });
        this.load();
      },
      error: (err: ApiError) => {
        const suffix = err.code ? ` [${err.code}]` : '';
        this.snackBar.open(`Fehler: ${err.message}${suffix}`, 'OK', {
          duration: 6000, panelClass: 'snack-error',
        });
      },
    });
  }

  protected statusClass(status: FristStatus): string {
    return `status-${status.toLowerCase()}`;
  }

  protected canSichten(status: FristStatus): boolean {
    return status === 'ERINNERT' || status === 'ESKALIERT';
  }

  protected shortId(id: string): string {
    return id.substring(0, 8) + '…';
  }
}

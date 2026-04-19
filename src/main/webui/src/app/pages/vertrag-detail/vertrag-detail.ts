import { Component, ChangeDetectionStrategy, inject, signal, computed, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { VertragService } from '../../core/vertrag';
import type { ApiError } from '../../core/api-client';
import type { VertragDto, VertragStatus } from '../../core/models';
import { AblehnenDialog } from './ablehnen-dialog';

function randomUuid(): string { return crypto.randomUUID(); }

@Component({
  selector: 'app-vertrag-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
  ],
  templateUrl: './vertrag-detail.html',
  styleUrl: './vertrag-detail.scss',
})
export class VertragDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(VertragService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);

  protected readonly vertrag = signal<VertragDto | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly canEinreichen = computed(() => {
    const status = this.vertrag()?.status;
    return status === 'ENTWURF' || status === 'UEBERARBEITUNG';
  });

  protected readonly canEntscheiden = computed(() => this.vertrag()?.status === 'IN_PRUEFUNG');

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.load(id);
  }

  protected load(id: string): void {
    this.service.byId(id).subscribe({
      next: (v) => { this.vertrag.set(v); this.error.set(null); },
      error: (err: ApiError) => this.error.set(err.message),
    });
  }

  protected einreichen(): void {
    const v = this.vertrag();
    if (!v) return;
    this.service.einreichen(v.vertragId, randomUuid()).subscribe({
      next: (updated) => {
        this.vertrag.set(updated);
        this.snackBar.open('Vertrag zur Pruefung eingereicht', 'OK', { duration: 3000 });
      },
      error: (err: ApiError) => this.notifyError(err),
    });
  }

  protected genehmigen(): void {
    const v = this.vertrag();
    if (!v) return;
    this.service.genehmigen(v.vertragId, randomUuid()).subscribe({
      next: (updated) => {
        this.vertrag.set(updated);
        this.snackBar.open('Vertrag freigegeben', 'OK', { duration: 3000 });
      },
      error: (err: ApiError) => this.notifyError(err),
    });
  }

  protected ablehnen(): void {
    const v = this.vertrag();
    if (!v) return;
    this.dialog.open(AblehnenDialog).afterClosed().subscribe((begruendung: string | undefined) => {
      if (!begruendung) return;
      this.service.ablehnen(v.vertragId, randomUuid(), begruendung).subscribe({
        next: (updated) => {
          this.vertrag.set(updated);
          this.snackBar.open('Vertrag zur Ueberarbeitung zurueckgewiesen', 'OK', { duration: 3000 });
        },
        error: (err: ApiError) => this.notifyError(err),
      });
    });
  }

  protected statusClass(status: VertragStatus | undefined): string {
    if (!status) return '';
    return `status-${status.toLowerCase()}`;
  }

  private notifyError(err: ApiError): void {
    const suffix = err.code ? ` [${err.code}]` : '';
    this.snackBar.open(`Fehler: ${err.message}${suffix}`, 'OK', {
      duration: 6000,
      panelClass: 'snack-error',
    });
  }
}

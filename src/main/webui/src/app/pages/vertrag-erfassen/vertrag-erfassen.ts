import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { VertragService } from '../../core/vertrag';
import type { ApiError } from '../../core/api-client';
import { VERTRAGSARTEN } from '../../core/models';

function randomUuid(): string {
  return crypto.randomUUID();
}

function formatDate(value: Date | null | undefined): string | null {
  if (!value) return null;
  const y = value.getFullYear();
  const m = String(value.getMonth() + 1).padStart(2, '0');
  const d = String(value.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

@Component({
  selector: 'app-vertrag-erfassen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    provideNativeDateAdapter(),
    { provide: MAT_DATE_LOCALE, useValue: 'de-CH' },
  ],
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatIconModule,
  ],
  templateUrl: './vertrag-erfassen.html',
  styleUrl: './vertrag-erfassen.scss',
})
export class VertragErfassen {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(VertragService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly vertragsarten = VERTRAGSARTEN;
  protected readonly submitting = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    titel:                  ['', [Validators.required, Validators.maxLength(255)]],
    vertragsart:            ['DIENSTLEISTUNG', [Validators.required]],
    mandantId:              [randomUuid(), [Validators.required]],
    partnerId:              [randomUuid(), [Validators.required]],
    startDatum:             this.fb.control<Date | null>(null),
    endDatum:               this.fb.control<Date | null>(null),
    kuendigungsfristTage:   this.fb.control<number | null>(null, [Validators.min(0)]),
    antragstellerId:        [randomUuid(), [Validators.required]],
  });

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    const v = this.form.getRawValue();
    this.service.erfassen({
      mandantId: v.mandantId,
      titel: v.titel,
      vertragsart: v.vertragsart as (typeof VERTRAGSARTEN)[number],
      partnerId: v.partnerId,
      startDatum: formatDate(v.startDatum),
      endDatum: formatDate(v.endDatum),
      kuendigungsfristTage: v.kuendigungsfristTage,
      antragstellerId: v.antragstellerId,
    }).subscribe({
      next: (created) => {
        this.snackBar.open(`Vertrag "${created.titel}" erfasst`, 'OK', { duration: 4000 });
        void this.router.navigate(['/vertraege', created.vertragId]);
      },
      error: (err: ApiError) => {
        this.submitting.set(false);
        this.snackBar.open(`Fehler: ${err.message}`, 'OK', { duration: 6000, panelClass: 'snack-error' });
      },
    });
  }
}

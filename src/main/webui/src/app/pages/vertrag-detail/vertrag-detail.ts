import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ContractService } from '../../core/contract';
import { VertragDto, VertragStage } from '../../core/models';

interface StageAction {
  trigger: string;
  label: string;
}

const AVAILABLE_TRIGGERS: Record<VertragStage, StageAction[]> = {
  ENTWURF: [{ trigger: 'einreichen', label: 'Zur Prüfung einreichen' }],
  IN_PRUEFUNG: [
    { trigger: 'freigeben', label: 'Freigeben' },
    { trigger: 'korrekturbeantragen', label: 'Korrektur beantragen' },
  ],
  KORREKTURBEDARF: [{ trigger: 'einreichen', label: 'Erneut einreichen' }],
  FREIGEGEBEN: [{ trigger: 'zurSignaturSenden', label: 'Zur Signatur senden' }],
  ZUR_SIGNATUR: [{ trigger: 'unterzeichnen', label: 'Unterzeichnen' }],
  UNTERZEICHNET: [{ trigger: 'archivieren', label: 'Archivieren' }],
  ARCHIVIERT: [
    { trigger: 'ablaufen', label: 'Als abgelaufen markieren' },
    { trigger: 'kuendigen', label: 'Kündigen' },
  ],
  ABGELAUFEN: [],
  GEKUENDIGT: [],
};

@Component({
  selector: 'app-vertrag-detail',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    RouterLink,
  ],
  templateUrl: './vertrag-detail.html',
  styleUrl: './vertrag-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VertragDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly contract = inject(ContractService);
  private readonly snack = inject(MatSnackBar);

  readonly loading = signal(true);
  readonly vertrag = signal<VertragDto | null>(null);
  readonly error = signal<string | null>(null);

  readonly actions = computed<StageAction[]>(() => {
    const v = this.vertrag();
    return v ? AVAILABLE_TRIGGERS[v.stage] : [];
  });

  constructor() {
    this.reload();
  }

  reload() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;
    this.loading.set(true);
    this.contract.byId(id).subscribe({
      next: (v) => {
        this.vertrag.set(v);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.message ?? 'Fehler');
        this.loading.set(false);
      },
    });
  }

  trigger(t: string) {
    const v = this.vertrag();
    if (!v) return;
    this.contract.trigger(v.id, t).subscribe({
      next: () => {
        this.snack.open(`Trigger "${t}" ausgeführt`, 'OK', { duration: 2000 });
        this.reload();
      },
      error: (err) => {
        this.snack.open(err?.message ?? 'Fehler', 'OK', { duration: 4000 });
      },
    });
  }
}

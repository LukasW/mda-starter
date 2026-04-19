import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { ContractService } from '../../core/contract';
import { VertragsTyp } from '../../core/models';

@Component({
  selector: 'app-vertrag-erfassen',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './vertrag-erfassen.html',
  styleUrl: './vertrag-erfassen.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VertragErfassen {
  private readonly fb = inject(FormBuilder);
  private readonly contract = inject(ContractService);
  private readonly snack = inject(MatSnackBar);
  private readonly router = inject(Router);

  readonly submitting = signal(false);

  readonly typen: VertragsTyp[] = [
    'LIEFERANTENVERTRAG',
    'KUNDENVERTRAG',
    'ARBEITSVERTRAG',
    'KOOPERATIONSVERTRAG',
    'SONSTIGES',
  ];

  readonly form = this.fb.nonNullable.group({
    titel: ['', [Validators.required, Validators.maxLength(200)]],
    typ: ['SONSTIGES' as VertragsTyp, [Validators.required]],
    erstellerId: ['', [Validators.required]],
  });

  submit() {
    if (this.form.invalid) {
      return;
    }
    const v = this.form.getRawValue();
    this.submitting.set(true);
    this.contract.erstellen(v.titel, v.typ, v.erstellerId).subscribe({
      next: (res) => {
        this.submitting.set(false);
        this.snack.open('Vertrag angelegt', 'OK', { duration: 2000 });
        this.router.navigate(['/vertraege', res.id]);
      },
      error: (err) => {
        this.submitting.set(false);
        this.snack.open(err?.message ?? 'Fehler', 'OK', { duration: 4000 });
      },
    });
  }
}

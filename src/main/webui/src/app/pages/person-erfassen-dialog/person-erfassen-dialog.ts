import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { PersonService } from '../../core/person';

@Component({
  selector: 'app-person-erfassen-dialog',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './person-erfassen-dialog.html',
  styleUrl: './person-erfassen-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonErfassenDialog {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(PersonService);
  private readonly ref = inject(MatDialogRef<PersonErfassenDialog>);
  readonly data = inject(MAT_DIALOG_DATA, { optional: true });

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    vorname: ['', [Validators.required, Validators.maxLength(120)]],
    nachname: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(200)]],
    organisation: [''],
    funktion: [''],
  });

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const v = this.form.getRawValue();
    this.service
      .erfassen({
        vorname: v.vorname,
        nachname: v.nachname,
        email: v.email,
        organisation: v.organisation || null,
        funktion: v.funktion || null,
      })
      .subscribe({
        next: ({ id }) => {
          this.submitting.set(false);
          this.ref.close({ id });
        },
        error: (err) => {
          this.error.set(err?.message ?? 'Fehler beim Speichern');
          this.submitting.set(false);
        },
      });
  }

  cancel() {
    this.ref.close();
  }
}

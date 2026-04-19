import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-ablehnen-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  template: `
    <h2 mat-dialog-title>Vertrag ablehnen</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Begruendung (Pflicht)</mat-label>
          <textarea matInput rows="4" formControlName="begruendung" required></textarea>
          @if (form.controls.begruendung.hasError('required') && form.controls.begruendung.touched) {
            <mat-error>Begruendung ist Pflicht.</mat-error>
          }
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="abbrechen()">Abbrechen</button>
      <button mat-flat-button color="warn"
              [disabled]="form.invalid"
              (click)="bestaetigen()">Ablehnen</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width { width: 100%; min-width: 360px; }
  `],
})
export class AblehnenDialog {
  private readonly fb = inject(FormBuilder);
  private readonly ref = inject<MatDialogRef<AblehnenDialog, string>>(MatDialogRef);

  protected readonly form = this.fb.nonNullable.group({
    begruendung: ['', [Validators.required, Validators.minLength(3)]],
  });

  protected abbrechen(): void { this.ref.close(undefined); }
  protected bestaetigen(): void {
    if (this.form.invalid) return;
    this.ref.close(this.form.controls.begruendung.value);
  }
}

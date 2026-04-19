import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PersonService } from '../../core/person';
import { PersonDto } from '../../core/models';
import { PersonLoeschenDialog } from '../person-loeschen-dialog/person-loeschen-dialog';

@Component({
  selector: 'app-person-detail',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './person-detail.html',
  styleUrl: './person-detail.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonDetail {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(PersonService);
  private readonly snack = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly person = signal<PersonDto | null>(null);
  readonly istIntern = computed(() => this.person()?.quelleTyp === 'INTERN');
  readonly istExtern = computed(() => this.person()?.quelleTyp === 'EXTERN_API');

  readonly form = this.fb.nonNullable.group({
    vorname: ['', [Validators.required, Validators.maxLength(120)]],
    nachname: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(200)]],
    organisation: [''],
    funktion: [''],
  });

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.load(id);
    } else {
      this.error.set('Ungueltige ID');
      this.loading.set(false);
    }
  }

  private load(id: string) {
    this.loading.set(true);
    this.service.byId(id).subscribe({
      next: (p) => {
        this.person.set(p);
        this.form.patchValue({
          vorname: p.vorname,
          nachname: p.nachname,
          email: p.email,
          organisation: p.organisation ?? '',
          funktion: p.funktion ?? '',
        });
        if (p.quelleTyp === 'EXTERN_API') {
          this.form.disable();
        }
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.message ?? 'Fehler beim Laden');
        this.loading.set(false);
      },
    });
  }

  save() {
    const p = this.person();
    if (!p || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    const v = this.form.getRawValue();
    this.service
      .aendern(p.id, {
        vorname: v.vorname,
        nachname: v.nachname,
        email: v.email,
        organisation: v.organisation || null,
        funktion: v.funktion || null,
        expectedVersion: p.versionNumber,
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.snack.open('Person aktualisiert.', 'OK', { duration: 3000 });
          this.load(p.id);
        },
        error: (err) => {
          this.saving.set(false);
          if (err?.code === 'MDA-PER-409') {
            this.error.set('Versions-Konflikt: Person wurde zwischenzeitlich geaendert.');
          } else if (err?.code === 'MDA-PER-002') {
            this.error.set('Diese Person ist read-only (externe Quelle).');
          } else {
            this.error.set(err?.message ?? 'Fehler beim Speichern');
          }
        },
      });
  }

  loeschen() {
    const p = this.person();
    if (!p) return;
    const ref = this.dialog.open(PersonLoeschenDialog, {
      data: { vorname: p.vorname, nachname: p.nachname },
      autoFocus: 'first-tabbable',
    });
    ref.afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.saving.set(true);
      this.service.loeschen(p.id, p.versionNumber).subscribe({
        next: () => {
          this.saving.set(false);
          this.snack.open('Person gelöscht.', 'OK', { duration: 3000 });
          this.router.navigate(['/personen']);
        },
        error: (err) => {
          this.saving.set(false);
          if (err?.code === 'MDA-PER-003') {
            this.error.set('Externe Personen können nicht gelöscht werden.');
          } else if (err?.code === 'MDA-PER-409') {
            this.error.set('Versions-Konflikt: Person wurde zwischenzeitlich geändert.');
          } else {
            this.error.set(err?.message ?? 'Fehler beim Löschen');
          }
        },
      });
    });
  }
}

import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { PersonService } from '../../core/person';
import { PersonDto } from '../../core/models';

@Component({
  selector: 'app-person-liste',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatChipsModule,
  ],
  templateUrl: './person-liste.html',
  styleUrl: './person-liste.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonListe {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(PersonService);

  readonly loading = signal(false);
  readonly results = signal<PersonDto[]>([]);
  readonly error = signal<string | null>(null);
  readonly displayedColumns = ['name', 'email', 'organisation', 'quelle'];

  readonly searchForm = this.fb.nonNullable.group({ query: [''] });

  search() {
    this.loading.set(true);
    this.error.set(null);
    this.service.search(this.searchForm.getRawValue().query, 20).subscribe({
      next: (data) => {
        this.results.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.message ?? 'Fehler');
        this.loading.set(false);
      },
    });
  }
}

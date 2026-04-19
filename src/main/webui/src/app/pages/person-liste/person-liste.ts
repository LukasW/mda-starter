import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { PersonService } from '../../core/person';
import { PersonDto } from '../../core/models';
import { PersonErfassenDialog } from '../person-erfassen-dialog/person-erfassen-dialog';

const UUID_RE = /^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{12}$/i;

@Component({
  selector: 'app-person-liste',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatTableModule,
  ],
  templateUrl: './person-liste.html',
  styleUrl: './person-liste.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonListe implements AfterViewInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(PersonService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly displayedColumns = ['name', 'email', 'organisation', 'quelle'];
  readonly dataSource = new MatTableDataSource<PersonDto>([]);
  readonly hasResults = computed(() => this.dataSource.data.length > 0);

  @ViewChild(MatPaginator) paginator?: MatPaginator;

  readonly searchForm = this.fb.nonNullable.group({
    query: ['', [this.kein_uuid_validator]],
  });

  constructor() {
    this.search();
  }

  ngAfterViewInit() {
    if (this.paginator) {
      this.dataSource.paginator = this.paginator;
    }
  }

  private kein_uuid_validator(control: { value: string }) {
    const v = (control.value ?? '').trim();
    if (v && UUID_RE.test(v)) {
      return { uuidNichtErlaubt: true };
    }
    return null;
  }

  search() {
    if (this.searchForm.invalid) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.service.search(this.searchForm.getRawValue().query, 200).subscribe({
      next: (data) => {
        this.dataSource.data = data;
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.message ?? 'Fehler');
        this.loading.set(false);
      },
    });
  }

  openErfassen() {
    const ref = this.dialog.open(PersonErfassenDialog, { autoFocus: 'first-tabbable' });
    ref.afterClosed().subscribe((result) => {
      if (result?.id) {
        this.search();
      }
    });
  }

  oeffnen(person: PersonDto) {
    this.router.navigate(['/personen', person.id]);
  }
}

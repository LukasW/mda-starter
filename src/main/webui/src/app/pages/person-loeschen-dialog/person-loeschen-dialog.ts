import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export interface PersonLoeschenDialogData {
  vorname: string;
  nachname: string;
}

@Component({
  selector: 'app-person-loeschen-dialog',
  imports: [MatButtonModule, MatDialogModule, MatIconModule],
  templateUrl: './person-loeschen-dialog.html',
  styleUrl: './person-loeschen-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonLoeschenDialog {
  readonly data = inject<PersonLoeschenDialogData>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<PersonLoeschenDialog>);

  bestaetigen() {
    this.ref.close(true);
  }

  abbrechen() {
    this.ref.close(false);
  }
}

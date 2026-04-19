import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'vertraege',
  },
  {
    path: 'vertraege',
    loadComponent: () =>
      import('./pages/vertrag-liste/vertrag-liste').then(m => m.VertragListe),
    title: 'Vertraege — CLM',
  },
  {
    path: 'vertraege/neu',
    loadComponent: () =>
      import('./pages/vertrag-erfassen/vertrag-erfassen').then(m => m.VertragErfassen),
    title: 'Neuer Vertrag — CLM',
  },
  {
    path: 'vertraege/:id',
    loadComponent: () =>
      import('./pages/vertrag-detail/vertrag-detail').then(m => m.VertragDetail),
    title: 'Vertragsdetails — CLM',
  },
  {
    path: 'fristen',
    loadComponent: () =>
      import('./pages/frist-liste/frist-liste').then(m => m.FristListe),
    title: 'Fristen — CLM',
  },
  {
    path: '**',
    redirectTo: 'vertraege',
  },
];

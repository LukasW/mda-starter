import { Routes } from '@angular/router';
import { AppShell } from './layout/app-shell/app-shell';

export const routes: Routes = [
  {
    path: '',
    component: AppShell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'vertraege' },
      {
        path: 'vertraege',
        loadComponent: () => import('./pages/vertrag-liste/vertrag-liste').then((m) => m.VertragListe),
      },
      {
        path: 'vertraege/neu',
        loadComponent: () => import('./pages/vertrag-erfassen/vertrag-erfassen').then((m) => m.VertragErfassen),
      },
      {
        path: 'vertraege/:id',
        loadComponent: () => import('./pages/vertrag-detail/vertrag-detail').then((m) => m.VertragDetail),
      },
      {
        path: 'personen',
        loadComponent: () => import('./pages/person-liste/person-liste').then((m) => m.PersonListe),
      },
      {
        path: 'personen/:id',
        loadComponent: () =>
          import('./pages/person-detail/person-detail').then((m) => m.PersonDetail),
      },
    ],
  },
];

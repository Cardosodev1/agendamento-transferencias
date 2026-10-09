import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    title: 'Agendamento de Transferências',
    loadComponent: () =>
      import('./transferencias/transferencias-page').then((m) => m.TransferenciasPage),
  },
  { path: '**', redirectTo: '' },
];

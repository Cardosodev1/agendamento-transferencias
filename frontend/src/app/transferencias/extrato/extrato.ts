import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { Transferencia } from '../transferencia.model';

@Component({
  selector: 'app-extrato',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './extrato.html',
})
export class Extrato {
  readonly transferencias = input.required<Transferencia[]>();
  readonly carregando = input(false);
}

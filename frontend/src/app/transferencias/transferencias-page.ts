import { Component, inject, OnInit, signal } from '@angular/core';
import { mensagemDeErro } from '../core/mensagem-erro';
import { AgendamentoForm } from './agendamento-form/agendamento-form';
import { Extrato } from './extrato/extrato';
import { Transferencia } from './transferencia.model';
import { TransferenciaService } from './transferencia.service';

@Component({
  selector: 'app-transferencias-page',
  imports: [AgendamentoForm, Extrato],
  template: `
    <div class="row g-4">
      <div class="col-12 col-xl-5">
        <app-agendamento-form (agendada)="carregarExtrato()" />
      </div>
      <div class="col-12 col-xl-7">
        @if (erro(); as mensagem) {
          <div class="alert alert-danger" role="alert">{{ mensagem }}</div>
        }
        <app-extrato [transferencias]="transferencias()" [carregando]="carregando()" />
      </div>
    </div>
  `,
})
export class TransferenciasPage implements OnInit {
  private readonly service = inject(TransferenciaService);

  protected readonly transferencias = signal<Transferencia[]>([]);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    this.carregarExtrato();
  }

  protected carregarExtrato(): void {
    this.carregando.set(true);
    this.erro.set(null);

    this.service.listarExtrato().subscribe({
      next: (transferencias) => {
        this.transferencias.set(transferencias);
        this.carregando.set(false);
      },
      error: (erro) => {
        this.erro.set(mensagemDeErro(erro));
        this.carregando.set(false);
      },
    });
  }
}

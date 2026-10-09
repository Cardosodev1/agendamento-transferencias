import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  map,
  Observable,
  of,
  startWith,
  switchMap,
} from 'rxjs';
import { hojeIso } from '../../core/datas';
import { mensagemDeErro } from '../../core/mensagem-erro';
import { Transferencia } from '../transferencia.model';
import { TransferenciaService } from '../transferencia.service';
import { contasDiferentes, maximoDuasCasasDecimais, PADRAO_CONTA } from './validadores';

export const ATRASO_SIMULACAO_MS = 300;

export type Simulacao =
  | { estado: 'aguardando' }
  | { estado: 'calculando' }
  | { estado: 'calculada'; taxa: number }
  | { estado: 'indisponivel'; mensagem: string };

@Component({
  selector: 'app-agendamento-form',
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  templateUrl: './agendamento-form.html',
})
export class AgendamentoForm {
  private readonly service = inject(TransferenciaService);

  readonly agendada = output<Transferencia>();

  protected readonly hoje = hojeIso();

  protected readonly form = inject(FormBuilder).nonNullable.group(
    {
      contaOrigem: ['', [Validators.required, Validators.pattern(PADRAO_CONTA)]],
      contaDestino: ['', [Validators.required, Validators.pattern(PADRAO_CONTA)]],
      valor: [
        null as number | null,
        [Validators.required, Validators.min(0.01), maximoDuasCasasDecimais],
      ],
      dataTransferencia: [this.hoje, Validators.required],
    },
    { validators: contasDiferentes('contaOrigem', 'contaDestino') },
  );

  protected readonly simulacao = toSignal(this.simularAoAlterar(), {
    initialValue: { estado: 'aguardando' } as Simulacao,
  });

  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly sucesso = signal<Transferencia | null>(null);

  protected readonly podeAgendar = computed(
    () => !this.enviando() && this.simulacao().estado !== 'indisponivel',
  );

  protected agendar(): void {
    this.erro.set(null);
    this.sucesso.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { contaOrigem, contaDestino, valor, dataTransferencia } = this.form.getRawValue();
    this.enviando.set(true);

    this.service
      .agendar({ contaOrigem, contaDestino, valor: valor!, dataTransferencia })
      .subscribe({
        next: (transferencia) => {
          this.enviando.set(false);
          this.sucesso.set(transferencia);
          this.form.reset();
          this.agendada.emit(transferencia);
        },
        error: (erro) => {
          this.enviando.set(false);
          this.erro.set(mensagemDeErro(erro));
        },
      });
  }

  protected invalido(campo: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[campo];
    return control.invalid && (control.touched || control.dirty);
  }

  protected contasIguais(): boolean {
    return this.form.hasError('contasIguais') && this.form.controls.contaDestino.dirty;
  }

  private simularAoAlterar(): Observable<Simulacao> {
    const { valor, dataTransferencia } = this.form.controls;

    return this.form.valueChanges.pipe(
      startWith(this.form.value),
      map(() => ({ valor: valor.valid ? valor.value : null, data: dataTransferencia.value })),
      distinctUntilChanged((a, b) => a.valor === b.valor && a.data === b.data),
      debounceTime(ATRASO_SIMULACAO_MS),
      switchMap(({ valor, data }) => {
        if (valor === null || !data) {
          return of<Simulacao>({ estado: 'aguardando' });
        }
        return this.service.simularTaxa(valor, data).pipe(
          map(({ taxa }): Simulacao => ({ estado: 'calculada', taxa })),
          catchError((erro) =>
            of<Simulacao>({ estado: 'indisponivel', mensagem: mensagemDeErro(erro) }),
          ),
          startWith<Simulacao>({ estado: 'calculando' }),
        );
      }),
    );
  }
}

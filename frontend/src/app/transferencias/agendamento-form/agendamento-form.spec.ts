import { HttpErrorResponse } from '@angular/common/http';
import { DEFAULT_CURRENCY_CODE, LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { SimulacaoTaxa, Transferencia } from '../transferencia.model';
import { TransferenciaService } from '../transferencia.service';
import { AgendamentoForm, ATRASO_SIMULACAO_MS } from './agendamento-form';

registerLocaleData(localePt);

const TRANSFERENCIA: Transferencia = {
  id: 1,
  contaOrigem: '1234567890',
  contaDestino: '0987654321',
  valor: 1000,
  taxa: 12,
  dataTransferencia: '2026-10-12',
  dataAgendamento: '2026-10-08',
};

describe('AgendamentoForm', () => {
  let fixture: ComponentFixture<AgendamentoForm>;
  let elemento: HTMLElement;
  let service: {
    agendar: ReturnType<typeof vi.fn>;
    simularTaxa: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    vi.useFakeTimers();
    service = {
      agendar: vi.fn(),
      simularTaxa: vi.fn().mockReturnValue(of<SimulacaoTaxa>({ taxa: 12 })),
    };

    await TestBed.configureTestingModule({
      imports: [AgendamentoForm],
      providers: [
        { provide: TransferenciaService, useValue: service },
        { provide: LOCALE_ID, useValue: 'pt-BR' },
        { provide: DEFAULT_CURRENCY_CODE, useValue: 'BRL' },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AgendamentoForm);
    elemento = fixture.nativeElement;
    fixture.detectChanges();
  });

  afterEach(() => vi.useRealTimers());

  function preencher(campo: string, valor: string): void {
    const input = elemento.querySelector<HTMLInputElement>(`#${campo}`)!;
    input.value = valor;
    input.dispatchEvent(new Event('input'));
  }

  function preencherFormularioValido(): void {
    preencher('contaOrigem', '1234567890');
    preencher('contaDestino', '0987654321');
    preencher('valor', '1000');
    preencher('dataTransferencia', '2026-10-12');
  }

  async function aguardarSimulacao(): Promise<void> {
    await vi.advanceTimersByTimeAsync(ATRASO_SIMULACAO_MS);
    fixture.detectChanges();
  }

  function submeter(): void {
    elemento.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('deve exibir a taxa calculada pela API', async () => {
    preencherFormularioValido();
    await aguardarSimulacao();

    expect(service.simularTaxa).toHaveBeenLastCalledWith(1000, '2026-10-12');
    expect(elemento.querySelector('[data-testid="taxa"]')?.textContent).toContain('12,00');
  });

  it('deve alertar e bloquear o agendamento quando nao ha taxa aplicavel', async () => {
    const mensagem =
      'Não há taxa aplicável para transferências agendadas com 60 dia(s) de antecedência.';
    service.simularTaxa.mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 422, error: { status: 422, mensagem, campos: [] } }),
      ),
    );

    preencherFormularioValido();
    await aguardarSimulacao();

    expect(elemento.querySelector('[data-testid="taxa-indisponivel"]')?.textContent).toContain(
      mensagem,
    );
    expect(elemento.querySelector<HTMLButtonElement>('button[type="submit"]')!.disabled).toBe(true);
  });

  it('nao deve enviar formulario invalido', () => {
    preencher('contaOrigem', '123');
    submeter();

    expect(service.agendar).not.toHaveBeenCalled();
    expect(elemento.querySelector('#contaOrigem')!.classList).toContain('is-invalid');
  });

  it('deve acusar contas de origem e destino iguais', () => {
    preencher('contaOrigem', '1234567890');
    preencher('contaDestino', '1234567890');
    fixture.detectChanges();

    expect(elemento.querySelector('#contaDestino')!.classList).toContain('is-invalid');
  });

  it('deve agendar, emitir o evento e limpar o formulario', async () => {
    const resposta = new Subject<Transferencia>();
    service.agendar.mockReturnValue(resposta);
    const emitidas: Transferencia[] = [];
    fixture.componentInstance.agendada.subscribe((t) => emitidas.push(t));

    preencherFormularioValido();
    await aguardarSimulacao();
    submeter();

    expect(service.agendar).toHaveBeenCalledWith({
      contaOrigem: '1234567890',
      contaDestino: '0987654321',
      valor: 1000,
      dataTransferencia: '2026-10-12',
    });

    resposta.next(TRANSFERENCIA);
    resposta.complete();
    fixture.detectChanges();

    expect(emitidas).toEqual([TRANSFERENCIA]);
    expect(elemento.querySelector('.alert-success')?.textContent).toContain('12/10/2026');
    expect(elemento.querySelector<HTMLInputElement>('#contaOrigem')!.value).toBe('');
  });

  it('deve exibir a mensagem de erro retornada pela API', async () => {
    service.agendar.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 422,
            error: { status: 422, mensagem: 'A conta de destino deve ser diferente.', campos: [] },
          }),
      ),
    );

    preencherFormularioValido();
    await aguardarSimulacao();
    submeter();

    expect(elemento.querySelector('.alert-danger')?.textContent).toContain(
      'A conta de destino deve ser diferente.',
    );
  });
});

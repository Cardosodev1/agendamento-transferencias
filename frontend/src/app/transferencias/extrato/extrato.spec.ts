import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { DEFAULT_CURRENCY_CODE, LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Extrato } from './extrato';

registerLocaleData(localePt);

describe('Extrato', () => {
  let fixture: ComponentFixture<Extrato>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Extrato],
      providers: [
        { provide: LOCALE_ID, useValue: 'pt-BR' },
        { provide: DEFAULT_CURRENCY_CODE, useValue: 'BRL' },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(Extrato);
  });

  it('deve informar quando nao ha agendamentos', () => {
    fixture.componentRef.setInput('transferencias', []);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Nenhuma transferência agendada');
  });

  it('deve listar os agendamentos formatados', () => {
    fixture.componentRef.setInput('transferencias', [
      {
        id: 7,
        contaOrigem: '1234567890',
        contaDestino: '0987654321',
        valor: 1500.5,
        taxa: 126.04,
        dataTransferencia: '2026-10-20',
        dataAgendamento: '2026-10-08',
      },
    ]);
    fixture.detectChanges();

    const celulas = [...fixture.nativeElement.querySelectorAll('tbody td')].map((td: Element) =>
      td.textContent!.replace(/\s/g, ' ').trim(),
    );
    expect(celulas).toEqual([
      '7',
      '1234567890',
      '0987654321',
      'R$ 1.500,50',
      'R$ 126,04',
      '20/10/2026',
      '08/10/2026',
    ]);
  });
});

import { HttpErrorResponse } from '@angular/common/http';
import { mensagemDeErro } from './mensagem-erro';

describe('mensagemDeErro', () => {
  it('deve usar a mensagem de regra de negocio retornada pela API', () => {
    const erro = new HttpErrorResponse({
      status: 422,
      error: { status: 422, mensagem: 'Não há taxa aplicável.', campos: [] },
    });

    expect(mensagemDeErro(erro)).toBe('Não há taxa aplicável.');
  });

  it('deve detalhar os campos invalidos', () => {
    const erro = new HttpErrorResponse({
      status: 400,
      error: {
        status: 400,
        mensagem: 'Dados inválidos.',
        campos: [{ campo: 'contaOrigem', mensagem: 'deve conter exatamente 10 dígitos' }],
      },
    });

    expect(mensagemDeErro(erro)).toBe(
      'Dados inválidos. Conta de origem deve conter exatamente 10 dígitos.',
    );
  });

  it('deve informar quando a API esta fora do ar', () => {
    expect(mensagemDeErro(new HttpErrorResponse({ status: 0 }))).toContain(
      'Não foi possível conectar',
    );
  });

  it('deve usar mensagem generica para erros desconhecidos', () => {
    expect(mensagemDeErro(new Error('x'))).toBe('Erro inesperado. Tente novamente.');
    expect(mensagemDeErro(new HttpErrorResponse({ status: 502, error: '<html>' }))).toBe(
      'Erro inesperado. Tente novamente.',
    );
  });
});

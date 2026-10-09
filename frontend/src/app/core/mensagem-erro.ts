import { HttpErrorResponse } from '@angular/common/http';

/** Corpo de erro padronizado retornado pela API (ErroResponse). */
export interface ErroApi {
  status: number;
  mensagem: string;
  campos: { campo: string; mensagem: string }[];
}

const ROTULOS_CAMPOS: Record<string, string> = {
  contaOrigem: 'Conta de origem',
  contaDestino: 'Conta de destino',
  valor: 'Valor',
  dataTransferencia: 'Data da transferência',
};

export function mensagemDeErro(erro: unknown): string {
  if (!(erro instanceof HttpErrorResponse)) {
    return 'Erro inesperado. Tente novamente.';
  }
  if (erro.status === 0) {
    return 'Não foi possível conectar ao servidor. Verifique se a API está em execução.';
  }

  const corpo = erro.error as Partial<ErroApi> | null;
  if (!corpo?.mensagem) {
    return 'Erro inesperado. Tente novamente.';
  }

  const detalhes = (corpo.campos ?? [])
    .map(({ campo, mensagem }) => `${ROTULOS_CAMPOS[campo] ?? campo} ${mensagem}`)
    .join('; ');

  return detalhes ? `${corpo.mensagem} ${detalhes}.` : corpo.mensagem;
}

/** Datas no formato ISO (yyyy-MM-dd). */
export interface Transferencia {
  id: number;
  contaOrigem: string;
  contaDestino: string;
  valor: number;
  taxa: number;
  dataTransferencia: string;
  dataAgendamento: string;
}

export interface AgendamentoRequest {
  contaOrigem: string;
  contaDestino: string;
  valor: number;
  dataTransferencia: string;
}

export interface SimulacaoTaxa {
  taxa: number;
}

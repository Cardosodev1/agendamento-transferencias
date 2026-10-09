package br.com.agendamento.transferencias.exception;

public class TaxaNaoAplicavelException extends RegraNegocioException {

	public TaxaNaoAplicavelException(long dias) {
		super(String.format("Não há taxa aplicável para transferências agendadas com %d dia(s) de antecedência. "
				+ "Escolha uma data entre hoje e 50 dias à frente.", dias));
	}

}

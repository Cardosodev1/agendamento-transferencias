package br.com.agendamento.transferencias.dto;

import java.math.BigDecimal;

public class SimulacaoTaxaResponse {

	private final BigDecimal taxa;

	public SimulacaoTaxaResponse(BigDecimal taxa) {
		this.taxa = taxa;
	}

	public BigDecimal getTaxa() {
		return taxa;
	}

}

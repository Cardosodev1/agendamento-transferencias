package br.com.agendamento.transferencias.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.agendamento.transferencias.entity.Transferencia;

public class TransferenciaResponse {

	private final Long id;
	private final String contaOrigem;
	private final String contaDestino;
	private final BigDecimal valor;
	private final BigDecimal taxa;
	private final LocalDate dataTransferencia;
	private final LocalDate dataAgendamento;

	public TransferenciaResponse(Transferencia transferencia) {
		this.id = transferencia.getId();
		this.contaOrigem = transferencia.getContaOrigem();
		this.contaDestino = transferencia.getContaDestino();
		this.valor = transferencia.getValor();
		this.taxa = transferencia.getTaxa();
		this.dataTransferencia = transferencia.getDataTransferencia();
		this.dataAgendamento = transferencia.getDataAgendamento();
	}

	public Long getId() {
		return id;
	}

	public String getContaOrigem() {
		return contaOrigem;
	}

	public String getContaDestino() {
		return contaDestino;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public BigDecimal getTaxa() {
		return taxa;
	}

	public LocalDate getDataTransferencia() {
		return dataTransferencia;
	}

	public LocalDate getDataAgendamento() {
		return dataAgendamento;
	}

}

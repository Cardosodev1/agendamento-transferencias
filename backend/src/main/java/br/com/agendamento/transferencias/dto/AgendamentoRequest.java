package br.com.agendamento.transferencias.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class AgendamentoRequest {

	private static final String PADRAO_CONTA = "\\d{10}";
	private static final String MENSAGEM_CONTA = "deve conter exatamente 10 dígitos";
	private static final String OBRIGATORIO = "é obrigatório";

	@NotNull(message = OBRIGATORIO)
	@Pattern(regexp = PADRAO_CONTA, message = MENSAGEM_CONTA)
	private String contaOrigem;

	@NotNull(message = OBRIGATORIO)
	@Pattern(regexp = PADRAO_CONTA, message = MENSAGEM_CONTA)
	private String contaDestino;

	@NotNull(message = OBRIGATORIO)
	@DecimalMin(value = "0.01", message = "deve ser maior que zero")
	@Digits(integer = 13, fraction = 2, message = "deve ter no máximo 2 casas decimais")
	private BigDecimal valor;

	@NotNull(message = OBRIGATORIO)
	private LocalDate dataTransferencia;

	public AgendamentoRequest() {
	}

	public AgendamentoRequest(String contaOrigem, String contaDestino, BigDecimal valor, LocalDate dataTransferencia) {
		this.contaOrigem = contaOrigem;
		this.contaDestino = contaDestino;
		this.valor = valor;
		this.dataTransferencia = dataTransferencia;
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

	public LocalDate getDataTransferencia() {
		return dataTransferencia;
	}

}

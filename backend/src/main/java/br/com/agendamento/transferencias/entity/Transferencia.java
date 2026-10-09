package br.com.agendamento.transferencias.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "transferencia")
public class Transferencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "conta_origem", nullable = false, length = 10)
	private String contaOrigem;

	@Column(name = "conta_destino", nullable = false, length = 10)
	private String contaDestino;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal valor;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal taxa;

	@Column(name = "data_transferencia", nullable = false)
	private LocalDate dataTransferencia;

	@Column(name = "data_agendamento", nullable = false)
	private LocalDate dataAgendamento;

	public Transferencia() {
	}

	public Transferencia(String contaOrigem, String contaDestino, BigDecimal valor, BigDecimal taxa, LocalDate dataTransferencia, LocalDate dataAgendamento) {
		this.contaOrigem = contaOrigem;
		this.contaDestino = contaDestino;
		this.valor = valor;
		this.taxa = taxa;
		this.dataTransferencia = dataTransferencia;
		this.dataAgendamento = dataAgendamento;
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

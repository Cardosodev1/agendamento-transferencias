package br.com.agendamento.transferencias.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agendamento.transferencias.domain.CalculadoraTaxa;
import br.com.agendamento.transferencias.dto.AgendamentoRequest;
import br.com.agendamento.transferencias.entity.Transferencia;
import br.com.agendamento.transferencias.exception.RegraNegocioException;
import br.com.agendamento.transferencias.repository.TransferenciaRepository;

@Service
public class TransferenciaService {

	private final TransferenciaRepository repository;
	private final CalculadoraTaxa calculadoraTaxa;
	private final Clock clock;

	public TransferenciaService(TransferenciaRepository repository, CalculadoraTaxa calculadoraTaxa, Clock clock) {
		this.repository = repository;
		this.calculadoraTaxa = calculadoraTaxa;
		this.clock = clock;
	}

	@Transactional
	public Transferencia agendar(AgendamentoRequest request) {
		if (request.getContaOrigem().equals(request.getContaDestino())) {
			throw new RegraNegocioException("A conta de destino deve ser diferente da conta de origem.");
		}

		LocalDate hoje = LocalDate.now(clock);
		BigDecimal valor = request.getValor().setScale(2, RoundingMode.HALF_UP);
		BigDecimal taxa = calculadoraTaxa.calcular(valor, hoje, request.getDataTransferencia());

		return repository.save(new Transferencia(request.getContaOrigem(), request.getContaDestino(), valor, taxa, request.getDataTransferencia(), hoje));
	}

	@Transactional(readOnly = true)
	public List<Transferencia> listarExtrato() {
		return repository.findAllByOrderByDataAgendamentoDescIdDesc();
	}

	public BigDecimal simularTaxa(BigDecimal valor, LocalDate dataTransferencia) {
		return calculadoraTaxa.calcular(valor, LocalDate.now(clock), dataTransferencia);
	}

}

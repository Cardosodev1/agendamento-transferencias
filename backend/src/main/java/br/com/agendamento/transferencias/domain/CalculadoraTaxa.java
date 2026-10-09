package br.com.agendamento.transferencias.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import br.com.agendamento.transferencias.exception.TaxaNaoAplicavelException;

@Component
public class CalculadoraTaxa {

	public BigDecimal calcular(BigDecimal valor, LocalDate dataAgendamento, LocalDate dataTransferencia) {
		long dias = ChronoUnit.DAYS.between(dataAgendamento, dataTransferencia);

		return FaixaTaxa.paraDias(dias)
				.map(faixa -> faixa.calcular(valor))
				.map(taxa -> taxa.setScale(2, RoundingMode.HALF_UP))
				.orElseThrow(() -> new TaxaNaoAplicavelException(dias));
	}

}

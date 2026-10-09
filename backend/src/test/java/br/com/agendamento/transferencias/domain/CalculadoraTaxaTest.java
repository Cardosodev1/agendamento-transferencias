package br.com.agendamento.transferencias.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.agendamento.transferencias.exception.TaxaNaoAplicavelException;

class CalculadoraTaxaTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 10, 7);
	private static final BigDecimal MIL = new BigDecimal("1000.00");

	private final CalculadoraTaxa calculadora = new CalculadoraTaxa();

	@ParameterizedTest(name = "{0} dia(s) -> taxa R$ {1}")
	@CsvSource({
			"0,  28.00",  // R$ 3,00 + 2,5%
			"1,  12.00",  // R$ 12,00 + 0%
			"10, 12.00",
			"11, 82.00",  // 8,2%
			"20, 82.00",
			"21, 69.00",  // 6,9%
			"30, 69.00",
			"31, 47.00",  // 4,7%
			"40, 47.00",
			"41, 17.00",  // 1,7%
			"50, 17.00"
	})
	void deveCalcularTaxaConformeFaixaDeDias(int dias, String taxaEsperada) {
		BigDecimal taxa = calculadora.calcular(MIL, HOJE, HOJE.plusDays(dias));

		assertThat(taxa).isEqualByComparingTo(taxaEsperada);
	}

	@Test
	void deveArredondarTaxaParaDuasCasasComHalfUp() {
		// R$ 3,00 + 2,5% de R$ 1,00 = R$ 3,025
		BigDecimal taxa = calculadora.calcular(new BigDecimal("1.00"), HOJE, HOJE);

		assertThat(taxa).isEqualTo(new BigDecimal("3.03"));
	}

	@Test
	void deveConsiderarDiasCorridosEntreMeses() {
		// 30/09 -> 01/11 = 32 dias (faixa de 4,7%)
		BigDecimal taxa = calculadora.calcular(MIL, LocalDate.of(2026, 9, 30), LocalDate.of(2026, 11, 1));

		assertThat(taxa).isEqualByComparingTo("47.00");
	}

	@ParameterizedTest(name = "{0} dia(s) -> sem taxa aplicavel")
	@ValueSource(ints = { -1, -30, 51, 365 })
	void deveRecusarQuandoNaoHaTaxaAplicavel(int dias) {
		assertThatThrownBy(() -> calculadora.calcular(MIL, HOJE, HOJE.plusDays(dias)))
				.isInstanceOf(TaxaNaoAplicavelException.class)
				.hasMessageContaining(dias + " dia(s)");
	}

}

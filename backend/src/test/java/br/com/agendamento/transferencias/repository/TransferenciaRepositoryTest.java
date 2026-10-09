package br.com.agendamento.transferencias.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import br.com.agendamento.transferencias.entity.Transferencia;

@DataJpaTest
class TransferenciaRepositoryTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 10, 7);

	@Autowired
	private TransferenciaRepository repository;

	@Test
	void devePersistirTodosOsCampos() {
		Transferencia salva = repository.saveAndFlush(novaTransferencia(HOJE, "1500.50"));

		Transferencia encontrada = repository.findById(salva.getId()).orElseThrow();

		assertThat(encontrada.getContaOrigem()).isEqualTo("1234567890");
		assertThat(encontrada.getContaDestino()).isEqualTo("0987654321");
		assertThat(encontrada.getValor()).isEqualByComparingTo("1500.50");
		assertThat(encontrada.getTaxa()).isEqualByComparingTo("12.00");
		assertThat(encontrada.getDataTransferencia()).isEqualTo(HOJE.plusDays(5));
		assertThat(encontrada.getDataAgendamento()).isEqualTo(HOJE);
	}

	@Test
	void deveListarExtratoDoAgendamentoMaisRecenteParaOMaisAntigo() {
		Transferencia antiga = repository.save(novaTransferencia(HOJE.minusDays(2), "100.00"));
		Transferencia recente1 = repository.save(novaTransferencia(HOJE, "200.00"));
		Transferencia recente2 = repository.save(novaTransferencia(HOJE, "300.00"));

		List<Transferencia> extrato = repository.findAllByOrderByDataAgendamentoDescIdDesc();

		assertThat(extrato).containsExactly(recente2, recente1, antiga);
	}

	private Transferencia novaTransferencia(LocalDate dataAgendamento, String valor) {
		return new Transferencia("1234567890", "0987654321", new BigDecimal(valor), new BigDecimal("12.00"),
				dataAgendamento.plusDays(5), dataAgendamento);
	}

}

package br.com.agendamento.transferencias.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.agendamento.transferencias.domain.CalculadoraTaxa;
import br.com.agendamento.transferencias.dto.AgendamentoRequest;
import br.com.agendamento.transferencias.entity.Transferencia;
import br.com.agendamento.transferencias.exception.RegraNegocioException;
import br.com.agendamento.transferencias.exception.TaxaNaoAplicavelException;
import br.com.agendamento.transferencias.repository.TransferenciaRepository;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

	private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
	private static final LocalDate HOJE = LocalDate.of(2026, 10, 7);
	private static final String ORIGEM = "1234567890";
	private static final String DESTINO = "0987654321";
	private static final BigDecimal MIL = new BigDecimal("1000.00");

	@Mock
	private TransferenciaRepository repository;

	private TransferenciaService service;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(HOJE.atStartOfDay(SAO_PAULO).toInstant(), SAO_PAULO);
		service = new TransferenciaService(repository, new CalculadoraTaxa(), clock);
	}

	@Test
	void deveAgendarComDataDeHojeETaxaCalculada() {
		when(repository.save(any(Transferencia.class))).thenAnswer(inv -> inv.getArgument(0));

		Transferencia transferencia = service.agendar(request(ORIGEM, DESTINO, MIL, HOJE.plusDays(15)));

		assertThat(transferencia.getContaOrigem()).isEqualTo(ORIGEM);
		assertThat(transferencia.getContaDestino()).isEqualTo(DESTINO);
		assertThat(transferencia.getValor()).isEqualByComparingTo("1000.00");
		assertThat(transferencia.getTaxa()).isEqualByComparingTo("82.00");
		assertThat(transferencia.getDataTransferencia()).isEqualTo(HOJE.plusDays(15));
		assertThat(transferencia.getDataAgendamento()).isEqualTo(HOJE);
		verify(repository).save(transferencia);
	}

	@Test
	void deveNormalizarValorParaDuasCasasDecimais() {
		when(repository.save(any(Transferencia.class))).thenAnswer(inv -> inv.getArgument(0));

		Transferencia transferencia = service.agendar(
				request(ORIGEM, DESTINO, new BigDecimal("1000"), HOJE.plusDays(5)));

		assertThat(transferencia.getValor()).isEqualTo(new BigDecimal("1000.00"));
	}

	@Test
	void deveUsarDataDeHojeNoFusoDeSaoPaulo() {
		// 02:00 UTC de 08/10 ainda e 23:00 de 07/10 em Sao Paulo
		Clock clockUtc = Clock.fixed(Instant.parse("2026-10-08T02:00:00Z"), SAO_PAULO);
		service = new TransferenciaService(repository, new CalculadoraTaxa(), clockUtc);
		when(repository.save(any(Transferencia.class))).thenAnswer(inv -> inv.getArgument(0));

		Transferencia transferencia = service.agendar(request(ORIGEM, DESTINO, MIL, HOJE));

		assertThat(transferencia.getDataAgendamento()).isEqualTo(HOJE);
		assertThat(transferencia.getTaxa()).isEqualByComparingTo("28.00");
	}

	@Test
	void naoDeveSalvarQuandoNaoHaTaxaAplicavel() {
		assertThatThrownBy(() -> service.agendar(request(ORIGEM, DESTINO, MIL, HOJE.plusDays(51))))
				.isInstanceOf(TaxaNaoAplicavelException.class);

		verify(repository, never()).save(any());
	}

	@Test
	void naoDeveSalvarQuandoDataEstaNoPassado() {
		assertThatThrownBy(() -> service.agendar(request(ORIGEM, DESTINO, MIL, HOJE.minusDays(1))))
				.isInstanceOf(TaxaNaoAplicavelException.class);

		verify(repository, never()).save(any());
	}

	@Test
	void naoDeveAgendarParaAMesmaConta() {
		assertThatThrownBy(() -> service.agendar(request(ORIGEM, ORIGEM, MIL, HOJE)))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessage("A conta de destino deve ser diferente da conta de origem.");

		verify(repository, never()).save(any());
	}

	@Test
	void deveSimularTaxaSemPersistir() {
		assertThat(service.simularTaxa(MIL, HOJE.plusDays(25))).isEqualByComparingTo("69.00");

		verify(repository, never()).save(any());
	}

	@Test
	void deveListarExtratoDoRepositorio() {
		Transferencia transferencia = new Transferencia(ORIGEM, DESTINO, MIL, new BigDecimal("28.00"), HOJE, HOJE);
		when(repository.findAllByOrderByDataAgendamentoDescIdDesc()).thenReturn(List.of(transferencia));

		assertThat(service.listarExtrato()).containsExactly(transferencia);
	}

	private static AgendamentoRequest request(String origem, String destino, BigDecimal valor, LocalDate data) {
		return new AgendamentoRequest(origem, destino, valor, data);
	}

}

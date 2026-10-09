package br.com.agendamento.transferencias.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import br.com.agendamento.transferencias.dto.AgendamentoRequest;
import br.com.agendamento.transferencias.entity.Transferencia;
import br.com.agendamento.transferencias.exception.RegraNegocioException;
import br.com.agendamento.transferencias.exception.TaxaNaoAplicavelException;
import br.com.agendamento.transferencias.service.TransferenciaService;

@WebMvcTest(TransferenciaController.class)
class TransferenciaControllerTest {

	private static final String URL = "/api/transferencias";
	private static final LocalDate HOJE = LocalDate.of(2026, 10, 7);

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TransferenciaService service;

	@Test
	void deveAgendarERetornar201ComLocation() throws Exception {
		Transferencia salva = transferencia(1L);
		when(service.agendar(any(AgendamentoRequest.class))).thenReturn(salva);

		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1234567890", "0987654321",
				"1000.00", "2026-10-12")))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", endsWith("/api/transferencias/1")))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.contaOrigem").value("1234567890"))
				.andExpect(jsonPath("$.taxa").value(12.00))
				.andExpect(jsonPath("$.dataTransferencia").value("2026-10-12"))
				.andExpect(jsonPath("$.dataAgendamento").value("2026-10-07"));

		ArgumentCaptor<AgendamentoRequest> captor = ArgumentCaptor.forClass(AgendamentoRequest.class);
		verify(service).agendar(captor.capture());
		AgendamentoRequest enviado = captor.getValue();
		assertThat(enviado.getContaOrigem()).isEqualTo("1234567890");
		assertThat(enviado.getContaDestino()).isEqualTo("0987654321");
		assertThat(enviado.getValor()).isEqualByComparingTo("1000.00");
		assertThat(enviado.getDataTransferencia()).isEqualTo(HOJE.plusDays(5));
	}

	@Test
	void deveRetornar400ComCamposInvalidos() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("123", "abcdefghij",
				"0", "2026-10-12")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.campos[*].campo", containsInAnyOrder("contaOrigem", "contaDestino", "valor")));

		verifyNoInteractions(service);
	}

	@Test
	void deveRetornar400QuandoCamposObrigatoriosAusentes() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos", hasSize(4)))
				.andExpect(jsonPath("$.campos[0].mensagem").value("é obrigatório"));

		verifyNoInteractions(service);
	}

	@Test
	void deveRetornar400QuandoValorTemMaisDeDuasCasasDecimais() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1234567890", "0987654321",
				"10.001", "2026-10-12")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos[0].campo").value("valor"));
	}

	@Test
	void deveRetornar400QuandoJsonEMalFormado() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1234567890", "0987654321",
				"1000.00", "12/10/2026")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").exists());
	}

	@Test
	void deveRetornar422QuandoNaoHaTaxaAplicavel() throws Exception {
		when(service.agendar(any(AgendamentoRequest.class))).thenThrow(new TaxaNaoAplicavelException(51));

		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1234567890", "0987654321",
				"1000.00", "2026-11-27")))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status").value(422))
				.andExpect(jsonPath("$.mensagem").value(new TaxaNaoAplicavelException(51).getMessage()));
	}

	@Test
	void deveRetornar422QuandoContasSaoIguais() throws Exception {
		when(service.agendar(any(AgendamentoRequest.class)))
				.thenThrow(new RegraNegocioException("A conta de destino deve ser diferente da conta de origem."));

		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1234567890", "1234567890",
				"1000.00", "2026-10-12")))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void deveListarExtrato() throws Exception {
		when(service.listarExtrato()).thenReturn(List.of(transferencia(2L), transferencia(1L)));

		mockMvc.perform(get(URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id").value(2))
				.andExpect(jsonPath("$[1].id").value(1));
	}

	@Test
	void deveSimularTaxa() throws Exception {
		when(service.simularTaxa(new BigDecimal("1000.00"), HOJE)).thenReturn(new BigDecimal("28.00"));

		mockMvc.perform(get(URL + "/taxa").param("valor", "1000.00").param("dataTransferencia", "2026-10-07"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.taxa").value(28.00));
	}

	@Test
	void deveRetornar400AoSimularComValorInvalido() throws Exception {
		mockMvc.perform(get(URL + "/taxa").param("valor", "-1").param("dataTransferencia", "2026-10-07"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos[0].campo").value("valor"));

		verifyNoInteractions(service);
	}

	@Test
	void deveRetornar422AoSimularSemTaxaAplicavel() throws Exception {
		when(service.simularTaxa(any(), eq(HOJE.plusDays(60)))).thenThrow(new TaxaNaoAplicavelException(60));

		mockMvc.perform(get(URL + "/taxa").param("valor", "1000.00").param("dataTransferencia", "2026-12-06"))
				.andExpect(status().isUnprocessableEntity());
	}

	private static String json(String origem, String destino, String valor, String data) {
		return String.format(
				"{\"contaOrigem\":\"%s\",\"contaDestino\":\"%s\",\"valor\":%s,\"dataTransferencia\":\"%s\"}",
				origem, destino, valor, data);
	}

	private static Transferencia transferencia(Long id) {
		Transferencia transferencia = new Transferencia("1234567890", "0987654321", new BigDecimal("1000.00"),
				new BigDecimal("12.00"), HOJE.plusDays(5), HOJE);
		ReflectionTestUtils.setField(transferencia, "id", id);
		return transferencia;
	}

}

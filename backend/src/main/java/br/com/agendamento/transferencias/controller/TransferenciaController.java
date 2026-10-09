package br.com.agendamento.transferencias.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.agendamento.transferencias.dto.AgendamentoRequest;
import br.com.agendamento.transferencias.dto.SimulacaoTaxaResponse;
import br.com.agendamento.transferencias.dto.TransferenciaResponse;
import br.com.agendamento.transferencias.entity.Transferencia;
import br.com.agendamento.transferencias.service.TransferenciaService;

@Validated
@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaController {

	private final TransferenciaService service;

	public TransferenciaController(TransferenciaService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<TransferenciaResponse> agendar(@Valid @RequestBody AgendamentoRequest request) {
		Transferencia transferencia = service.agendar(request);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(transferencia.getId())
				.toUri();

		return ResponseEntity.created(location).body(new TransferenciaResponse(transferencia));
	}

	@GetMapping
	public List<TransferenciaResponse> listarExtrato() {
		return service.listarExtrato().stream()
				.map(TransferenciaResponse::new)
				.collect(Collectors.toList());
	}

	@GetMapping("/taxa")
	public SimulacaoTaxaResponse simularTaxa(
			@RequestParam @DecimalMin(value = "0.01", message = "deve ser maior que zero") BigDecimal valor,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataTransferencia) {
		return new SimulacaoTaxaResponse(service.simularTaxa(valor, dataTransferencia));
	}

}

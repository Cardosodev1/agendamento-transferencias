package br.com.agendamento.transferencias.exception;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.agendamento.transferencias.dto.ErroResponse;
import br.com.agendamento.transferencias.dto.ErroResponse.CampoInvalido;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

	private static final String DADOS_INVALIDOS = "Dados inválidos. Verifique os campos informados.";
	private static final String REQUISICAO_INVALIDA = "Requisição inválida. Verifique o formato dos dados enviados.";

	@ExceptionHandler(RegraNegocioException.class)
	public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException ex) {
		return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), List.of());
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErroResponse> tratarParametroInvalido(ConstraintViolationException ex) {
		List<CampoInvalido> campos = ex.getConstraintViolations().stream()
				.map(violacao -> new CampoInvalido(ultimoNo(violacao.getPropertyPath().toString()),
						violacao.getMessage()))
				.collect(Collectors.toList());

		return resposta(HttpStatus.BAD_REQUEST, DADOS_INVALIDOS, campos);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResponse> tratarErroInesperado(Exception ex) {
		LOG.error("Erro inesperado ao processar a requisição", ex);
		return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado. Tente novamente mais tarde.",
				List.of());
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		List<CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
				.map(erro -> new CampoInvalido(erro.getField(), erro.getDefaultMessage()))
				.collect(Collectors.toList());

		return new ResponseEntity<>(new ErroResponse(status.value(), DADOS_INVALIDOS, campos), headers, status);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		String mensagem = status == HttpStatus.BAD_REQUEST ? REQUISICAO_INVALIDA : status.getReasonPhrase();
		return new ResponseEntity<>(new ErroResponse(status.value(), mensagem), headers, status);
	}

	private static ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem,
			List<CampoInvalido> campos) {
		return ResponseEntity.status(status).body(new ErroResponse(status.value(), mensagem, campos));
	}

	private static String ultimoNo(String caminho) {
		return caminho.substring(caminho.lastIndexOf('.') + 1);
	}

}

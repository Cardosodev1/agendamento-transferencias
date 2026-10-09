package br.com.agendamento.transferencias.dto;

import java.util.List;

public class ErroResponse {

	private final int status;
	private final String mensagem;
	private final List<CampoInvalido> campos;

	public ErroResponse(int status, String mensagem) {
		this(status, mensagem, List.of());
	}

	public ErroResponse(int status, String mensagem, List<CampoInvalido> campos) {
		this.status = status;
		this.mensagem = mensagem;
		this.campos = campos;
	}

	public int getStatus() {
		return status;
	}

	public String getMensagem() {
		return mensagem;
	}

	public List<CampoInvalido> getCampos() {
		return campos;
	}

	public static class CampoInvalido {

		private final String campo;
		private final String mensagem;

		public CampoInvalido(String campo, String mensagem) {
			this.campo = campo;
			this.mensagem = mensagem;
		}

		public String getCampo() {
			return campo;
		}

		public String getMensagem() {
			return mensagem;
		}

	}

}

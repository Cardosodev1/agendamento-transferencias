package br.com.agendamento.transferencias.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

public enum FaixaTaxa {

	MESMO_DIA(0, 0, "3.00", "2.5"),
	ATE_10_DIAS(1, 10, "12.00", "0.0"),
	ATE_20_DIAS(11, 20, "0.00", "8.2"),
	ATE_30_DIAS(21, 30, "0.00", "6.9"),
	ATE_40_DIAS(31, 40, "0.00", "4.7"),
	ATE_50_DIAS(41, 50, "0.00", "1.7");

	private final long diasDe;
	private final long diasAte;
	private final BigDecimal valorFixo;
	private final BigDecimal percentual;

	FaixaTaxa(long diasDe, long diasAte, String valorFixo, String percentual) {
		this.diasDe = diasDe;
		this.diasAte = diasAte;
		this.valorFixo = new BigDecimal(valorFixo);
		this.percentual = new BigDecimal(percentual);
	}

	public static Optional<FaixaTaxa> paraDias(long dias) {
		return Arrays.stream(values())
				.filter(faixa -> dias >= faixa.diasDe && dias <= faixa.diasAte)
				.findFirst();
	}

	public BigDecimal calcular(BigDecimal valor) {
		return valorFixo.add(valor.multiply(percentual).movePointLeft(2));
	}

}

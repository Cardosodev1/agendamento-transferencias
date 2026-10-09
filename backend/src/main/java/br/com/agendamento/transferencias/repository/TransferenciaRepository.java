package br.com.agendamento.transferencias.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.agendamento.transferencias.entity.Transferencia;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

	List<Transferencia> findAllByOrderByDataAgendamentoDescIdDesc();

}

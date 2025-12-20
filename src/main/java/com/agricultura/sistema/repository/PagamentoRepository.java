package com.agricultura.sistema.repository;

import com.agricultura.sistema.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByExecucaoId(Long execucaoId);
}

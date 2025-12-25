package com.agricultura.sistema.repository;

import com.agricultura.sistema.model.Execucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExecucaoRepository extends JpaRepository<Execucao, Long> {

    // Find executions by Produtor ID
    List<Execucao> findByProdutorId(Long produtorId);

    // Find executions by Service ID
    List<Execucao> findByServicoId(Long servicoId);

    // Find executions within a date range
    List<Execucao> findByDataExecucaoBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT e FROM Execucao e LEFT JOIN FETCH e.produtor LEFT JOIN FETCH e.servico WHERE e.deletadoEm IS NULL")
    List<Execucao> findAllCompleto();
}

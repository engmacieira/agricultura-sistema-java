package com.agricultura.sistema.repository;

import com.agricultura.sistema.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    // Find services by name containing (case insensitive) for search features
    List<Servico> findByNomeContainingIgnoreCase(String nome);
}

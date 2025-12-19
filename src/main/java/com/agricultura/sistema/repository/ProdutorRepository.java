package com.agricultura.sistema.repository;

import com.agricultura.sistema.model.Produtor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutorRepository extends JpaRepository<Produtor, Long> {

    Page<Produtor> findByNomeContainingIgnoreCaseOrApelidoContainingIgnoreCaseOrCpfContaining(
            String nome, String apelido, String cpf, Pageable pageable);

    // Optional: Query method for exact verification if needed, e.g., for duplicate
    // check
    Optional<Produtor> findByCpf(String cpf);

    // Check by Nome (for creation validation) - finding exact match or similar
    boolean existsByNomeIgnoreCase(String nome);
}

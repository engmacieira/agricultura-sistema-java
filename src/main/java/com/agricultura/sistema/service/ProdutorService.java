package com.agricultura.sistema.service;

import com.agricultura.sistema.exception.ResourceNotFoundException;
import com.agricultura.sistema.model.Produtor;
import com.agricultura.sistema.repository.ProdutorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProdutorService {

    private static final Logger logger = LoggerFactory.getLogger(ProdutorService.class);

    @Autowired
    private ProdutorRepository produtorRepository;

    /**
     * Get all produtores with pagination and optional search term.
     */
    public Page<Produtor> findAll(Pageable pageable, String searchTerm) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return produtorRepository.findByNomeContainingIgnoreCaseOrApelidoContainingIgnoreCaseOrCpfContaining(
                    searchTerm, searchTerm, searchTerm, java.util.Objects.requireNonNull(pageable));
        }
        return produtorRepository.findAll(java.util.Objects.requireNonNull(pageable));
    }

    /**
     * Get all produtores as a list (useful for simple UI Tables).
     */
    public java.util.List<Produtor> listarTodos() {
        return produtorRepository.findAll();
    }

    /**
     * Find produtor by ID.
     */
    public Produtor findById(Long id) {
        return produtorRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Produtor não encontrado com ID: " + id));
    }

    /**
     * Create a new produtor.
     */
    public Produtor create(Produtor produtor) {
        validateProdutor(produtor);

        // Check for duplicates if needed (example: CPF)
        if (produtor.getCpf() != null && produtorRepository.findByCpf(produtor.getCpf()).isPresent()) {
            logger.warn("Tentativa de cadastro com CPF duplicado: {}", produtor.getCpf());
            // For now, just logging, but could throw exception
            // throw new IllegalArgumentException("CPF já cadastrado");
        }

        try {
            return produtorRepository.save(produtor);
        } catch (Exception e) {
            logger.error("Erro ao criar produtor: ", e);
            throw new RuntimeException("Erro ao criar produtor", e);
        }
    }

    /**
     * Update an existing produtor.
     */
    public Produtor update(Long id, Produtor produtorDetails) {
        Produtor existingProdutor = findById(id);

        validateProdutor(produtorDetails);

        existingProdutor.setNome(produtorDetails.getNome());
        existingProdutor.setApelido(produtorDetails.getApelido());
        existingProdutor.setCpf(produtorDetails.getCpf());
        existingProdutor.setRegiao(produtorDetails.getRegiao());
        existingProdutor.setReferencia(produtorDetails.getReferencia());
        existingProdutor.setTelefone(produtorDetails.getTelefone());

        try {
            Produtor updatedProdutor = produtorRepository.save(existingProdutor);
            logger.info("Produtor atualizado! ID: {}", updatedProdutor.getId());
            return updatedProdutor;
        } catch (Exception e) {
            logger.error("Erro ao atualizar produtor ID {}: ", id, e);
            throw new RuntimeException("Erro ao atualizar produtor", e);
        }
    }

    /**
     * Soft delete a produtor.
     */
    public void delete(Long id) {
        if (!produtorRepository.existsById(java.util.Objects.requireNonNull(id))) {
            throw new ResourceNotFoundException("Produtor não encontrado com ID: " + id);
        }

        try {
            produtorRepository.deleteById(java.util.Objects.requireNonNull(id)); // Will trigger @SQLDelete
            logger.info("Soft delete do Produtor ID: {} realizado com sucesso.", id);
        } catch (Exception e) {
            logger.error("Erro ao excluir produtor ID {}: ", id, e);
            throw new RuntimeException("Erro ao excluir produtor", e);
        }
    }

    private void validateProdutor(Produtor produtor) {
        if (produtor.getNome() == null || produtor.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O campo 'nome' é obrigatório");
        }
    }
}

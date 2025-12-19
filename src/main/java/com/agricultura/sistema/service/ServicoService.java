package com.agricultura.sistema.service;

import com.agricultura.sistema.exception.ResourceNotFoundException;
import com.agricultura.sistema.model.Servico;
import com.agricultura.sistema.repository.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServicoService {

    @Autowired
    private ServicoRepository servicoRepository;

    public List<Servico> listarTodos() {
        return servicoRepository.findAll();
    }

    public Servico findById(Long id) {
        return servicoRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com ID: " + id));
    }

    public Servico create(Servico servico) {
        validateServico(servico);
        return servicoRepository.save(java.util.Objects.requireNonNull(servico));
    }

    public Servico update(Long id, Servico servicoDetails) {
        Servico existingServico = findById(id);
        validateServico(servicoDetails);

        existingServico.setNome(servicoDetails.getNome());
        existingServico.setValorUnitario(servicoDetails.getValorUnitario());

        return servicoRepository.save(existingServico);
    }

    public void delete(Long id) {
        if (!servicoRepository.existsById(java.util.Objects.requireNonNull(id))) {
            throw new ResourceNotFoundException("Serviço não encontrado com ID: " + id);
        }
        servicoRepository.deleteById(java.util.Objects.requireNonNull(id));
    }

    private void validateServico(Servico servico) {
        if (servico.getNome() == null || servico.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do serviço é obrigatório.");
        }
        if (servico.getValorUnitario() == null || servico.getValorUnitario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor unitário deve ser maior que zero.");
        }
    }
}

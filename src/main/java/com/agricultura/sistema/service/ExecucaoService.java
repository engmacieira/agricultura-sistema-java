package com.agricultura.sistema.service;

import com.agricultura.sistema.exception.ResourceNotFoundException;
import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Produtor;
import com.agricultura.sistema.model.Servico;
import com.agricultura.sistema.repository.ExecucaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ExecucaoService {

    @Autowired
    private ExecucaoRepository execucaoRepository;

    @Autowired
    private ProdutorService produtorService;

    @Autowired
    private ServicoService servicoService;

    public List<Execucao> listarTodos() {
        return execucaoRepository.findAll();
    }

    public Execucao findById(Long id) {
        return execucaoRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Execução não encontrada com ID: " + id));
    }

    public Execucao create(Execucao execucao) {
        // Validate and fetch Produtor/Servico to ensure they exist
        Produtor produtor = produtorService.findById(execucao.getProdutor().getId());
        Servico servico = servicoService.findById(execucao.getServico().getId());

        // Update references with managed entities
        execucao.setProdutor(produtor);
        execucao.setServico(servico);

        calcularValorTotal(execucao);

        return execucaoRepository.save(java.util.Objects.requireNonNull(execucao));
    }

    public Execucao update(Long id, Execucao execucaoDetails) {
        Execucao existingExecucao = findById(id);

        Produtor produtor = produtorService.findById(execucaoDetails.getProdutor().getId());
        Servico servico = servicoService.findById(execucaoDetails.getServico().getId());

        existingExecucao.setProdutor(produtor);
        existingExecucao.setServico(servico);
        existingExecucao.setDataExecucao(execucaoDetails.getDataExecucao());
        existingExecucao.setHorasPrestadas(execucaoDetails.getHorasPrestadas());

        calcularValorTotal(existingExecucao);

        return execucaoRepository.save(java.util.Objects.requireNonNull(existingExecucao));
    }

    public void delete(Long id) {
        if (!execucaoRepository.existsById(java.util.Objects.requireNonNull(id))) {
            throw new ResourceNotFoundException("Execução não encontrada com ID: " + id);
        }
        execucaoRepository.deleteById(java.util.Objects.requireNonNull(id));
    }

    private void calcularValorTotal(Execucao execucao) {
        if (execucao.getHorasPrestadas() == null || execucao.getHorasPrestadas().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("As horas prestadas devem ser maiores que zero.");
        }

        BigDecimal valorUnitario = execucao.getServico().getValorUnitario();
        BigDecimal valorTotal = valorUnitario.multiply(execucao.getHorasPrestadas());

        execucao.setValorTotal(valorTotal);
    }
}

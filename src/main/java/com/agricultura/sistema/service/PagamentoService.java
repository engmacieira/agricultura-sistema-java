package com.agricultura.sistema.service;

import com.agricultura.sistema.exception.ResourceNotFoundException;
import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Pagamento;
import com.agricultura.sistema.repository.ExecucaoRepository; // Se usar ExecucaoService, mude aqui
import com.agricultura.sistema.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;

    // Nota: Para manter consistência com o teste que mocka o Service,
    // idealmente injetamos ExecucaoService, mas se usar Repository direto funciona
    // também,
    // desde que a lógica esteja aqui. Vou manter como estava no seu código original
    // (Repository)
    // para não quebrar a injeção do Spring.
    private final ExecucaoRepository execucaoRepository;

    /**
     * Registra um novo pagamento.
     */
    @Transactional
    public Pagamento create(Pagamento pagamento) {
        if (pagamento.getExecucao() == null || pagamento.getExecucao().getId() == null) {
            throw new IllegalArgumentException("Pagamento deve estar vinculado a uma Execução válida.");
        }

        long execucaoId = pagamento.getExecucao().getId();
        Execucao execucao = execucaoRepository.findById(execucaoId)
                .orElseThrow(() -> new IllegalArgumentException("Execução não encontrada."));

        // Validação de Saldo
        List<Pagamento> pagamentosAnteriores = pagamentoRepository.findByExecucaoId(execucao.getId());

        BigDecimal somaPagamentos = BigDecimal.ZERO;
        if (pagamentosAnteriores != null) {
            somaPagamentos = pagamentosAnteriores.stream()
                    .map(Pagamento::getValorPago)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal novoTotal = somaPagamentos.add(pagamento.getValorPago());

        if (novoTotal.compareTo(execucao.getValorTotal()) > 0) {
            throw new IllegalArgumentException("Valor excede o saldo devedor");
        }

        pagamento.setExecucao(execucao);
        return pagamentoRepository.save(pagamento);
    }

    public List<Pagamento> listarPorExecucao(Long execucaoId) {
        return pagamentoRepository.findByExecucaoId(execucaoId);
    }

    // --- CRUD ADICIONAL ---

    public Pagamento findById(Long id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado com ID: " + id));
    }

    public List<Pagamento> listarTodos() {
        return pagamentoRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        if (!pagamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pagamento não encontrado com ID: " + id);
        }
        pagamentoRepository.deleteById(id);
    }
}
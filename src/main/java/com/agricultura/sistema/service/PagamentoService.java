package com.agricultura.sistema.service;

import com.agricultura.sistema.exception.ResourceNotFoundException;
import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Pagamento;
import com.agricultura.sistema.repository.ExecucaoRepository;
import com.agricultura.sistema.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects; // <--- Importante para a correção

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final ExecucaoRepository execucaoRepository;

    /**
     * Registra um novo pagamento, validando se não excede o valor total da
     * execução.
     */
    @Transactional
    public Pagamento create(Pagamento pagamento) {
        // 1. Validação de Vínculo
        if (pagamento.getExecucao() == null || pagamento.getExecucao().getId() == null) {
            throw new IllegalArgumentException("Pagamento deve estar vinculado a uma Execução válida.");
        }

        long execucaoId = pagamento.getExecucao().getId();
        Execucao execucao = execucaoRepository.findById(execucaoId)
                .orElseThrow(() -> new IllegalArgumentException("Execução não encontrada."));

        // 2. Validação de Saldo (Regra de Negócio)
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

        // 3. Persistência
        pagamento.setExecucao(execucao);
        return pagamentoRepository.save(pagamento);
    }

    /**
     * Lista pagamentos vinculados a uma execução específica.
     */
    public List<Pagamento> listarPorExecucao(Long execucaoId) {
        // requireNonNull garante que não passamos null para o repositório
        return pagamentoRepository.findByExecucaoId(Objects.requireNonNull(execucaoId));
    }

    // --- CRUD PADRÃO ---

    public Pagamento findById(Long id) {
        // Correção aqui: Validamos o ID antes de passar
        return pagamentoRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado com ID: " + id));
    }

    public List<Pagamento> listarTodos() {
        return pagamentoRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        // Correção aqui: Validamos o ID nas duas chamadas
        Long safeId = Objects.requireNonNull(id);

        if (!pagamentoRepository.existsById(safeId)) {
            throw new ResourceNotFoundException("Pagamento não encontrado com ID: " + safeId);
        }
        pagamentoRepository.deleteById(safeId);
    }
}
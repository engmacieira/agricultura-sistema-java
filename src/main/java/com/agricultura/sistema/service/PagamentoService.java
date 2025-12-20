package com.agricultura.sistema.service;

import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Pagamento;
import com.agricultura.sistema.repository.ExecucaoRepository;
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
    private final ExecucaoRepository execucaoRepository;

    @Transactional
    public Pagamento create(Pagamento pagamento) {
        if (pagamento.getExecucao() == null || pagamento.getExecucao().getId() == null) {
            throw new IllegalArgumentException("Pagamento deve estar vinculado a uma Execução válida.");
        }

        long execucaoId = pagamento.getExecucao().getId();
        Execucao execucao = execucaoRepository.findById(execucaoId)
                .orElseThrow(() -> new IllegalArgumentException("Execução não encontrada."));

        // Validar saldo
        List<Pagamento> pagamentosAnteriores = pagamentoRepository.findByExecucaoId(execucao.getId());
        BigDecimal somaPagamentos = pagamentosAnteriores.stream()
                .map(Pagamento::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal novoTotal = somaPagamentos.add(pagamento.getValorPago());

        if (novoTotal.compareTo(execucao.getValorTotal()) > 0) {
            throw new IllegalArgumentException("Valor excede o saldo devedor");
        }

        // Ensure the correct execution instance is set (though it likely is)
        pagamento.setExecucao(execucao);
        return pagamentoRepository.save(pagamento);
    }

    public List<Pagamento> listarPorExecucao(Long execucaoId) {
        return pagamentoRepository.findByExecucaoId(execucaoId);
    }
}

package com.agricultura.sistema.service;

import com.agricultura.sistema.dto.ResumoFinanceiroDTO;
import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Pagamento;
import com.agricultura.sistema.model.Produtor;
import com.agricultura.sistema.repository.ProdutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <--- 1. Importante!

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class RelatorioService {

    @Autowired
    private ProdutorRepository produtorRepository;

    // 2. Adicionamos @Transactional. readOnly = true otimiza a performance para
    // leitura.
    @Transactional(readOnly = true)
    public List<ResumoFinanceiroDTO> gerarRelatorioDevedores() {
        List<ResumoFinanceiroDTO> relatorio = new ArrayList<>();
        List<Produtor> produtores = produtorRepository.findAll();

        for (Produtor produtor : produtores) {
            BigDecimal totalExecucoes = BigDecimal.ZERO;
            BigDecimal totalPagos = BigDecimal.ZERO;

            // Como estamos "dentro da transação", o Hibernate busca as execuções sob
            // demanda
            if (produtor.getExecucoes() != null) {
                for (Execucao execucao : produtor.getExecucoes()) {
                    if (execucao.getValorTotal() != null) {
                        totalExecucoes = totalExecucoes.add(execucao.getValorTotal());
                    }

                    // E também busca os pagamentos sob demanda
                    if (execucao.getPagamentos() != null) {
                        for (Pagamento pagamento : execucao.getPagamentos()) {
                            if (pagamento.getValorPago() != null) {
                                totalPagos = totalPagos.add(pagamento.getValorPago());
                            }
                        }
                    }
                }
            }

            BigDecimal totalDivida = totalExecucoes.subtract(totalPagos);

            // Apenas adiciona se tiver dívida positiva
            if (totalDivida.compareTo(BigDecimal.ZERO) > 0) {
                relatorio.add(new ResumoFinanceiroDTO(produtor.getNome(), totalDivida, totalPagos));
            }
        }

        return relatorio;
    }
}
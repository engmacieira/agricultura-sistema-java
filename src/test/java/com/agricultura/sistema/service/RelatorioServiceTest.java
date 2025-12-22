package com.agricultura.sistema.service;

import com.agricultura.sistema.dto.ResumoFinanceiroDTO;
import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Pagamento;
import com.agricultura.sistema.model.Produtor;
import com.agricultura.sistema.repository.ProdutorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RelatorioServiceTest {

    @InjectMocks
    private RelatorioService relatorioService;

    @Mock
    private ProdutorRepository produtorRepository;

    @Test
    @DisplayName("Deve listar apenas produtores com dívida ativa")
    void testGerarRelatorioDevedores() {
        // --- CENÁRIO (ARRANGE) ---

        // 1. Produtor Devedor (João)
        Produtor joao = new Produtor();
        joao.setNome("João Devedor");

        Execucao execJoao = new Execucao();
        execJoao.setValorTotal(new BigDecimal("1000.00")); // Deve 1000

        Pagamento pagJoao = new Pagamento();
        pagJoao.setValorPago(new BigDecimal("300.00")); // Pagou 300

        // Relacionamento: Execução tem pagamentos
        execJoao.setPagamentos(Arrays.asList(pagJoao));
        // Relacionamento: Produtor tem execuções
        joao.setExecucoes(Arrays.asList(execJoao));

        // 2. Produtor Quitado (Maria)
        Produtor maria = new Produtor();
        maria.setNome("Maria Quitada");

        Execucao execMaria = new Execucao();
        execMaria.setValorTotal(new BigDecimal("500.00")); // Deve 500

        Pagamento pagMaria = new Pagamento();
        pagMaria.setValorPago(new BigDecimal("500.00")); // Pagou 500

        execMaria.setPagamentos(Arrays.asList(pagMaria));
        maria.setExecucoes(Arrays.asList(execMaria));

        // Mock do Repositório
        when(produtorRepository.findAll()).thenReturn(Arrays.asList(joao, maria));

        // --- AÇÃO (ACT) ---
        List<ResumoFinanceiroDTO> resultado = relatorioService.gerarRelatorioDevedores();

        // --- VERIFICAÇÃO (ASSERT) ---
        assertNotNull(resultado);
        assertEquals(1, resultado.size(), "Deveria retornar apenas 1 produtor (o devedor)");

        ResumoFinanceiroDTO dto = resultado.get(0);
        assertEquals("João Devedor", dto.getNomeProdutor());
        assertEquals(new BigDecimal("300.00"), dto.getValorTotalPago());

        // Dívida esperada: 1000 - 300 = 700
        assertEquals(new BigDecimal("700.00"), dto.getValorTotalDivida());
    }

    @Test
    @DisplayName("Deve retornar lista vazia se ninguém dever nada")
    void testGerarRelatorioSemDevedores() {
        Produtor maria = new Produtor();
        maria.setNome("Maria Quitada");
        maria.setExecucoes(Collections.emptyList());

        when(produtorRepository.findAll()).thenReturn(Arrays.asList(maria));

        List<ResumoFinanceiroDTO> resultado = relatorioService.gerarRelatorioDevedores();

        assertTrue(resultado.isEmpty(), "A lista deveria estar vazia");
    }
}
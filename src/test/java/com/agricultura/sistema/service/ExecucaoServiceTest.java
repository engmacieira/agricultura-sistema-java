package com.agricultura.sistema.service;

import com.agricultura.sistema.exception.ResourceNotFoundException;
import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Produtor;
import com.agricultura.sistema.model.Servico;
import com.agricultura.sistema.repository.ExecucaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null") // <--- A MÁGICA: Silencia os alertas de Null Safety do Mockito
class ExecucaoServiceTest {

    @InjectMocks
    private ExecucaoService execucaoService;

    @Mock
    private ExecucaoRepository execucaoRepository;

    @Mock
    private ProdutorService produtorService;

    @Mock
    private ServicoService servicoService;

    private Execucao execucaoPadrao;
    private Produtor produtorPadrao;
    private Servico servicoPadrao;

    @BeforeEach
    void setup() {
        // Preparando dados (Fixtures)
        produtorPadrao = new Produtor();
        produtorPadrao.setId(1L);
        produtorPadrao.setNome("João da Silva");

        servicoPadrao = new Servico();
        servicoPadrao.setId(1L);
        servicoPadrao.setNome("Aragem");
        servicoPadrao.setValorUnitario(new BigDecimal("100.00"));

        execucaoPadrao = new Execucao();
        execucaoPadrao.setId(10L);
        execucaoPadrao.setProdutor(produtorPadrao);
        execucaoPadrao.setServico(servicoPadrao);
        execucaoPadrao.setDataExecucao(LocalDate.now());
        execucaoPadrao.setHorasPrestadas(new BigDecimal("5"));
    }

    @Test
    @DisplayName("Deve criar execução com cálculo automático do valor total")
    void testCreateExecucaoComCalculo() {
        // Arrange
        when(produtorService.findById(1L)).thenReturn(produtorPadrao);
        when(servicoService.findById(1L)).thenReturn(servicoPadrao);
        when(execucaoRepository.save(any(Execucao.class))).thenReturn(execucaoPadrao);

        // Act
        execucaoPadrao.setValorTotal(null); // Reseta para garantir que o service calcula
        Execucao salva = execucaoService.create(execucaoPadrao);

        // Assert
        assertNotNull(salva.getValorTotal(), "O valor total não deve ser nulo");
        assertEquals(new BigDecimal("500.00"), salva.getValorTotal(), "O cálculo (5 * 100) está incorreto");
        verify(execucaoRepository, times(1)).save(execucaoPadrao);
    }

    @Test
    @DisplayName("Deve buscar execução por ID com sucesso")
    void testFindById() {
        when(execucaoRepository.findById(10L)).thenReturn(Optional.of(execucaoPadrao));

        Execucao encontrada = execucaoService.findById(10L);

        assertNotNull(encontrada);
        assertEquals(10L, encontrada.getId());
        assertEquals("João da Silva", encontrada.getProdutor().getNome());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar ID inexistente")
    void testFindByIdInexistente() {
        when(execucaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            execucaoService.findById(99L);
        });
    }

    @Test
    @DisplayName("Deve listar todas as execuções")
    void testListarTodos() {
        when(execucaoRepository.findAllCompleto()).thenReturn(Arrays.asList(execucaoPadrao, new Execucao()));

        List<Execucao> lista = execucaoService.listarTodos();

        assertEquals(2, lista.size());
        verify(execucaoRepository, times(1)).findAllCompleto();
    }

    @Test
    @DisplayName("Deve atualizar as horas prestadas e recalcular valor")
    void testUpdate() {
        // Arrange
        Execucao dadosAtualizados = new Execucao();
        dadosAtualizados.setProdutor(produtorPadrao);
        dadosAtualizados.setServico(servicoPadrao);
        dadosAtualizados.setDataExecucao(LocalDate.now());
        dadosAtualizados.setHorasPrestadas(new BigDecimal("10")); // Mudou para 10 horas

        when(execucaoRepository.findById(10L)).thenReturn(Optional.of(execucaoPadrao));
        when(produtorService.findById(1L)).thenReturn(produtorPadrao);
        when(servicoService.findById(1L)).thenReturn(servicoPadrao);
        when(execucaoRepository.save(any(Execucao.class))).thenReturn(execucaoPadrao);

        // Act
        Execucao atualizada = execucaoService.update(10L, dadosAtualizados);

        // Assert
        // Verifica se o valor foi recalculado para 10 * 100 = 1000
        assertEquals(new BigDecimal("1000.00"), atualizada.getValorTotal());
        verify(execucaoRepository, times(1)).save(execucaoPadrao);
    }

    @Test
    @DisplayName("Deve deletar execução")
    void testDelete() {
        when(execucaoRepository.existsById(10L)).thenReturn(true);
        doNothing().when(execucaoRepository).deleteById(10L);

        assertDoesNotThrow(() -> execucaoService.delete(10L));

        verify(execucaoRepository, times(1)).deleteById(10L);
    }
}
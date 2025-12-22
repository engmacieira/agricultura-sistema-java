package com.agricultura.sistema.service;

import com.agricultura.sistema.exception.ResourceNotFoundException;
import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Pagamento;
import com.agricultura.sistema.repository.ExecucaoRepository;
import com.agricultura.sistema.repository.PagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PagamentoServiceTest {

    @InjectMocks
    private PagamentoService pagamentoService;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private ExecucaoRepository execucaoRepository; // <--- CORREÇÃO: Agora mockamos o Repositório

    private Pagamento pagamentoPadrao;
    private Execucao execucaoPadrao;

    @BeforeEach
    void setup() {
        execucaoPadrao = new Execucao();
        execucaoPadrao.setId(100L);
        execucaoPadrao.setValorTotal(new BigDecimal("1000.00"));

        pagamentoPadrao = new Pagamento();
        pagamentoPadrao.setId(1L);
        pagamentoPadrao.setValorPago(new BigDecimal("200.00"));
        pagamentoPadrao.setDataPagamento(LocalDate.now());
        pagamentoPadrao.setExecucao(execucaoPadrao);
    }

    @Test
    @DisplayName("Deve registrar pagamento com sucesso")
    void testCreateSuccess() {
        // Arrange
        when(execucaoRepository.findById(100L)).thenReturn(Optional.of(execucaoPadrao));
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamentoPadrao);

        // Act
        Pagamento criado = pagamentoService.create(pagamentoPadrao);

        // Assert
        assertNotNull(criado);
        assertEquals(new BigDecimal("200.00"), criado.getValorPago());
        verify(pagamentoRepository, times(1)).save(pagamentoPadrao);
    }

    @Test
    @DisplayName("Deve validar se valor excede o saldo")
    void testCreateValorExcedente() {
        // Cenário: Já pagou 900, tenta pagar mais 200 (Total 1100 > 1000)
        Pagamento anterior = new Pagamento();
        anterior.setValorPago(new BigDecimal("900.00"));

        when(execucaoRepository.findById(100L)).thenReturn(Optional.of(execucaoPadrao));
        when(pagamentoRepository.findByExecucaoId(100L)).thenReturn(List.of(anterior));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pagamentoService.create(pagamentoPadrao);
        });

        assertEquals("Valor excede o saldo devedor", exception.getMessage());
    }

    @Test
    @DisplayName("Deve buscar pagamento por ID")
    void testFindById() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamentoPadrao));

        Pagamento encontrado = pagamentoService.findById(1L);

        assertEquals(1L, encontrado.getId());
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar ID inexistente")
    void testFindByIdNotFound() {
        when(pagamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            pagamentoService.findById(99L);
        });
    }

    @Test
    @DisplayName("Deve listar todos os pagamentos")
    void testListarTodos() {
        when(pagamentoRepository.findAll()).thenReturn(List.of(pagamentoPadrao));

        List<Pagamento> lista = pagamentoService.listarTodos();

        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Deve deletar pagamento")
    void testDelete() {
        when(pagamentoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pagamentoRepository).deleteById(1L);

        assertDoesNotThrow(() -> pagamentoService.delete(1L));

        verify(pagamentoRepository, times(1)).deleteById(1L);
    }
}
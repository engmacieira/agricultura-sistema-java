package com.agricultura.sistema.service;

import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Pagamento;
import com.agricultura.sistema.repository.ExecucaoRepository;
import com.agricultura.sistema.repository.PagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private ExecucaoRepository execucaoRepository;

    @InjectMocks
    private PagamentoService pagamentoService;

    private Execucao execucao;

    @BeforeEach
    void setUp() {
        execucao = new Execucao();
        execucao.setId(1L);
        execucao.setValorTotal(new BigDecimal("1000.00"));
    }

    @Test
    @SuppressWarnings("null")
    void create_DeveSalvarPagamento_QuandoValorValido() {
        Pagamento novoPagamento = new Pagamento();
        novoPagamento.setExecucao(execucao);
        novoPagamento.setValorPago(new BigDecimal("200.00"));
        novoPagamento.setDataPagamento(LocalDate.now());

        when(execucaoRepository.findById(1L)).thenReturn(Optional.of(execucao));
        when(pagamentoRepository.findByExecucaoId(1L)).thenReturn(Collections.emptyList());
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(novoPagamento);

        Pagamento salvo = pagamentoService.create(novoPagamento);

        assertNotNull(salvo);
        verify(pagamentoRepository, times(1)).save(novoPagamento);
    }

    @Test
    @SuppressWarnings("null")
    void create_DeveLancarException_QuandoValorExcedeSaldo() {
        // Já existe um pagamento de 900
        Pagamento pagamentoExistente = new Pagamento();
        pagamentoExistente.setValorPago(new BigDecimal("900.00"));

        // Tentativa de pagar mais 200 (Total 1100 > 1000)
        Pagamento novoPagamento = new Pagamento();
        novoPagamento.setExecucao(execucao);
        novoPagamento.setValorPago(new BigDecimal("200.00"));
        novoPagamento.setDataPagamento(LocalDate.now());

        when(execucaoRepository.findById(1L)).thenReturn(Optional.of(execucao));
        when(pagamentoRepository.findByExecucaoId(1L)).thenReturn(Arrays.asList(pagamentoExistente));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pagamentoService.create(novoPagamento);
        });

        assertEquals("Valor excede o saldo devedor", exception.getMessage());
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
    }

    @Test
    @SuppressWarnings("null")
    void create_DeveSalvarPagamento_QuandoValorIgualAoSaldoRestante() {
        // Já existe um pagamento de 500
        Pagamento pagamentoExistente = new Pagamento();
        pagamentoExistente.setValorPago(new BigDecimal("500.00"));

        // Tentativa de pagar mais 500 (Total 1000 == 1000) -> OK
        Pagamento novoPagamento = new Pagamento();
        novoPagamento.setExecucao(execucao);
        novoPagamento.setValorPago(new BigDecimal("500.00"));
        novoPagamento.setDataPagamento(LocalDate.now());

        when(execucaoRepository.findById(1L)).thenReturn(Optional.of(execucao));
        when(pagamentoRepository.findByExecucaoId(1L)).thenReturn(Arrays.asList(pagamentoExistente));
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(novoPagamento);

        assertDoesNotThrow(() -> pagamentoService.create(novoPagamento));
        verify(pagamentoRepository, times(1)).save(novoPagamento);
    }
}

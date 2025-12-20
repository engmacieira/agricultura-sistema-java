package com.agricultura.sistema.service;

import com.agricultura.sistema.model.Execucao;
import com.agricultura.sistema.model.Produtor;
import com.agricultura.sistema.model.Servico;
import com.agricultura.sistema.repository.ExecucaoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class ExecucaoServiceTest {

    @Mock
    private ExecucaoRepository execucaoRepository;

    @Mock
    private ProdutorService produtorService; // <--- O culpado estava aqui (sem comportamento definido)

    @Mock
    private ServicoService servicoService; // <--- E aqui

    @InjectMocks
    private ExecucaoService execucaoService;

    @Test
    public void deveCalcularValorTotalCorretamente() {
        // 1. CENÁRIO
        Produtor produtorMock = Produtor.builder().id(1L).nome("Seu Zé").build();
        Servico servicoMock = Servico.builder().id(2L).nome("Trator").valorUnitario(new BigDecimal("100.00")).build();

        Execucao novaExecucao = Execucao.builder()
                .produtor(produtorMock)
                .servico(servicoMock)
                .horasPrestadas(new BigDecimal("5.0"))
                .build();

        // 2. MOCKS (Ensinando os serviços a responderem)

        // CORREÇÃO: Ensinamos o Mockito a devolver os objetos quando buscados por ID
        Mockito.when(produtorService.findById(1L)).thenReturn(produtorMock);
        Mockito.when(servicoService.findById(2L)).thenReturn(servicoMock);

        // Ensinamos o repositório a devolver a própria execução salva
        Mockito.when(execucaoRepository.save(Mockito.any(Execucao.class)))
                .thenAnswer(i -> (Execucao) i.getArguments()[0]);

        // 3. AÇÃO
        Execucao salva = execucaoService.create(novaExecucao);

        // 4. VERIFICAÇÃO
        Assertions.assertEquals(new BigDecimal("500.00"), salva.getValorTotal());

        System.out.println("Teste de Cálculo: Sucesso! Valor calculado: " + salva.getValorTotal());
    }
}
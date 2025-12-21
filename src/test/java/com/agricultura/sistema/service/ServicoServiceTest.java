package com.agricultura.sistema.service;

import com.agricultura.sistema.model.Servico;
import com.agricultura.sistema.repository.ServicoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    @Test
    public void testCreateServicoSuccess() {
        // Arrange (Preparar)
        Servico servico = Servico.builder()
                .nome("Colheita Mecanizada")
                .descricao("Colheita de soja com maquinário")
                .valorUnitario(BigDecimal.valueOf(1500.00)) // CORRIGIDO: BigDecimal e nome do campo
                .build();

        Mockito.when(servicoRepository.save(Mockito.any(Servico.class))).thenReturn(servico);

        // Act (Agir)
        Servico created = servicoService.create(servico);

        // Assert (Verificar)
        Assertions.assertNotNull(created);
        Assertions.assertEquals("Colheita Mecanizada", created.getNome());
        Assertions.assertEquals(BigDecimal.valueOf(1500.00), created.getValorUnitario()); // CORRIGIDO
        Mockito.verify(servicoRepository, Mockito.times(1)).save(servico);
    }

    @Test
    public void testCreateServicoValidationError() {
        // Tentar criar serviço sem nome deve falhar
        Servico servico = Servico.builder()
                .valorUnitario(BigDecimal.valueOf(100.0)) // CORRIGIDO
                .build();

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            servicoService.create(servico);
        });

        // Verifique se a mensagem bate com a sua implementação no Service real
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("nome"));
        Mockito.verify(servicoRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testFindByIdSuccess() {
        Servico servico = Servico.builder().id(1L).nome("Arado").build();
        Mockito.when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        Servico found = servicoService.findById(1L);

        Assertions.assertEquals("Arado", found.getNome());
    }

    @Test
    public void testDeleteSuccess() {
        Mockito.when(servicoRepository.existsById(1L)).thenReturn(true);
        Mockito.doNothing().when(servicoRepository).deleteById(1L);

        servicoService.delete(1L);

        Mockito.verify(servicoRepository, Mockito.times(1)).deleteById(1L);
    }
}
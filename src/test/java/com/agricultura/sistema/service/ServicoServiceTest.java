package com.agricultura.sistema.service;

import com.agricultura.sistema.exception.ResourceNotFoundException;
import com.agricultura.sistema.model.Servico;
import com.agricultura.sistema.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ServicoServiceTest {

    @InjectMocks
    private ServicoService servicoService;

    @Mock
    private ServicoRepository servicoRepository;

    private Servico servicoPadrao;

    @BeforeEach
    void setup() {
        servicoPadrao = new Servico();
        servicoPadrao.setId(1L);
        servicoPadrao.setNome("Colheita Mecanizada");
        servicoPadrao.setDescricao("Colheita de soja");
        servicoPadrao.setValorUnitario(new BigDecimal("150.00")); // R$ 150,00 por ha/hora
    }

    // --- CREATE ---

    @Test
    @DisplayName("Deve criar serviço com dados válidos")
    void testCreateSuccess() {
        when(servicoRepository.save(any(Servico.class))).thenReturn(servicoPadrao);

        Servico criado = servicoService.create(servicoPadrao);

        assertNotNull(criado);
        assertEquals(new BigDecimal("150.00"), criado.getValorUnitario());
        verify(servicoRepository, times(1)).save(servicoPadrao);
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios ao criar")
    void testCreateValidacao() {
        // Supondo que o serviço valida nome nulo
        servicoPadrao.setNome(null);

        // Se o seu Service não tiver validação explícita, esse teste vai falhar (o que
        // é bom, pois descobre um bug!)
        // Se falhar, adicione a validação no Service: if(nome == null) throw new
        // IllegalArgumentException...
        assertThrows(IllegalArgumentException.class, () -> {
            servicoService.create(servicoPadrao);
        });

        verify(servicoRepository, never()).save(any());
    }

    // --- FIND ---

    @Test
    @DisplayName("Deve buscar serviço por ID")
    void testFindById() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoPadrao));

        Servico encontrado = servicoService.findById(1L);

        assertEquals("Colheita Mecanizada", encontrado.getNome());
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar ID inexistente")
    void testFindByIdNotFound() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            servicoService.findById(99L);
        });
    }

    @Test
    @DisplayName("Deve listar todos os serviços")
    void testListarTodos() {
        when(servicoRepository.findAll()).thenReturn(List.of(servicoPadrao));

        List<Servico> lista = servicoService.listarTodos();

        assertEquals(1, lista.size());
        assertEquals(servicoPadrao, lista.get(0));
    }

    // --- UPDATE ---

    @Test
    @DisplayName("Deve atualizar valor do serviço")
    void testUpdate() {
        Servico novosDados = new Servico();
        novosDados.setNome("Colheita Premium");
        novosDados.setValorUnitario(new BigDecimal("200.00")); // Aumento de preço

        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoPadrao));
        when(servicoRepository.save(any(Servico.class))).thenReturn(servicoPadrao);

        Servico atualizado = servicoService.update(1L, novosDados);

        // O mock retorna o objeto que foi modificado pelo service
        assertEquals("Colheita Premium", atualizado.getNome());
        assertEquals(new BigDecimal("200.00"), atualizado.getValorUnitario());

        verify(servicoRepository, times(1)).save(servicoPadrao);
    }

    // --- DELETE ---

    @Test
    @DisplayName("Deve deletar serviço existente")
    void testDelete() {
        when(servicoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(servicoRepository).deleteById(1L);

        assertDoesNotThrow(() -> servicoService.delete(1L));

        verify(servicoRepository, times(1)).deleteById(1L);
    }
}